package mchorse.bbs_mod.mixin.client.sodium;

// Sodium API changed
// Sodium API changed
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SodiumBufferBuilder.class)
public interface SodiumBufferBuilderAccessor
{
    @Accessor(value = "builder", remap = false)
    public ExtendedBufferBuilder bbs$getBuilder();
}
