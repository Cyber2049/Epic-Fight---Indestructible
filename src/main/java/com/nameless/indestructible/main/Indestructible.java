package com.nameless.indestructible.main;

import com.nameless.indestructible.gameasset.GuardAnimations;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Indestructible.MOD_ID)
public class Indestructible {
    public static final String MOD_ID = "indestructible";
    public Indestructible(){
        MinecraftForge.EVENT_BUS.register(this);
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(GuardAnimations::registerAnimations);
    }
}
