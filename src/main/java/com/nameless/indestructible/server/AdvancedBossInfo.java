package com.nameless.indestructible.server;

import com.nameless.indestructible.network.SPCancelBossInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.network.EpicFightNetworkManager;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class AdvancedBossInfo extends ServerBossEvent {
    private final LivingEntity living;
    private final Set<ServerPlayer> unseen = new HashSet<>();

    public AdvancedBossInfo(LivingEntity living) {
        super(Component.literal("advanced epic fight boss"), BossBarColor.WHITE, BossBarOverlay.PROGRESS);
        this.setVisible(true);
        this.living = living;
    }

    public void update() {
        Iterator<ServerPlayer> it = this.unseen.iterator();
        while (it.hasNext()) {
            ServerPlayer player = it.next();
            if (this.living.hasLineOfSight(player)) {
                super.addPlayer(player);
                it.remove();
            }
        }
    }

    @Override
    public void addPlayer(ServerPlayer player) {
        if (this.living.hasLineOfSight(player)) {
            super.addPlayer(player);
        } else {
            this.unseen.add(player);
        }
    }

    @Override
    public void removePlayer(ServerPlayer player) {
        super.removePlayer(player);
        this.unseen.remove(player);
        SPCancelBossInfo msg = new SPCancelBossInfo(this.getId());
        EpicFightNetworkManager.sendToPlayer(msg, player);
    }
}
