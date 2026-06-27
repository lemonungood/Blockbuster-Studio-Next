package mchorse.bbs_mod.utils;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class PermissionUtils
{
    public static boolean arePanelsAllowed(MinecraftServer server, ServerPlayer player)
    {
        return server.getPlayerList().isOp(new net.minecraft.server.players.NameAndId(player.getUUID(), player.getScoreboardName()));
    }
}