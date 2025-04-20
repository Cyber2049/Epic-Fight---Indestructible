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
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class AdvancedCustomHumanoidMobPatchProviderBuilder {
    protected final AdvancedMobpatchReloader.AdvancedCustomHumanoidMobPatchProvider provider = new AdvancedMobpatchReloader.AdvancedCustomHumanoidMobPatchProvider();

    public static AdvancedCustomHumanoidMobPatchProviderBuilder builder() {
        return new AdvancedCustomHumanoidMobPatchProviderBuilder();
    }

    public AdvancedCustomHumanoidMobPatchProviderBuilder setFaction(Faction faction) {
        provider.faction = faction;
        return this;
    }

    public AdvancedCustomHumanoidMobPatchProviderBuilder setCombatBehaviorsBuilder(Map<WeaponCategory, Map<Style, CombatBehaviors.Builder<HumanoidMobPatch<?>>>> behaviors) {
        provider.AHCombatBehaviors = behaviors;
        return this;
    }

    public AdvancedCustomHumanoidMobPatchProviderBuilder setHumanoidWeaponMotions(Map<WeaponCategory, Map<Style, Set<Pair<LivingMotion, StaticAnimation>>>> motions) {
        provider.AHWeaponMotions = motions;
        return this;
    }

    public AdvancedCustomHumanoidMobPatchProviderBuilder setHumanoidGuardMotion(Map<WeaponCategory, Map<Style, GuardMotion>> guardMotions) {
        provider.guardMotions = guardMotions;
        return this;
    }

    public AdvancedCustomHumanoidMobPatchProviderBuilder setRegenStaminaStandbyTime(int tick) {
        provider.regenStaminaStandbyTime = tick;
        return this;
    }

    public AdvancedCustomHumanoidMobPatchProviderBuilder setStunReduction(boolean reduce) {
        provider.hasStunReduction = reduce;
        return this;
    }

    public AdvancedCustomHumanoidMobPatchProviderBuilder setMaxStunShield(float maxStunShield) {
        provider.maxStunShield = maxStunShield;
        return this;
    }

    public AdvancedCustomHumanoidMobPatchProviderBuilder setReganShieldStandByTime(int reganShieldStandbyTime) {
        provider.reganShieldStandbyTime = reganShieldStandbyTime;
        return this;
    }

    public AdvancedCustomHumanoidMobPatchProviderBuilder setReganShieldMultiply(float reganShieldMultiply) {
        provider.reganShieldMultiply = reganShieldMultiply;
        return this;
    }

    public AdvancedCustomHumanoidMobPatchProviderBuilder setStaminaLoseMultiply(float staminaLoseMultiply) {
        provider.staminaLoseMultiply = staminaLoseMultiply;
        return this;
    }

    public AdvancedCustomHumanoidMobPatchProviderBuilder setGuardRadius(float guardRadius) {
        provider.guardRadius = guardRadius;
        return this;
    }

    public AdvancedCustomHumanoidMobPatchProviderBuilder setAttackRadius(float attackRadius) {
        provider.attackRadius = attackRadius;
        return this;
    }

    public AdvancedCustomHumanoidMobPatchProviderBuilder setDefaultAnimation(List<Pair<LivingMotion, StaticAnimation>> list) {
        provider.defaultAnimations = list;
        return this;
    }

    public AdvancedCustomHumanoidMobPatchProviderBuilder setStunAnimationMap(Map<StunType, StaticAnimation> stunAnimations) {
        provider.stunAnimations = stunAnimations;
        return this;
    }

    public AdvancedCustomHumanoidMobPatchProviderBuilder setAttributeMap(Map<Attribute, Double> attributeValues) {
        provider.attributeValues = attributeValues;
        return this;
    }

    public AdvancedCustomHumanoidMobPatchProviderBuilder setChasingSpeed(double speed) {
        provider.chasingSpeed = speed;
        return this;
    }

    public AdvancedCustomHumanoidMobPatchProviderBuilder setScale(float scale) {
        provider.scale = scale;
        return this;
    }

    public AdvancedCustomHumanoidMobPatchProviderBuilder hasBossBar(boolean hasBossBar) {
        provider.hasBossBar = hasBossBar;
        return this;
    }

    public AdvancedCustomHumanoidMobPatchProviderBuilder setBossBarResourceLocation(ResourceLocation rl) {
        provider.bossBar = rl;
        return this;
    }

    public AdvancedCustomHumanoidMobPatchProviderBuilder setStunEvent(List<LivingEntityPatchEvent.StunEvent> stunEvent) {
        provider.stunEvent = stunEvent;
        return this;
    }

    public MobPatchReloadListener.AbstractMobPatchProvider build() {
        return this.provider;
    }
}