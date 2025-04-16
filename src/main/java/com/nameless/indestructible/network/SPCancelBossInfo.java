package com.nameless.indestructible.network;

import com.nameless.indestructible.client.gui.BossBarGUi;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SPCancelBossInfo {
    protected UUID id;
    public SPCancelBossInfo(UUID id) {
        this.id = id;
    }
    public static SPCancelBossInfo fromBytes(FriendlyByteBuf buf) {
        long mostSignificant = buf.readLong();
        long leastSignificant = buf.readLong();
        UUID uuid = new UUID(mostSignificant, leastSignificant);

        return new SPCancelBossInfo(uuid);
    }
    public static void toBytes(SPCancelBossInfo msg, FriendlyByteBuf buf) {
        buf.writeLong(msg.id.getMostSignificantBits());
        buf.writeLong(msg.id.getLeastSignificantBits());
    }
    public static void handle(SPCancelBossInfo msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> BossBarGUi.BossBarEntities.remove(msg.id));
        ctx.get().setPacketHandled(true);
    }
}
