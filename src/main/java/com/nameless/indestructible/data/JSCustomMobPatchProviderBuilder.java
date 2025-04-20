package com.nameless.indestructible.data;


import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.nameless.indestructible.api.animation.types.LivingEntityPatchEvent;
import com.nameless.indestructible.compat.kubejs.JsCustomMobPatch;
import com.nameless.indestructible.main.Indestructible;
import com.nameless.indestructible.world.ai.CombatBehaviors.GuardMotion;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.data.reloader.MobPatchReloadListener;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.Faction;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class JSCustomMobPatchProviderBuilder {
    protected final JSCustomMobPatchProvider provider = new JSCustomMobPatchProvider();
    private final CompoundTag clientTag = new CompoundTag();
    private final CompoundTag livingMotionTag = new CompoundTag();
    protected CompoundTag buildClientTag(){
        this.clientTag.putBoolean("isHumanoid", false);
        this.clientTag.put("default_livingmotions", this.livingMotionTag);
        return this.clientTag;
    }
    protected MobPatchReloadListener.AbstractMobPatchProvider build(){
        return this.provider;
    }
    public static JSCustomMobPatchProviderBuilder builder() {
        return new JSCustomMobPatchProviderBuilder();
    }

    public JSCustomMobPatchProviderBuilder setModel(String model){
        this.clientTag.putString("model", model);
        return this;
    }
    public JSCustomMobPatchProviderBuilder setArmature(String armature){
        this.clientTag.putString("armature", armature);
        return this;
    }
    public JSCustomMobPatchProviderBuilder setRenderer(String renderer){
        this.clientTag.putString("renderer", renderer);
        return this;
    }
    public JSCustomMobPatchProviderBuilder setFaction(Object faction) {
        if(faction instanceof String string){
            this.provider.faction = Faction.valueOf(string.toUpperCase(Locale.ROOT));
            this.clientTag.putString("faction", string);
        } else if(faction instanceof Faction f) {
            this.provider.faction = f;
            this.clientTag.putString("faction", f.toString());
        } else throw new IllegalArgumentException(faction + " can't be recognized");

       return this;
    }
    public JSCustomMobPatchProviderBuilder hasBossBar() {
        provider.hasBossBar = true;
        return this;
    }
    public JSCustomMobPatchProviderBuilder setBossBarTexture(String rl) {
        this.clientTag.putString("custom_texture", rl);
        return this;
    }
    public JSCustomMobPatchProviderBuilder setBossName(String langkey){
        this.clientTag.putString("custom_name", langkey);
        return this;
    }
    public JSCustomMobPatchProviderBuilder setScale(float scale) {
        provider.scale = scale;
        CompoundTag att = new CompoundTag();
        att.putDouble("scale", scale);
        this.clientTag.put("attribute", att);
        return this;
    }
    public JSCustomMobPatchProviderBuilder setChasingSpeed(double speed) {
        provider.chasingSpeed = speed;
        return this;
    }
    public JSCustomMobPatchProviderBuilder setGuardRadius(float guardRadius) {
        provider.guardRadius = guardRadius;
        return this;
    }
    public JSCustomMobPatchProviderBuilder setAttackRadius(float attackRadius) {
        provider.attackRadius = attackRadius;
        return this;
    }

    public JSCustomMobPatchProviderBuilder addAttribute(Object object, Double value){
        if(object instanceof Attribute attribute){
            this.provider.attributeValues.put(attribute, value);
        } else if (object instanceof String string) {
            ResourceLocation rl = new ResourceLocation(string);
            Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(rl);
            if(attribute != null) this.provider.attributeValues.put(attribute, value);
            else Indestructible.LOGGER.info(string + " doesn't exist");
        } else Indestructible.LOGGER.info(object + " can't be recognized");
        return this;
    }
    public JSCustomMobPatchProviderBuilder addAttributesByMap(Map<Attribute, Double> map) {
        this.provider.attributeValues.putAll(map);
        return this;
    }
    public JSCustomMobPatchProviderBuilder setRegenStaminaStandbyTime(int tick) {
        provider.regenStaminaStandbyTime = tick;
        return this;
    }
    public JSCustomMobPatchProviderBuilder hasStunReduction(boolean reduce) {
        provider.hasStunReduction = reduce;
        return this;
    }
    public JSCustomMobPatchProviderBuilder setMaxStunShield(float maxStunShield) {
        provider.maxStunShield = maxStunShield;
        return this;
    }
    public JSCustomMobPatchProviderBuilder setReganShieldMultiply(float reganShieldMultiply) {
        provider.reganShieldMultiply = reganShieldMultiply;
        return this;
    }
    public JSCustomMobPatchProviderBuilder setReganShieldStandByTime(int reganShieldStandbyTime) {
        provider.reganShieldStandbyTime = reganShieldStandbyTime;
        return this;
    }
    public JSCustomMobPatchProviderBuilder setStaminaLoseMultiply(float staminaLoseMultiply) {
        provider.staminaLoseMultiply = staminaLoseMultiply;
        return this;
    }
    public JSCustomMobPatchProviderBuilder addLivingAnimation(Object object1, Object object2) {
        LivingMotion livingMotion = null;
        StaticAnimation animation = null;

        if(object1 instanceof String s){
            livingMotion = LivingMotions.valueOf(s.toUpperCase(Locale.ROOT));
        } else if(object1 instanceof LivingMotions l){
            livingMotion = l;
        } else Indestructible.LOGGER.info(object1 + " can't be recognized");

        if(object2 instanceof String s){
            animation = AnimationManager.getInstance().byKeyOrThrow(s);
        } else if (object2 instanceof StaticAnimation a){
            animation = a;
        } else Indestructible.LOGGER.info(object2 + " can't be recognized");

        if(livingMotion != null && animation != null) {
            this.provider.defaultAnimations.add(Pair.of(livingMotion, animation));
            this.livingMotionTag.putString(livingMotion.toString(), animation.getRegistryName().toString());
        }
        return this;
    }
    public JSCustomMobPatchProviderBuilder addLivingAnimationByList(List<Pair<LivingMotion, StaticAnimation>> list) {
        provider.defaultAnimations.addAll(list);
        list.forEach(l -> this.livingMotionTag.putString(l.getFirst().toString(), l.getSecond().getRegistryName().toString()));
        return this;
    }
    public JSCustomMobPatchProviderBuilder initLivingAnimationByDefaultPresent(){
        List<Pair<LivingMotion, StaticAnimation>> list = new ArrayList<>();
        list.add(Pair.of(LivingMotions.IDLE, Animations.BIPED_IDLE));
        list.add(Pair.of(LivingMotions.WALK, Animations.BIPED_WALK));
        list.add(Pair.of(LivingMotions.CHASE, Animations.BIPED_WALK));
        list.add(Pair.of(LivingMotions.FALL, Animations.BIPED_FALL));
        list.add(Pair.of(LivingMotions.MOUNT, Animations.BIPED_MOUNT));
        list.add(Pair.of(LivingMotions.DEATH, Animations.BIPED_DEATH));
        return this.addLivingAnimationByList(list);
    }
    public JSCustomMobPatchProviderBuilder setGuardMotion(GuardMotion guardMotions) {
        provider.defaultGuardMotion = guardMotions;
        return this;
    }
    public JSCustomMobPatchProviderBuilder addStunAnimation(Object object1, Object object2){
        StunType stunType = null;
        StaticAnimation animation = null;
        if(object1 instanceof StunType t){
            stunType = t;
        } else if(object1 instanceof String s){
            stunType = StunType.valueOf(s.toUpperCase(Locale.ROOT));
        } else Indestructible.LOGGER.info(object1 + " can't be recognized");

        if(object2 instanceof StaticAnimation a){
            animation = a;
        } else if (object2 instanceof String s) {
            animation = AnimationManager.getInstance().byKeyOrThrow(s);
        } else Indestructible.LOGGER.info(object2 + " can't be recognized");
        if(stunType != null && animation != null)this.provider.stunAnimations.put(stunType, animation);
        return this;
    }
    public JSCustomMobPatchProviderBuilder addStunAnimationByMap(Map<StunType, StaticAnimation> stunAnimations) {
        provider.stunAnimations.putAll(stunAnimations);
        return this;
    }
    public JSCustomMobPatchProviderBuilder intiStunAnimationByDefaultPresent(){
        Map<StunType, StaticAnimation> map = Maps.newHashMap();
        map.put(StunType.SHORT, Animations.BIPED_HIT_SHORT);
        map.put(StunType.LONG, Animations.BIPED_HIT_LONG);
        map.put(StunType.KNOCKDOWN, Animations.BIPED_KNOCKDOWN);
        map.put(StunType.NEUTRALIZE, Animations.BIPED_COMMON_NEUTRALIZED);
        map.put(StunType.FALL, Animations.BIPED_FALL);
        this.addStunAnimationByMap(map);
        return this;
    }
    public JSCustomMobPatchProviderBuilder addStunEvents(LivingEntityPatchEvent.StunEvent[] stunEvent) {
        provider.stunEvent.addAll(List.of(stunEvent));
        return this;
    }
    public JSCustomMobPatchProviderBuilder addStunEvent(LivingEntityPatchEvent.StunEvent stunEvent) {
        provider.stunEvent.add(stunEvent);
        return this;
    }
    public JSCustomMobPatchProviderBuilder setCombatBehavior(CombatBehaviors.Builder<MobPatch<?>> behaviors) {
        provider.combatBehaviorsBuilder = behaviors;
        return this;
    }
    public static class JSCustomMobPatchProvider extends AdvancedMobpatchReloader.AdvancedCustomMobPatchProvider {
        @SuppressWarnings("rawtypes") @Override
        public EntityPatch<?> get(Entity entity) {
            return new JsCustomMobPatch(faction, this);
        }
    }
}
