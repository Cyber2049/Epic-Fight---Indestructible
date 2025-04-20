package com.nameless.indestructible.data;


import com.mojang.datafixers.util.Pair;
import com.nameless.indestructible.api.animation.types.LivingEntityPatchEvent;
import com.nameless.indestructible.compat.kubejs.JsCustomHumanoidMobPatch;
import com.nameless.indestructible.world.ai.CombatBehaviors.GuardMotion;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.data.reloader.MobPatchReloadListener;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.Faction;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

import java.util.*;

public class JSCustomHumanoidMobPatchProviderBuilder {
    protected final JSCustomHumanoidMobPatchProvider provider = new JSCustomHumanoidMobPatchProvider();
    private final CompoundTag clientTag = new CompoundTag();
    public CompoundTag buildClientTag(){
        this.clientTag.putBoolean("isHumanoid", true);
        return this.clientTag;
    }
    public static JSCustomHumanoidMobPatchProviderBuilder builder() {
        return new JSCustomHumanoidMobPatchProviderBuilder();
    }

    public JSCustomHumanoidMobPatchProviderBuilder setModel(String model){
        this.clientTag.putString("model", model);
        return this;
    }
    public JSCustomHumanoidMobPatchProviderBuilder setArmature(String armature){
        this.clientTag.putString("armature", armature);
        return this;
    }
    public JSCustomHumanoidMobPatchProviderBuilder setRenderer(String renderer){
        this.clientTag.putString("renderer", renderer);
        return this;
    }
    public JSCustomHumanoidMobPatchProviderBuilder setFaction(Object faction) {
        if(faction instanceof String string){
            this.provider.faction = Faction.valueOf(string.toUpperCase(Locale.ROOT));
            this.clientTag.putString("faction", string);
        } else if(faction instanceof Faction f) {
            this.provider.faction = f;
            this.clientTag.putString("faction", f.toString());
        } else throw new IllegalArgumentException(faction + " can't be recognized");

       return this;
    }
    public JSCustomHumanoidMobPatchProviderBuilder hasBossBar() {
        provider.hasBossBar = true;
        return this;
    }
    public JSCustomHumanoidMobPatchProviderBuilder setBossBarTexture(String rl) {
        this.clientTag.putString("custom_texture", rl);
        return this;
    }
    public JSCustomHumanoidMobPatchProviderBuilder setBossName(String langkey){
        this.clientTag.putString("custom_name", langkey);
        return this;
    }
    public JSCustomHumanoidMobPatchProviderBuilder setScale(float scale) {
        provider.scale = scale;
        CompoundTag att = new CompoundTag();
        att.putDouble("scale", scale);
        this.clientTag.put("attribute", att);
        return this;
    }
    public JSCustomHumanoidMobPatchProviderBuilder setChasingSpeed(double speed) {
        provider.chasingSpeed = speed;
        return this;
    }
    public JSCustomHumanoidMobPatchProviderBuilder setGuardRadius(float guardRadius) {
        provider.guardRadius = guardRadius;
        return this;
    }
    public JSCustomHumanoidMobPatchProviderBuilder setAttackRadius(float attackRadius) {
        provider.attackRadius = attackRadius;
        return this;
    }
    public JSCustomHumanoidMobPatchProviderBuilder setAttributeMap(Map<Attribute, Double> map) {
        this.provider.attributeValues.putAll(map);
        return this;
    }
    public JSCustomHumanoidMobPatchProviderBuilder setRegenStaminaStandbyTime(int tick) {
        provider.regenStaminaStandbyTime = tick;
        return this;
    }
    public JSCustomHumanoidMobPatchProviderBuilder hasStunReduction(boolean reduce) {
        provider.hasStunReduction = reduce;
        return this;
    }
    public JSCustomHumanoidMobPatchProviderBuilder setMaxStunShield(float maxStunShield) {
        provider.maxStunShield = maxStunShield;
        return this;
    }
    public JSCustomHumanoidMobPatchProviderBuilder setReganShieldMultiply(float reganShieldMultiply) {
        provider.reganShieldMultiply = reganShieldMultiply;
        return this;
    }
    public JSCustomHumanoidMobPatchProviderBuilder setReganShieldStandByTime(int reganShieldStandbyTime) {
        provider.reganShieldStandbyTime = reganShieldStandbyTime;
        return this;
    }
    public JSCustomHumanoidMobPatchProviderBuilder setStaminaLoseMultiply(float staminaLoseMultiply) {
        provider.staminaLoseMultiply = staminaLoseMultiply;
        return this;
    }
    public JSCustomHumanoidMobPatchProviderBuilder setDefaultAnimation(List<Pair<LivingMotion, StaticAnimation>> list) {
        provider.defaultAnimations.addAll(list);
        CompoundTag tag = new CompoundTag();
        list.forEach(l -> tag.putString(l.getFirst().toString(), l.getSecond().getRegistryName().toString()));
        this.clientTag.put("default_livingmotions", tag);
        return this;
    }
    public JSCustomHumanoidMobPatchProviderBuilder setHumanoidWeaponMotion(Map<WeaponCategory, Map<Style, Set<Pair<LivingMotion, StaticAnimation>>>> motions) {
        provider.AHWeaponMotions.putAll(motions);
        return this;
    }
    public JSCustomHumanoidMobPatchProviderBuilder setHumanoidGuardMotion(Map<WeaponCategory, Map<Style, GuardMotion>> guardMotions) {
        provider.guardMotions.putAll(guardMotions);
        return this;
    }
    public JSCustomHumanoidMobPatchProviderBuilder setStunAnimation(Map<StunType, StaticAnimation> stunAnimations) {
        provider.stunAnimations.putAll(stunAnimations);
        return this;
    }
    public JSCustomHumanoidMobPatchProviderBuilder addStunEvents(LivingEntityPatchEvent.StunEvent[] stunEvent) {
        provider.stunEvent.addAll(List.of(stunEvent));
        return this;
    }
    public JSCustomHumanoidMobPatchProviderBuilder addStunEvent(LivingEntityPatchEvent.StunEvent stunEvent) {
        provider.stunEvent.add(stunEvent);
        return this;
    }
    public JSCustomHumanoidMobPatchProviderBuilder setCombatBehavior(Map<WeaponCategory, Map<Style, CombatBehaviors.Builder<HumanoidMobPatch<?>>>> behaviors) {
        provider.AHCombatBehaviors.putAll(behaviors);
        return this;
    }
    public JSCustomHumanoidMobPatchProviderBuilder addCombatBehavior(String[] categories, String style, CombatBehaviors.Builder<HumanoidMobPatch<?>> builder){
        List<WeaponCategory> weaponCategories = new ArrayList<>();
        Arrays.stream(categories).forEach(string -> weaponCategories.add(CapabilityItem.WeaponCategories.valueOf(string.toUpperCase(Locale.ROOT))));
        weaponCategories.forEach(w -> this.provider.AHCombatBehaviors.get(w).put(CapabilityItem.Styles.valueOf(style.toUpperCase(Locale.ROOT)), builder));
        return this;
    }
    public MobPatchReloadListener.AbstractMobPatchProvider build(){
        return this.provider;
    }

    public static class JSCustomHumanoidMobPatchProvider extends AdvancedMobpatchReloader.AdvancedCustomHumanoidMobPatchProvider {
        @SuppressWarnings("rawtypes") @Override
        public EntityPatch<?> get(Entity entity) {
            return new JsCustomHumanoidMobPatch(faction, this);
        }
    }
}
