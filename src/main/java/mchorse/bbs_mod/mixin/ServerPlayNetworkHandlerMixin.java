package mchorse.bbs_mod.mixin;

import com.mojang.brigadier.ParseResults;
import mchorse.bbs_mod.BBSMod;
import mchorse.bbs_mod.actions.types.blocks.InteractBlockActionClip;
import mchorse.bbs_mod.actions.types.chat.CommandActionClip;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerPlayNetworkHandlerMixin
{
    @Shadow
    public ServerPlayer player;

    @Inject(method = "parse", at = @At("HEAD"))
    public void onParse(String command, CallbackInfoReturnable<ParseResults<CommandSourceStack>> info)
    {
        BBSMod.getActions().addAction(this.player, () ->
        {
            CommandActionClip clip = new CommandActionClip();

            clip.command.set(command);

            return clip;
        });
    }

    @Redirect(method = "onPlayerInteractBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerInteractionManager;interactBlock(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;"))
    private InteractionResult redirectOnBlockInteract(ServerPlayerGameMode manager, ServerPlayer player, Level world, ItemStack stack, InteractionHand hand, BlockHitResult hitResult)
    {
        BBSMod.getActions().addAction(this.player, () ->
        {
            InteractBlockActionClip clip = new InteractBlockActionClip();

            clip.hit.setHitResult(hitResult);
            clip.hand.set(hand == InteractionHand.MAIN_HAND);

            return clip;
        });

        return manager.useItemOn(player, world, stack, hand, hitResult);
    }
}