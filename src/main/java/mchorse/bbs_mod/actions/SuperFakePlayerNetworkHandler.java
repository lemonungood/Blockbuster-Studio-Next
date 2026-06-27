package mchorse.bbs_mod.actions;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.dimension.DimensionType;

public class SuperFakePlayerNetworkHandler extends ServerGamePacketListenerImpl
{
    private static final Connection FAKE_CONNECTION = new Connection(PacketFlow.CLIENTBOUND);

    public SuperFakePlayerNetworkHandler(ServerPlayer player)
    {
        MinecraftServer server = ((ServerLevel)player.level()).getServer();
        super(server, FAKE_CONNECTION, player, 
            new CommonListenerCookie(player.getGameProfile(), 0, net.minecraft.server.level.ClientInformation.createDefault(), false));
    }

    @Override
    public void send(Packet<?> packet) {}
}
