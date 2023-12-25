package com.nameless.indestructible.data;

import com.mojang.datafixers.util.Pair;
import com.nameless.indestructible.world.capability.AdvancedCustomHumanoidMobPatch;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.data.reloader.MobPatchReloadListener;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.Faction;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class AdvancedCustomHumanoidMobPatchProvider extends MobPatchReloadListener.AbstractMobPatchProvider {
    public Map<WeaponCategory, Map<Style, CombatBehaviors.Builder<HumanoidMobPatch<?>>>> humanoidCombatBehaviors;
    public Map<WeaponCategory, Map<Style, Set<Pair<LivingMotion, StaticAnimation>>>> humanoidWeaponMotions;
    public Map<WeaponCategory, Map<Style,Pair<StaticAnimation,Pair<Float, Boolean>>>> guardMotions;
    public int regenStaminaStandbyTime;
    public float regenStaminaMultiply;
    public boolean hasStunReduction;
    public float maxStunShield;
    public int reganShieldStandbyTime;
    public float reganShieldMultiply;
    public float staminaLoseMultiply;
    public List<Pair<LivingMotion, StaticAnimation>> defaultAnimations;
    public Map<StunType, StaticAnimation> stunAnimations;
    public Map<Attribute, Double> attributeValues;
    public Faction faction;
    public double chasingSpeed;
    public float scale;
    public float maxStamina;
    public AdvancedCustomHumanoidMobPatchProvider() {
    }

    @SuppressWarnings("rawtypes")
    public EntityPatch<?> get(Entity entity) {
        return new AdvancedCustomHumanoidMobPatch(this.faction, this);
    }

    public Map<WeaponCategory, Map<Style, Set<Pair<LivingMotion, StaticAnimation>>>> getHumanoidWeaponMotions() {
        return this.humanoidWeaponMotions;
    }

    public Map<WeaponCategory, Map<Style, CombatBehaviors.Builder<HumanoidMobPatch<?>>>> getHumanoidCombatBehaviors() {
        return this.humanoidCombatBehaviors;
    }

    public Map<WeaponCategory, Map<Style,Pair<StaticAnimation,Pair<Float, Boolean>>>> getGuardMotions(){
        return this.guardMotions;
    }
    public List<Pair<LivingMotion, StaticAnimation>> getDefaultAnimations() {
        return this.defaultAnimations;
    }

    public Map<StunType, StaticAnimation> getStunAnimations() {
        return this.stunAnimations;
    }

    public Map<Attribute, Double> getAttributeValues() {
        return this.attributeValues;
    }

    public double getChasingSpeed() {
        return this.chasingSpeed;
    }

    public float getScale() {
        return this.scale;
    }

    //stamina
    public float getMaxStamina(){return this.maxStamina;}
    public int getRegenStaminaStandbyTime(){return this.regenStaminaStandbyTime;}
    public float getRegenStaminaMultiply(){return this.regenStaminaMultiply;}

    //stun
    public boolean hasStunReduction(){return this.hasStunReduction;}
    public float getMaxStunShield(){return this.maxStunShield;}
    public int getReganShieldStandbyTime(){return this.reganShieldStandbyTime;}
    public float getReganShieldMultiply() {return this.reganShieldMultiply;}
    public float getStaminaLoseMultiply(){return this.staminaLoseMultiply;}
}
