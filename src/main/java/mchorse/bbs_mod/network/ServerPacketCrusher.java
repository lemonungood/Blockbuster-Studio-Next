package mchorse.bbs_mod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class ServerPacketCrusher extends PacketCrusher
{
    @Override
    protected void sendBuffer(Player entity, Identifier identifier, FriendlyByteBuf buf)
    {
        ServerPlayer player = (ServerPlayer) entity;
        player.connection.send(new ClientboundCustomPayloadPacket(new ServerNetwork.GenericPayload(identifier, buf)));
    }
}