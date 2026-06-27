package mchorse.bbs_mod.mixin;

import net.minecraft.world.entity.WalkAnimationState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WalkAnimationState.class)
public interface LimbAnimatorAccessor
{
    @Accessor
    public float getPrevSpeed();

    @Accessor
    public void setPrevSpeed(float v);

    @Accessor
    public float getSpeed();

    @Accessor
    public void setSpeed(float v);

    @Accessor
    public float getPos();

    @Accessor
    public void setPos(float v);
}