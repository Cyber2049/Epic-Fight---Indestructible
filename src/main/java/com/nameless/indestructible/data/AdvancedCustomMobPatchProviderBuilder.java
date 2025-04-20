package com.nameless.indestructible.data;

import com.mojang.datafixers.util.Pair;
import com.nameless.indestructible.api.animation.types.LivingEntityPatchEvent;
import com.nameless.indestructible.world.ai.CombatBehaviors.GuardMotion;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.data.reloader.MobPatchReloadListener;
import yesman.epicfight.world.capabilities.entitypatch.Faction;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

import java.util.List;
import java.util.Map;

public class AdvancedCustomMobPatchProviderBuilder {
    protected final AdvancedMobpatchReloader.AdvancedCustomMobPatchProvider provider = new AdvancedMobpatchReloader.AdvancedCustomMobPatchProvider();

    public static AdvancedCustomMobPatchProviderBuilder builder() {
        return new AdvancedCustomMobPatchProviderBuilder();
    }

    public AdvancedCustomMobPatchProviderBuilder setFaction(Faction faction) {
        provider.faction = faction;
        return this;
    }

    public AdvancedCustomMobPatchProviderBuilder setCombatBehaviorsBuilder(CombatBehaviors.Builder<MobPatch<?>> behaviors) {
        provider.combatBehaviorsBuilder = behaviors;
        return this;
    }

    public AdvancedCustomMobPatchProviderBuilder setRegenStaminaStandbyTime(int tick) {
        provider.regenStaminaStandbyTime = tick;
        return this;
    }

    public AdvancedCustomMobPatchProviderBuilder setStunReduction(boolean reduce) {
        provider.hasStunReduction = reduce;
        return this;
    }

    public AdvancedCustomMobPatchProviderBuilder setMaxStunShield(float maxStunShield) {
        provider.maxStunShield = maxStunShield;
        return this;
    }

    public AdvancedCustomMobPatchProviderBuilder setReganShieldStandByTime(int reganShieldStandbyTime) {
        provider.reganShieldStandbyTime = reganShieldStandbyTime;
        return this;
    }

    public AdvancedCustomMobPatchProviderBuilder setReganShieldMultiply(float reganShieldMultiply) {
        provider.reganShieldMultiply = reganShieldMultiply;
        return this;
    }

    public AdvancedCustomMobPatchProviderBuilder setStaminaLoseMultiply(float staminaLoseMultiply) {
        provider.staminaLoseMultiply = staminaLoseMultiply;
        return this;
    }

    public AdvancedCustomMobPatchProviderBuilder setGuardRadius(float guardRadius) {
        provider.guardRadius = guardRadius;
        return this;
    }

    public AdvancedCustomMobPatchProviderBuilder setAttackRadius(float attackRadius) {
        provider.attackRadius = attackRadius;
        return this;
    }

    public AdvancedCustomMobPatchProviderBuilder setDefaultAnimation(List<Pair<LivingMotion, StaticAnimation>> list) {
        provider.defaultAnimations = list;
        return this;
    }

    public AdvancedCustomMobPatchProviderBuilder setStunAnimationMap(Map<StunType, StaticAnimation> stunAnimations) {
        provider.stunAnimations = stunAnimations;
        return this;
    }

    public AdvancedCustomMobPatchProviderBuilder setAttributeMap(Map<Attribute, Double> attributeValues) {
        provider.attributeValues = attributeValues;
        return this;
    }

    public AdvancedCustomMobPatchProviderBuilder setChasingSpeed(double speed) {
        provider.chasingSpeed = speed;
        return this;
    }

    public AdvancedCustomMobPatchProviderBuilder setScale(float scale) {
        provider.scale = scale;
        return this;
    }

    public AdvancedCustomMobPatchProviderBuilder hasBossBar(boolean hasBossBar) {
        provider.hasBossBar = hasBossBar;
        return this;
    }

    public AdvancedCustomMobPatchProviderBuilder setBossBarResourceLocation(ResourceLocation rl) {
        provider.bossBar = rl;
        return this;
    }

    public AdvancedCustomMobPatchProviderBuilder setDefaultGuardMotion(GuardMotion defaultGuardMotion) {
        provider.defaultGuardMotion = defaultGuardMotion;
        return this;
    }

    public AdvancedCustomMobPatchProviderBuilder setStunEvent(List<LivingEntityPatchEvent.StunEvent> stunEvent) {
        provider.stunEvent = stunEvent;
        return this;
    }

    public MobPatchReloadListener.AbstractMobPatchProvider build() {
        return this.provider;
    }
}
