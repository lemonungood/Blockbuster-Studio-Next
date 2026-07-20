package mchorse.bbs_mod.mixin.client;

import mchorse.bbs_mod.BBSModClient;
import mchorse.bbs_mod.BBSSettings;
import mchorse.bbs_mod.client.BBSRendering;
import mchorse.bbs_mod.utils.VideoRecorder;

/**
 * RenderTickCounter mixin - disabled in MC 26.2 because RenderTickCounter was removed.
 * The new system uses DeltaTracker instead.
 */
// @Mixin target net.minecraft.client.RenderTickCounter was removed in MC 26.2
public class RenderTickCounterMixin
{
    /* TODO: Re-implement using DeltaTracker or new tick system when needed.
    @Shadow
    public float tickDelta;

    @Shadow
    public float lastFrameDuration;

    @Shadow
    private long prevTimeMillis;

    private int heldFrames;

    @Inject(method = "beginRenderTick", at = @At("HEAD"), cancellable = true)
    public void onBeginRenderTick(long timeMillis, CallbackInfoReturnable<Integer> info)
    {
        // ... video recording tick control logic
    }
    */
}
