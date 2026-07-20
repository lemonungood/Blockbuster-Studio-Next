package mchorse.bbs_mod.forms.renderers;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.StringReader;
import mchorse.bbs_mod.BBSModClient;
import mchorse.bbs_mod.client.BBSShaders;
import mchorse.bbs_mod.forms.CustomVertexConsumer;
import mchorse.bbs_mod.forms.FormUtilsClient;
import mchorse.bbs_mod.forms.ITickable;
import mchorse.bbs_mod.forms.entities.IEntity;
import mchorse.bbs_mod.forms.forms.MobForm;
import mchorse.bbs_mod.mixin.LimbAnimatorAccessor;
import mchorse.bbs_mod.resources.Link;
import mchorse.bbs_mod.ui.framework.UIContext;
import mchorse.bbs_mod.utils.MathUtils;
import mchorse.bbs_mod.utils.PoseStackUtils;
import mchorse.bbs_mod.utils.PlayerUtils;
import mchorse.bbs_mod.utils.joml.Vectors;
import mchorse.bbs_mod.utils.pose.Pose;
import mchorse.bbs_mod.utils.pose.Transform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;

// [MC 26.2 REMOVED] import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.model.EntityModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
// [MC26.2] import net.minecraft.nbt.NbtUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.Identifier;
import org.joml.Quaternionf;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MobFormRenderer extends FormRenderer<MobForm> implements ITickable
{
    private static final Map<Class, Map<String, ModelPart>> parts = new HashMap<>();
    private static final Map<ModelPart, Transform> cache = new HashMap<>();
    private static Pose currentPose;
    private static Pose currentPoseOverlay;

    public static final GameProfile WIDE = new GameProfile(UUID.fromString("b99a2400-28a8-4288-92dc-924beafbf756"), "McHorseYT");
    public static final GameProfile SLIM = new GameProfile(UUID.fromString("5477bd28-e672-4f87-a209-c03cf75f3606"), "osmiq");

    private Entity entity;

    private String lastId = "";
    private String lastNBT = "";
    private boolean lastSlim;

    public float prevInteractionHandSwing;
    private float prevYawHead;
    private float prevPitch;

    public static Pose getCurrentPose()
    {
        return currentPose;
    }

    public static Pose getCurrentPoseOverlay()
    {
        return currentPoseOverlay;
    }

    public static Map<Class, Map<String, ModelPart>> getParts()
    {
        return parts;
    }

    public static Map<ModelPart, Transform> getCache()
    {
        return cache;
    }

    public MobFormRenderer(MobForm form)
    {
        super(form);
    }

    @Override
    public List<String> getBones()
    {
        this.ensureEntity();

        if (this.entity != null)
        {
            Map<String, ModelPart> stringModelPartMap = parts.get(this.entity.getClass());

            if (stringModelPartMap == null)
            {
                stringModelPartMap = new HashMap<>();

                if (Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(this.entity) instanceof LivingEntityRenderer renderer)
                {
                    EntityModel model = renderer.getModel();
                    Set<Field> fields = new HashSet<>();
                    Class aClass = model.getClass();

                    while (aClass != Object.class)
                    {
                        for (Field field : aClass.getDeclaredFields())
                        {
                            fields.add(field);
                        }

                        aClass = aClass.getSuperclass();
                    }

                    for (Field declaredField : fields)
                    {
                        if (declaredField.getType().equals(ModelPart.class))
                        {
                            try
                            {
                                declaredField.setAccessible(true);

                                ModelPart part = (ModelPart) declaredField.get(model);

                                stringModelPartMap.put(declaredField.getName(), part);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                parts.put(this.entity.getClass(), stringModelPartMap);
            }

            return new ArrayList<>(stringModelPartMap.keySet());
        }

        return super.getBones();
    }

    private void bindTexture()
    {
        Link link = this.form.texture.get();

        if (link != null)
        {
            BBSModClient.getTextures().bindTexture(link);
        }
    }

    private void ensureEntity()
    {
        String id = this.form.mobID.get();
        String nbt = this.form.mobNBT.get();
        boolean slim = this.form.slim.get();

        if (!this.lastId.equals(id) || !this.lastNBT.equals(nbt) || slim != this.lastSlim)
        {
            this.lastId = id;
            this.lastNBT = nbt;
            this.lastSlim = slim;
            this.entity = null;
        }

        if (this.entity != null)
        {
            return;
        }

        CompoundTag compound = new CompoundTag();

        try
        {
            compound = (new StringNbtReader(new StringReader(nbt))).parseCompound();
        }
        catch (Exception e)
        {}

        this.entity = BuiltInRegistries.ENTITY_TYPE.get(new Identifier(id)).create(Minecraft.getInstance().world);

        if (this.entity == null && this.form.isPlayer())
        {
            this.entity = new OtherClientPlayerEntity(Minecraft.getInstance().world, slim ? SLIM : WIDE);
            this.entity.getDataTracker().set(PlayerUtils.ProtectedAccess.getModelParts(), (byte) 0b1111111);
        }

        if (this.entity != null)
        {
            compound.putString("id", id);
            this.entity.readNbt(compound);
            this.entity.noClip = true;
        }
    }

    @Override
    protected void renderInUI(UIContext context, int x1, int y1, int x2, int y2)
    {
        this.ensureEntity();

        if (this.entity != null)
        {
            PoseStack stack = context.batcher.getContext().getMatrices();

            stack.push();

            Matrix4f uiMatrix = ModelFormRenderer.getUIMatrix(context, x1, y1, x2, y2);
            CustomVertexConsumer consumers = FormUtilsClient.getProvider();
            float scale = this.form.uiScale.get();
            float width = this.entity.getWidth();
            float height = this.entity.getHeight();

            scale = scale * Math.min(1.8F / Math.max(width, height), 1F);

            this.applyTransforms(uiMatrix, context.getTransition());
            PoseStackUtils.multiply(stack, uiMatrix);
            stack.scale(scale, scale, scale);

            if (!this.form.mobID.get().equals("minecraft:ender_dragon"))
            {
                stack.multiply(RotationAxis.POSITIVE_Y.rotation(MathUtils.PI));
            }

            stack.peek().getNormalMatrix().getScale(Vectors.EMPTY_3F);
            stack.peek().getNormalMatrix().scale(1F / Vectors.EMPTY_3F.x, -1F / Vectors.EMPTY_3F.y, 1F / Vectors.EMPTY_3F.z);

            BooleanHolder first = new BooleanHolder();

            CustomVertexConsumer.hijackVertexFormat((layer) ->
            {
                if (!first.bool)
                {
                    this.bindTexture();

                    first.bool = true;
                }
            });

            consumers.setUI(true);
            Minecraft.getInstance().getEntityRenderDispatcher().render(this.entity, 0D, 0D, 0D, 0F, context.getTransition(), stack, consumers, LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE);
            consumers.draw();
            consumers.setUI(false);

            CustomVertexConsumer.clearRunnables();

            stack.pop();

            RenderSystem.depthFunc(GL11.GL_ALWAYS);
        }
    }

    @Override
    protected void render3D(FormRenderingContext context)
    {
        this.ensureEntity();

        if (this.entity != null)
        {
            CustomVertexConsumer consumers = FormUtilsClient.getProvider();
            int light = context.light;
            BooleanHolder first = new BooleanHolder();

            if (context.isPicking())
            {
                CustomVertexConsumer.hijackVertexFormat((layer) ->
                {
                    if (!first.bool)
                    {
                        this.bindTexture();
                        this.setupTarget(context, BBSShaders.getPickerModelsProgram());
                        RenderSystem.setShader(BBSShaders::getPickerModelsProgram);

                        first.bool = true;
                    }
                });

                light = 0;
            }
            else
            {
                CustomVertexConsumer.hijackVertexFormat((layer) ->
                {
                    if (!first.bool)
                    {
                        this.bindTexture();

                        first.bool = true;
                    }
                });
            }

            context.stack.push();

            if (this.form.mobID.get().equals("minecraft:ender_dragon"))
            {
                context.stack.multiply(RotationAxis.POSITIVE_Y.rotation(MathUtils.PI));
            }

            if (this.entity instanceof LivingEntity entity)
            {
                int u = context.overlay & '\uffff';
                int v = context.overlay >> 16 & '\uffff';

                entity.hurtTime = v != 10 ? 100 : 0;
            }

            currentPose = this.form.pose.get();
            currentPoseOverlay = this.form.poseOverlay.get();

            Minecraft.getInstance().getEntityRenderDispatcher().render(this.entity, 0D, 0D, 0D, 0F, context.getTransition(), context.stack, consumers, light);

            currentPose = currentPoseOverlay = null;

            consumers.draw();
            CustomVertexConsumer.clearRunnables();

            context.stack.pop();

            RenderSystem.enableDepthTest();
        }
    }

    @Override
    public void tick(IEntity entity)
    {
        this.ensureEntity();

        if (this.entity != null)
        {
            this.entity.tick();

            this.entity.prevPitch = this.prevPitch;
            this.entity.prevYaw = 0F;

            if (this.entity instanceof LivingEntity livingEntity)
            {
                livingEntity.prevHeadYaw = this.prevYawHead;
                livingEntity.prevBodyYaw = 0F;

                /* Limb swing is so ugly */
                if (livingEntity.limbAnimator instanceof LimbAnimatorAccessor a && entity.getLimbAnimator() instanceof LimbAnimatorAccessor b)
                {
                    a.setPrevSpeed(b.getPrevSpeed());
                    a.setSpeed(b.getSpeed());
                    a.setPos(b.getPos());
                }

                /* Arm swing */
                float handSwingProgress = entity.getInteractionHandSwingProgress(0F);

                if (handSwingProgress < this.prevInteractionHandSwing)
                {
                    this.prevInteractionHandSwing = 0;
                }

                if (handSwingProgress > 0 && this.prevInteractionHandSwing == 0)
                {
                    livingEntity.swingInteractionHand(InteractionHand.MAIN_HAND);
                }

                this.prevInteractionHandSwing = handSwingProgress;
            }

            this.entity.setYaw(0F);
            this.entity.setHeadYaw(entity.getHeadYaw() - entity.getBodyYaw());
            this.entity.setPitch(entity.getPitch());
            this.entity.setBodyYaw(0F);

            this.entity.setPos(entity.getX(), entity.getY(), entity.getZ());
            this.entity.setOnGround(entity.isOnGround());
            this.entity.setSneaking(entity.isSneaking());
            this.entity.setSprinting(entity.isSprinting());
            this.entity.setPose(entity.isSneaking() ? EntityPose.CROUCHING : EntityPose.STANDING);
            this.entity.equipStack(EquipmentSlot.MAINHAND, entity.getEquipmentStack(EquipmentSlot.MAINHAND));
            this.entity.equipStack(EquipmentSlot.OFFHAND, entity.getEquipmentStack(EquipmentSlot.OFFHAND));
            this.entity.equipStack(EquipmentSlot.HEAD, entity.getEquipmentStack(EquipmentSlot.HEAD));
            this.entity.equipStack(EquipmentSlot.CHEST, entity.getEquipmentStack(EquipmentSlot.CHEST));
            this.entity.equipStack(EquipmentSlot.LEGS, entity.getEquipmentStack(EquipmentSlot.LEGS));
            this.entity.equipStack(EquipmentSlot.FEET, entity.getEquipmentStack(EquipmentSlot.FEET));
            this.entity.age = entity.getAge();
            this.entity.noClip = true;

            this.prevYawHead = entity.getHeadYaw() - entity.getBodyYaw();
            this.prevPitch = entity.getPitch();
        }
    }

    private static class BooleanHolder
    {
        public boolean bool;
    }
}


