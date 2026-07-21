package mchorse.bbs_mod.client.renderer.entity;

import mchorse.bbs_mod.cubic.render.vanilla.ArmorRenderer;
import mchorse.bbs_mod.entity.ActorEntity;
import mchorse.bbs_mod.forms.FormUtilsClient;
import mchorse.bbs_mod.forms.renderers.FormRenderType;
import mchorse.bbs_mod.forms.renderers.FormRenderingContext;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Pose;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class ActorEntityRenderer extends EntityRenderer<ActorEntity, EntityRenderState>
{
    public static ArmorRenderer armorRenderer;
    private ActorEntity currentEntity;

    public ActorEntityRenderer(EntityRendererProvider.Context ctx)
    {
        super(ctx);

        armorRenderer = new ArmorRenderer(Minecraft.getInstance().getModelManager(), false);

        this.shadowRadius = 0.5F;
    }

    @Override
    public EntityRenderState createRenderState()
    {
        return new EntityRenderState();
    }

    @Override
    public void extractRenderState(ActorEntity entity, EntityRenderState state, float tickDelta)
    {
        super.extractRenderState(entity, state, tickDelta);
        this.currentEntity = entity;
    }

    @Override
    public void submit(EntityRenderState state, PoseStack matrices, SubmitNodeCollector collector, CameraRenderState camera)
    {
        ActorEntity livingEntity = this.currentEntity;

        if (livingEntity == null)
        {
            super.submit(state, matrices, collector, camera);
            return;
        }

        matrices.pushPose();

        float bodyYaw = Mth.rotLerp(state.ageInTicks, livingEntity.yBodyRotO, livingEntity.yBodyRot);
        int overlay = livingEntity.hurtTime > 0 ? 0 : 0;

        this.setupTransforms(livingEntity, matrices, bodyYaw, state.ageInTicks);

        // enableBlend/disableBlend removed in MC 26.2
        // enableDepthTest removed;
        FormUtilsClient.render(livingEntity.getForm(), new FormRenderingContext()
            .set(FormRenderType.ENTITY, livingEntity.getEntity(), matrices, state.lightCoords, overlay, state.ageInTicks)
            .camera(Minecraft.getInstance().gameRenderer.mainCamera()));
        // disableDepthTest removed;

        matrices.popPose();

        super.submit(state, matrices, collector, camera);

        this.currentEntity = null;
    }

    protected boolean isVisible(ActorEntity entity)
    {
        return !entity.isInvisible();
    }

    protected void setupTransforms(ActorEntity entity, PoseStack matrices, float bodyYaw, float tickDelta)
    {
        if (!entity.hasPose(Pose.SLEEPING))
        {
            matrices.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-bodyYaw));
        }

        if (entity.deathTime > 0)
        {
            float deathAngle = (entity.deathTime + tickDelta - 1F) / 20F * 1.6F;

            matrices.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(Math.min(Mth.sqrt(deathAngle), 1F) * 90F));
        }
    }
}
