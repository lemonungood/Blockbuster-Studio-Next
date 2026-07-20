package mchorse.bbs_mod.mixin.client;

// [MC26.2] 
import mchorse.bbs_mod.forms.CustomVertexConsumer;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderTypes.class)
public class RenderTypesMixin
{
    @Inject(method = "draw", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderTypes;startDrawing()V", ordinal = 0, shift = At.Shift.AFTER))
    public void onDraw(BufferBuilder buffer, VertexSorter sorter, CallbackInfo info)
    {
        CustomVertexConsumer.drawLayer((RenderTypes) (Object) this);
    }
}

