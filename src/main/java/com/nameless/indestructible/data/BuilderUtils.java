package com.nameless.indestructible.data;

import com.mojang.datafixers.util.Pair;
import com.nameless.indestructible.api.animation.types.LivingEntityPatchEvent;
import com.nameless.indestructible.world.capability.Utils.BehaviorsUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.world.capabilities.entitypatch.Faction;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class BuilderUtils {
    public static class AdvancedCustomMobPatchProviderBuilder{
        private final AdvancedMobpatchReloader.AdvancedCustomMobPatchProvider provider = new AdvancedMobpatchReloader.AdvancedCustomMobPatchProvider();
        public static BuilderUtils.AdvancedCustomMobPatchProviderBuilder builder() {
            return new BuilderUtils.AdvancedCustomMobPatchProviderBuilder();
        }
        public AdvancedCustomMobPatchProviderBuilder setFaction(Faction faction){
            provider.faction = faction;
            return this;
        }
        public AdvancedCustomMobPatchProviderBuilder setCombatBehaviorsBuilder(CombatBehaviors.Builder<MobPatch<?>> behaviors){
            provider.combatBehaviorsBuilder = behaviors;
            return this;
        }
        public AdvancedCustomMobPatchProviderBuilder setRegenStaminaStandbyTime(int tick){
            provider.regenStaminaStandbyTime = tick;
            return this;
        }
        public AdvancedCustomMobPatchProviderBuilder setStunReduction(boolean reduce){
            provider.hasStunReduction = reduce;
            return this;
        }
        public AdvancedCustomMobPatchProviderBuilder setMaxStunShield(float maxStunShield){
            provider.maxStunShield = maxStunShield;
            return this;
        }
        public AdvancedCustomMobPatchProviderBuilder setReganShieldStandByTime(int reganShieldStandbyTime){
            provider.reganShieldStandbyTime = reganShieldStandbyTime;
            return this;
        }
        public AdvancedCustomMobPatchProviderBuilder setReganShieldMultiply(float reganShieldMultiply){
            provider.reganShieldMultiply = reganShieldMultiply;
            return this;
        }
        public AdvancedCustomMobPatchProviderBuilder setStaminaLoseMultiply(float staminaLoseMultiply){
            provider.staminaLoseMultiply = staminaLoseMultiply;
            return this;
        }
        public AdvancedCustomMobPatchProviderBuilder setGuardRadius(float guardRadius){
            provider.guardRadius = guardRadius;
            return this;
        }
        public AdvancedCustomMobPatchProviderBuilder setAttackRadius(float attackRadius){
            provider.attackRadius = attackRadius;
            return this;
        }
        public AdvancedCustomMobPatchProviderBuilder setDefaultAnimation(List<Pair<LivingMotion, StaticAnimation>> list){
            provider.defaultAnimations = list;
            return this;
        }
        public AdvancedCustomMobPatchProviderBuilder setStunAnimationMap(Map<StunType, StaticAnimation> stunAnimations){
            provider.stunAnimations = stunAnimations;
            return this;
        }
        public AdvancedCustomMobPatchProviderBuilder setAttributeMap(Map<Attribute, Double> attributeValues){
            provider.attributeValues = attributeValues;
            return this;
        }
        public AdvancedCustomMobPatchProviderBuilder setChasingSpeed(double speed){
            provider.chasingSpeed = speed;
            return this;
        }
        public AdvancedCustomMobPatchProviderBuilder setScale(float scale){
            provider.scale = scale;
            return this;
        }
        public AdvancedCustomMobPatchProviderBuilder hasBossBar(boolean hasBossBar){
            provider.hasBossBar = hasBossBar;
            return this;
        }
        public AdvancedCustomMobPatchProviderBuilder setBossBarResourceLocation(ResourceLocation rl){
            provider.bossBar = rl;
            return this;
        }
        public AdvancedCustomMobPatchProviderBuilder setDefaultGuardMotion(BehaviorsUtils.GuardMotion defaultGuardMotion){
            provider.defaultGuardMotion = defaultGuardMotion;
            return this;
        }
        public AdvancedCustomMobPatchProviderBuilder setStunEvent(List<LivingEntityPatchEvent.StunEvent> stunEvent){
            provider.stunEvent = stunEvent;
            return this;
        }
        public AdvancedMobpatchReloader.AdvancedCustomMobPatchProvider build(){
            return this.provider;
        }
    }
    public static class AdvancedCustomHumanoidMobPatchProviderBuilder{
        private final AdvancedMobpatchReloader.AdvancedCustomHumanoidMobPatchProvider provider = new AdvancedMobpatchReloader.AdvancedCustomHumanoidMobPatchProvider();
        public static BuilderUtils.AdvancedCustomHumanoidMobPatchProviderBuilder builder() {
            return new BuilderUtils.AdvancedCustomHumanoidMobPatchProviderBuilder();
        }
        public AdvancedCustomHumanoidMobPatchProviderBuilder setFaction(Faction faction){
            provider.faction = faction;
            return this;
        }
        public AdvancedCustomHumanoidMobPatchProviderBuilder setCombatBehaviorsBuilder(Map<WeaponCategory, Map<Style, CombatBehaviors.Builder<HumanoidMobPatch<?>>>> behaviors){
            provider.AHCombatBehaviors = behaviors;
            return this;
        }
        public AdvancedCustomHumanoidMobPatchProviderBuilder setHumanoidWeaponMotions(Map<WeaponCategory, Map<Style, Set<Pair<LivingMotion, StaticAnimation>>>> motions){
            provider.AHWeaponMotions = motions;
            return this;
        }
        public AdvancedCustomHumanoidMobPatchProviderBuilder setHumanoidGuardMotion(Map<WeaponCategory, Map<Style, BehaviorsUtils.GuardMotion>> guardMotions){
            provider.guardMotions = guardMotions;
            return this;
        }
        public AdvancedCustomHumanoidMobPatchProviderBuilder setRegenStaminaStandbyTime(int tick){
            provider.regenStaminaStandbyTime = tick;
            return this;
        }
        public AdvancedCustomHumanoidMobPatchProviderBuilder setStunReduction(boolean reduce){
            provider.hasStunReduction = reduce;
            return this;
        }
        public AdvancedCustomHumanoidMobPatchProviderBuilder setMaxStunShield(float maxStunShield){
            provider.maxStunShield = maxStunShield;
            return this;
        }
        public AdvancedCustomHumanoidMobPatchProviderBuilder setReganShieldStandByTime(int reganShieldStandbyTime){
            provider.reganShieldStandbyTime = reganShieldStandbyTime;
            return this;
        }
        public AdvancedCustomHumanoidMobPatchProviderBuilder setReganShieldMultiply(float reganShieldMultiply){
            provider.reganShieldMultiply = reganShieldMultiply;
            return this;
        }
        public AdvancedCustomHumanoidMobPatchProviderBuilder setStaminaLoseMultiply(float staminaLoseMultiply){
            provider.staminaLoseMultiply = staminaLoseMultiply;
            return this;
        }
        public AdvancedCustomHumanoidMobPatchProviderBuilder setGuardRadius(float guardRadius){
            provider.guardRadius = guardRadius;
            return this;
        }
        public AdvancedCustomHumanoidMobPatchProviderBuilder setAttackRadius(float attackRadius){
            provider.attackRadius = attackRadius;
            return this;
        }
        public AdvancedCustomHumanoidMobPatchProviderBuilder setDefaultAnimation(List<Pair<LivingMotion, StaticAnimation>> list){
            provider.defaultAnimations = list;
            return this;
        }
        public AdvancedCustomHumanoidMobPatchProviderBuilder setStunAnimationMap(Map<StunType, StaticAnimation> stunAnimations){
            provider.stunAnimations = stunAnimations;
            return this;
        }
        public AdvancedCustomHumanoidMobPatchProviderBuilder setAttributeMap(Map<Attribute, Double> attributeValues){
            provider.attributeValues = attributeValues;
            return this;
        }
        public AdvancedCustomHumanoidMobPatchProviderBuilder setChasingSpeed(double speed){
            provider.chasingSpeed = speed;
            return this;
        }
        public AdvancedCustomHumanoidMobPatchProviderBuilder setScale(float scale){
            provider.scale = scale;
            return this;
        }
        public AdvancedCustomHumanoidMobPatchProviderBuilder hasBossBar(boolean hasBossBar){
            provider.hasBossBar = hasBossBar;
            return this;
        }
        public AdvancedCustomHumanoidMobPatchProviderBuilder setBossBarResourceLocation(ResourceLocation rl){
            provider.bossBar = rl;
            return this;
        }
        public AdvancedCustomHumanoidMobPatchProviderBuilder setStunEvent(List<LivingEntityPatchEvent.StunEvent> stunEvent){
            provider.stunEvent = stunEvent;
            return this;
        }
        public AdvancedMobpatchReloader.AdvancedCustomHumanoidMobPatchProvider build(){
            return this.provider;
        }
    }
}
