package com.nameless.indestructible.server;

import com.nameless.indestructible.world.capability.AdvancedCustomHumanoidMobPatch;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class AdvancedBossInfo extends ServerBossEvent {
    private final AdvancedCustomHumanoidMobPatch<?> ACHPatch;

    private final Set<ServerPlayer> unseen = new HashSet<>();

    public AdvancedBossInfo(AdvancedCustomHumanoidMobPatch<?> achpatch) {
        super(new TextComponent("advanced epic fight boss"), BossBarColor.WHITE, BossBarOverlay.PROGRESS);
        this.setVisible(true);
        this.ACHPatch = achpatch;
    }

    public void update() {
        LivingEntity entity = this.ACHPatch.getOriginal();
        Iterator<ServerPlayer> it = this.unseen.iterator();
        while (it.hasNext()) {
            ServerPlayer player = it.next();
            if (entity.hasLineOfSight(player)) {
                super.addPlayer(player);
                it.remove();
            }
        }
    }

    @Override
    public void addPlayer(ServerPlayer player) {
        if (this.ACHPatch.getOriginal().hasLineOfSight(player)) {
            super.addPlayer(player);
        } else {
            this.unseen.add(player);
        }
    }

    @Override
    public void removePlayer(ServerPlayer player) {
        super.removePlayer(player);
        this.unseen.remove(player);
    }
}
