package com.nameless.indestructible.main;


import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.nameless.indestructible.client.UIConfig;
import com.nameless.indestructible.client.gui.BossBarGUi;
import com.nameless.indestructible.client.gui.StatusIndicator;
import com.nameless.indestructible.command.AHPatchPlayAnimationCommand;
import com.nameless.indestructible.command.AHPatchSetLookAtCommand;
import com.nameless.indestructible.command.AHPatchSetPhaseCommand;
import com.nameless.indestructible.data.AdvancedMobpatchReloader;
import com.nameless.indestructible.data.ExtraConditions;
import com.nameless.indestructible.gameasset.GuardAnimations;
import com.nameless.indestructible.network.SPCancelBossInfo;
import com.nameless.indestructible.network.SPDatapackSync;
import com.nameless.indestructible.server.CommonConfig;
import com.nameless.indestructible.world.capability.Utils.IBossEventCapability;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.StaticAnimationProvider;
import yesman.epicfight.client.gui.EntityIndicator;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;

import static com.nameless.indestructible.client.gui.BossBarGUi.cancelBossBar;

@Mod(Indestructible.MOD_ID)
public class Indestructible {
    public static final String MOD_ID = "indestructible";
    public static final Logger LOGGER = LogManager.getLogger(Indestructible.MOD_ID);
    public static StaticAnimationProvider[] NEUTRALIZE_ANIMATIONS;
    public static final ResourceLocation BOSS_BAR = new ResourceLocation(Indestructible.MOD_ID, "textures/gui/boss_bar.png");
    public Indestructible(){
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, UIConfig.SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
        ExtraConditions.CONDITIONS.register(bus);
        bus.addListener(GuardAnimations::registerAnimations);
        bus.addListener(this::doCommonStuff);
        bus.addListener(this::doClientStuff);
        MinecraftForge.EVENT_BUS.addListener(this::reloadListnerEvent);
        MinecraftForge.EVENT_BUS.addListener(this::onDatapackSync);
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
        MinecraftForge.EVENT_BUS.addListener(this::stopTrackingEvent);
    }

    private void doCommonStuff(final FMLCommonSetupEvent event) {
        EpicFightNetworkManager.INSTANCE.registerMessage(99, SPDatapackSync.class, SPDatapackSync::toBytes, SPDatapackSync::fromBytes, SPDatapackSync::handle);
        EpicFightNetworkManager.INSTANCE.registerMessage(98, SPCancelBossInfo.class, SPCancelBossInfo::toBytes, SPCancelBossInfo::fromBytes, SPCancelBossInfo::handle);
    }
    private void doClientStuff(final FMLClientSetupEvent event){
        EntityIndicator.ENTITY_INDICATOR_RENDERERS.add(new StatusIndicator());
        MinecraftForge.EVENT_BUS.register(new BossBarGUi());
        cancelBossBar.addAll(UIConfig.BOSS_NAME.get());
    }

    private void reloadListnerEvent(final AddReloadListenerEvent event) {
        event.addListener(new AdvancedMobpatchReloader());
        StaticAnimationProvider[] providers = new StaticAnimationProvider[CommonConfig.NEUTRALIZE_ANIMATION.get().size()];
        for (int i = 0; i < CommonConfig.NEUTRALIZE_ANIMATION.get().size(); i++) {
            ResourceLocation rl = new ResourceLocation(CommonConfig.NEUTRALIZE_ANIMATION.get().get(i));
            providers[i] = ()-> AnimationManager.getInstance().byKeyOrThrow(rl);
        }
        NEUTRALIZE_ANIMATIONS = providers;
    }

    private void onDatapackSync(final OnDatapackSyncEvent event) {
        ServerPlayer player = event.getPlayer();
        if(player != null){
            if (!player.getServer().isSingleplayerOwner(player.getGameProfile())) {
                SPDatapackSync mobPatchPacket = new SPDatapackSync(AdvancedMobpatchReloader.getTagCount());
                AdvancedMobpatchReloader.getDataStream().forEach(mobPatchPacket::write);
                EpicFightNetworkManager.sendToPlayer(mobPatchPacket, player);
            }
        } else {
            event.getPlayerList().getPlayers().forEach((serverPlayer -> {
                SPDatapackSync mobPatchPacket = new SPDatapackSync(AdvancedMobpatchReloader.getTagCount());
                AdvancedMobpatchReloader.getDataStream().forEach(mobPatchPacket::write);
                EpicFightNetworkManager.sendToPlayer(mobPatchPacket, serverPlayer);
            }));
        }

}
    private void registerCommands(final RegisterCommandsEvent event){
        event.getDispatcher().register(
                LiteralArgumentBuilder.<CommandSourceStack>literal(Indestructible.MOD_ID)
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("living_entity", EntityArgument.entity())
                                .then(AHPatchSetPhaseCommand.register())
                                .then(AHPatchSetLookAtCommand.register())
                                .then(AHPatchPlayAnimationCommand.register()))
        );
    }

    private void stopTrackingEvent(PlayerEvent.StopTracking event) {
        Entity trackingTarget = event.getTarget();
        MobPatch<?> mobPatch = EpicFightCapabilities.getEntityPatch(trackingTarget, MobPatch.class);

        if (mobPatch instanceof IBossEventCapability ibc) {
            ibc.onStopTracking(event.getEntity());
        }
    }
}
