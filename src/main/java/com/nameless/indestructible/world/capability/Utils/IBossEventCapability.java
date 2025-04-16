package com.nameless.indestructible.world.capability.Utils;

import com.nameless.indestructible.client.ClientBossInfo;
import com.nameless.indestructible.server.AdvancedBossInfo;
import io.netty.buffer.ByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public interface IBossEventCapability {
    boolean hasBossBar();
    void setupServerBossInfo(AdvancedBossInfo info);
    AdvancedBossInfo getServerBossInfo();
    void setupClientBossInfo(ClientBossInfo info);
    ClientBossInfo getClientBossInfo();
    void onStartTracking(ServerPlayer trackingPlayer);
    void onStopTracking(Player trackingPlayer);
    void processSpawnData(ByteBuf buf);
}
