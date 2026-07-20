package mchorse.bbs_mod.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mchorse.bbs_mod.client.renderer.MorphRenderer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin
{
    @WrapOperation(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/entity/EntityRenderer;render(Lnet/minecraft/entity/Entity;FFLnet/minecraft/client/util/math/PoseStack;Lnet/minecraft/client/render/VertexConsumer;I)V"
        )
    )
    private <E extends Entity> void wrapRender(
        EntityRenderer renderer, E entity, float yaw, float tickDelta,
        PoseStack matrices, VertexConsumer vcp, int light,
        Operation<Void> original
    ) {
        if (entity instanceof LivingEntity livingEntity)
        {
            float whiteOverlayProgress = 0;

            if (renderer instanceof LivingEntityRendererInvoker invoker)
            {
                whiteOverlayProgress = invoker.bbs$getAnimationCounter(livingEntity, tickDelta);
            }

            int o = LivingEntityRenderer.getOverlay(livingEntity, whiteOverlayProgress);

            if (MorphRenderer.renderLivingEntity(livingEntity, yaw, tickDelta, matrices, vcp, light, o))
            {
                return;
            }
        }

        original.call(renderer, entity, yaw, tickDelta, matrices, vcp, light);
    }
}


