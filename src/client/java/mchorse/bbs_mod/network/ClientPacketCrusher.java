package mchorse.bbs_mod.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

public class ClientPacketCrusher extends PacketCrusher
{
    @Override
    protected void sendBuffer(Player entity, Identifier identifier, FriendlyByteBuf buf)
    {
        ClientPlayNetworking.send(identifier, buf);
    }
}


