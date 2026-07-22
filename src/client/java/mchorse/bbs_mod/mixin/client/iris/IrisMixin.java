package mchorse.bbs_mod.mixin.client.iris;

import mchorse.bbs_mod.utils.iris.QueueMap;
import mchorse.bbs_mod.utils.iris.ShaderCurves;
import net.irisshaders.iris.Iris;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(Iris.class)
public class IrisMixin
{
    @Inject(method = "getShaderPackOptionQueue", at = @At("RETURN"), cancellable = true, remap = false)
    private static void onGetShaderPackOptionQueue(CallbackInfoReturnable<Map<String, String>> info)
    {
        Map<String, String> returnValue = info.getReturnValue() == null ? null : new QueueMap<>(info.getReturnValue());

        info.setReturnValue(returnValue);
    }

    @Inject(method = "loadShaderpack", at = @At("HEAD"), remap = false)
    private static void onLoadShaderpackHead(CallbackInfo ci)
    {
        ShaderCurves.reset();
    }

    @Inject(method = "loadShaderpack", at = @At("RETURN"), remap = false)
    private static void onLoadShaderpackReturn(CallbackInfo ci)
    {
        ShaderCurves.finishLoading();
    }
}
