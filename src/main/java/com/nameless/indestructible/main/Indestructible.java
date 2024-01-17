package com.nameless.indestructible.main;


import com.nameless.indestructible.data.AdvancedMobpatchReloader;
import com.nameless.indestructible.gameasset.GuardAnimations;
import com.nameless.indestructible.network.NetworkManager;
import com.nameless.indestructible.network.SPDatapackSync;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Indestructible.MOD_ID)
public class Indestructible {
    public static final String MOD_ID = "indestructible";
    public static final Logger LOGGER = LogManager.getLogger(Indestructible.MOD_ID);
    public Indestructible(){
        MinecraftForge.EVENT_BUS.register(this);
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(GuardAnimations::registerAnimations);
        bus.addListener(this::doCommonStuff);
        MinecraftForge.EVENT_BUS.addListener(this::reloadListnerEvent);
        MinecraftForge.EVENT_BUS.addListener(this::onDatapackSync);
    }

    private void doCommonStuff(final FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkManager::registerPackets);
    }

    private void reloadListnerEvent(final AddReloadListenerEvent event) {
        event.addListener(new AdvancedMobpatchReloader());
    }

    private void onDatapackSync(final OnDatapackSyncEvent event) {
        ServerPlayer player = event.getPlayer();
        PacketDistributor.PacketTarget target = player == null ? PacketDistributor.ALL.noArg() : PacketDistributor.PLAYER.with(() -> player);
        if (player == null || !player.getServer().isSingleplayerOwner(player.getGameProfile())) {
            SPDatapackSync mobPatchPacket = new SPDatapackSync(AdvancedMobpatchReloader.getTagCount());
            AdvancedMobpatchReloader.getDataStream().forEach(mobPatchPacket::write);
            NetworkManager.sendToClient(mobPatchPacket, target);
        }
    }
}
