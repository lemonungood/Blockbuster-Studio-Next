package mchorse.bbs_mod.actions.types.blocks;

import mchorse.bbs_mod.actions.SuperFakePlayer;
import mchorse.bbs_mod.actions.types.ActionClip;
import mchorse.bbs_mod.actions.values.ValueBlockHitResult;
import mchorse.bbs_mod.film.Film;
import mchorse.bbs_mod.film.replays.Replay;
import mchorse.bbs_mod.settings.values.numeric.ValueBoolean;
import mchorse.bbs_mod.utils.clips.Clip;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;

public class InteractBlockActionClip extends ActionClip
{
    public final ValueBlockHitResult hit = new ValueBlockHitResult("hit");
    public final ValueBoolean hand = new ValueBoolean("hand", true);

    public InteractBlockActionClip()
    {
        super();

        this.add(this.hit);
        this.add(this.hand);
    }

    @Override
    public void shift(double dx, double dy, double dz)
    {
        super.shift(dx, dy, dz);

        this.hit.shift(dx, dy, dz);
    }

    @Override
    public void applyAction(LivingEntity actor, SuperFakePlayer player, Film film, Replay replay, int tick)
    {
        this.applyPositionRotation(player, replay, tick);

        BlockHitResult result = this.hit.getHitResult();
        InteractionHand hand = this.hand.get() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;

        // 以玩家手持物 right-click 目标方块：完整交互管线（item on block -> block without item -> item use），
        // 与真实玩家右键一致；签名同 ServerPlayNetworkHandlerMixin 中录制时使用的 ServerPlayerGameMode.useItemOn。
        player.gameMode.useItemOn(player, player.level(), player.getItemInHand(hand), hand, result);
    }

    @Override
    protected Clip create()
    {
        return new InteractBlockActionClip();
    }
}