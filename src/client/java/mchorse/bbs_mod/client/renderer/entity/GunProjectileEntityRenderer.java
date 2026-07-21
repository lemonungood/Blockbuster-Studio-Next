package mchorse.bbs_mod.client.renderer.entity;

import mchorse.bbs_mod.entity.GunProjectileEntity;
import mchorse.bbs_mod.forms.FormUtilsClient;
import mchorse.bbs_mod.forms.renderers.FormRenderType;
import mchorse.bbs_mod.forms.renderers.FormRenderingContext;
import mchorse.bbs_mod.items.GunProperties;
import mchorse.bbs_mod.utils.PoseStackUtils;
import mchorse.bbs_mod.utils.interps.Lerps;
import net.minecraft.client.Minecraft;
// [MC 26.2 REMOVED] import net.minecraft.client.renderer.texture.OverlayTexture;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class GunProjectileEntityRenderer extends EntityRenderer<GunProjectileEntity, EntityRenderState>
{
    public GunProjectileEntityRenderer(EntityRendererProvider.Context ctx)
    {
        super(ctx);
    }

    public Identifier getTexture(GunProjectileEntity entity)
    {
        return Identifier.parse("minecraft:textures/entity/player/wide/steve.png");
    }

    public void render(GunProjectileEntity projectile, float yaw, float tickDelta, PoseStack matrices, VertexConsumer vertexConsumers, int light)
    {
        matrices.pushPose();

        GunProperties properties = projectile.getProperties();
        int out = properties.lifeSpan - 2;

        float bodyYaw = Mth.rotLerp(tickDelta, projectile.yRotO, projectile.getYRot());
        float pitch = Mth.rotLerp(tickDelta, projectile.xRotO, projectile.getXRot());
        float scale = Lerps.envelope(projectile.tickCount + tickDelta, 0, properties.fadeIn, out - properties.fadeOut, out);

        if (properties.yaw) matrices.mulPose(com.mojang.math.Axis.YP.rotationDegrees(bodyYaw));
        if (properties.pitch) matrices.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-pitch));
        matrices.scale(scale, scale, scale);
        PoseStackUtils.applyTransform(matrices, properties.projectileTransform);

        // enableDepthTest removed;
        FormUtilsClient.render(projectile.getForm(), new FormRenderingContext()
            .set(FormRenderType.ENTITY, projectile.getEntity(), matrices, light, 0, tickDelta)
            .camera(Minecraft.getInstance().gameRenderer.mainCamera()));
        // disableDepthTest removed;

        matrices.popPose();
    }

    @Override
    public EntityRenderState createRenderState()
    {
        return new EntityRenderState();
    }
}


