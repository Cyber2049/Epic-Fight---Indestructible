package com.nameless.indestructible.data;

import com.nameless.indestructible.data.conditions.CustomPhase;
import com.nameless.indestructible.data.conditions.TargetIsGuardBreak;
import com.nameless.indestructible.data.conditions.TargetWithinState;
import com.nameless.indestructible.main.Indestructible;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.main.EpicFightMod;

import java.util.function.Supplier;

public class ExtraConditions {
    public static final DeferredRegister<Supplier<Condition<?>>> CONDITIONS = DeferredRegister.create(new ResourceLocation(EpicFightMod.MODID, "conditions"), Indestructible.MOD_ID);
    private static final RegistryObject<Supplier<Condition<?>>> TargetGuardBreak = CONDITIONS.register(new ResourceLocation(Indestructible.MOD_ID, "guard_break").getPath(), () -> TargetIsGuardBreak::new);
    private static final RegistryObject<Supplier<Condition<?>>> TargetIsKnockDown = CONDITIONS.register(new ResourceLocation(Indestructible.MOD_ID, "knock_down").getPath(), () -> com.nameless.indestructible.data.conditions.TargetIsKnockDown::new);
    private static final RegistryObject<Supplier<Condition<?>>> SelfStamina = CONDITIONS.register(new ResourceLocation(Indestructible.MOD_ID, "stamina").getPath(), () -> com.nameless.indestructible.data.conditions.SelfStamina::new);
    private static final RegistryObject<Supplier<Condition<?>>> TargetIsUsingItem = CONDITIONS.register(new ResourceLocation(Indestructible.MOD_ID, "using_item").getPath(), () -> com.nameless.indestructible.data.conditions.TargetIsUsingItem::new);
    private static final RegistryObject<Supplier<Condition<?>>> AttackPhase = CONDITIONS.register(new ResourceLocation(Indestructible.MOD_ID, "attack_level").getPath(), () -> TargetWithinState::new);
    private static final RegistryObject<Supplier<Condition<?>>> CustomPhase = CONDITIONS.register(new ResourceLocation(Indestructible.MOD_ID, "phase").getPath(), () -> CustomPhase::new);
}
