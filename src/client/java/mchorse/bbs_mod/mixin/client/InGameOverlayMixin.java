package mchorse.bbs_mod.mixin.client;

import mchorse.bbs_mod.BBSModClient;
import mchorse.bbs_mod.camera.controller.ICameraController;
import mchorse.bbs_mod.camera.controller.PlayCameraController;
import mchorse.bbs_mod.client.BBSRendering;
import net.minecraft.client.gui.GuiGraphicsExtractor;
// [MC 26.2 REMOVED] import net.minecraft.client.gui.hud.InGameOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameOverlay.class)
public class InGameOverlayMixin
{
    @Inject(method = "render", at = @At(value = "HEAD"), cancellable = true)
    public void render(GuiGraphicsExtractor drawContext, float tickDelta, CallbackInfo info)
    {
        ICameraController current = BBSModClient.getCameraController().getCurrent();

        if (current instanceof PlayCameraController)
        {
            BBSRendering.onRenderBeforeScreen();

            info.cancel();
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void onRenderEnd(CallbackInfo info)
    {
        BBSRendering.onRenderBeforeScreen();
    }
}


