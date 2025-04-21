package com.nameless.indestructible.data;


import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.nameless.indestructible.api.animation.types.LivingEntityPatchEvent;
import com.nameless.indestructible.compat.kubejs.JsMobPatch;
import com.nameless.indestructible.main.Indestructible;
import com.nameless.indestructible.world.ai.CombatBehaviors.GuardMotion;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
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

public class JsMobPatchProviderBuilder {
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
    public static JsMobPatchProviderBuilder builder() {
        return new JsMobPatchProviderBuilder();
    }

    @Info(value = "mandatory, model of the entity")
    public JsMobPatchProviderBuilder setModel(String model){
        this.clientTag.putString("model", model);
        return this;
    }
    @Info(value = "mandatory, armature of the entity")
    public JsMobPatchProviderBuilder setArmature(String armature){
        this.clientTag.putString("armature", armature);
        return this;
    }
    @Info(value = "mandatory, renderer of the entity")
    public JsMobPatchProviderBuilder setRenderer(String renderer){
        this.clientTag.putString("renderer", renderer);
        return this;
    }
    @Info(value = "mandatory, faction of the entity")
    public JsMobPatchProviderBuilder setFaction(Object faction) {
        if(faction instanceof String string){
            this.provider.faction = Faction.valueOf(string.toUpperCase(Locale.ROOT));
            this.clientTag.putString("faction", string);
        } else if(faction instanceof Faction f) {
            this.provider.faction = f;
            this.clientTag.putString("faction", f.toString());
        } else throw new IllegalArgumentException(faction + " can't be recognized");

       return this;
    }
    @Info(value = "optional, will display a customizable boss bar")
    public JsMobPatchProviderBuilder hasBossBar() {
        provider.hasBossBar = true;
        return this;
    }
    @Info(value = "optional, define the boss bar texture")
    public JsMobPatchProviderBuilder setBossBarTexture(String rl) {
        this.clientTag.putString("custom_texture", rl);
        return this;
    }
    @Info(value = "optional, define the boss bar title, default: registry name of this entity")
    public JsMobPatchProviderBuilder setBossName(String langkey){
        this.clientTag.putString("custom_name", langkey);
        return this;
    }
    @Info(value = "optional, scale, default: 1")
    public JsMobPatchProviderBuilder setScale(float scale) {
        provider.scale = scale;
        CompoundTag att = new CompoundTag();
        att.putDouble("scale", scale);
        this.clientTag.put("attribute", att);
        return this;
    }
    @Info(value = "optional, movement speed, default: 1")
    public JsMobPatchProviderBuilder setChasingSpeed(double speed) {
        provider.chasingSpeed = speed;
        return this;
    }
    @Info(value = "optional, consider stopping defence if target is out of distance , default: 3")
    public JsMobPatchProviderBuilder setGuardRadius(float guardRadius) {
        provider.guardRadius = guardRadius;
        return this;
    }
    @Info(value = "optional, consider getting closer to the target if target is out of distance and no action to be taken, default: 1.5")
    public JsMobPatchProviderBuilder setAttackRadius(float attackRadius) {
        provider.attackRadius = attackRadius;
        return this;
    }
    @Info(value = "optional, define its attributes' base value one by one", params = {
            @Param(name = "object", value = "Attribute or registry name of attribute(String)"), @Param(name = "value", value = "number")
    })
    public JsMobPatchProviderBuilder addAttribute(Object object, Double value){
        if(object instanceof Attribute attribute){
            this.provider.attributeValues.put(attribute, value);
        } else if (object instanceof String string) {
            ResourceLocation rl = new ResourceLocation(string);
            Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(rl);
            if(attribute != null) this.provider.attributeValues.put(attribute, value);
            else Indestructible.LOGGER.warn(string + " doesn't exist");
        } else Indestructible.LOGGER.warn(object + " can't be recognized");
        return this;
    }
    @Info(value = "optional, define its attributes' base value by map, call AttributeMapHelper.getHelper() to get the map builder, add attribute values to this map builder, and call createMap() to return current map")
    public JsMobPatchProviderBuilder addAttributesByMap(Map<Attribute, Double> map) {
        this.provider.attributeValues.putAll(map);
        return this;
    }
    @Info(value = "optional, define the delay time of stamina regen, default: 30(tick)")
    public JsMobPatchProviderBuilder setRegenStaminaStandbyTime(int tick) {
        provider.regenStaminaStandbyTime = tick;
        return this;
    }
    @Info(value = "optional, if stun time decay during continuous stun, default: true")
    public JsMobPatchProviderBuilder hasStunReduction(boolean reduce) {
        provider.hasStunReduction = reduce;
        return this;
    }
    @Info(value = "optional, max value of stun shield which can prevent entity from being stunned, default: 0")
    public JsMobPatchProviderBuilder setMaxStunShield(float maxStunShield) {
        provider.maxStunShield = maxStunShield;
        return this;
    }

    public JsMobPatchProviderBuilder setReganShieldMultiply(float reganShieldMultiply) {
        provider.reganShieldMultiply = reganShieldMultiply;
        return this;
    }
    @Info(value = "optional, define the delay time of stun shield regen if entity has stun shield, default: 30")
    public JsMobPatchProviderBuilder setReganShieldStandByTime(int reganShieldStandbyTime) {
        provider.reganShieldStandbyTime = reganShieldStandbyTime;
        return this;
    }
    @Info(value = "optional, if entity will lose stamina when it's stunned, and define the rate of losing stamina, default: 0")
    public JsMobPatchProviderBuilder setStaminaLoseMultiply(float staminaLoseMultiply) {
        provider.staminaLoseMultiply = staminaLoseMultiply;
        return this;
    }
    @Info(value = "mandatory or alternative method, bind animation to living motion of entity one by one", params = {
            @Param(name = "object1", value = "living motion, instance or its name(string)"), @Param(name = "object2", value = "animation, animation instance or its registry name(String)")
    })
    public JsMobPatchProviderBuilder addLivingAnimation(Object object1, Object object2) {
        LivingMotion livingMotion = null;
        StaticAnimation animation = null;

        if(object1 instanceof String s){
            livingMotion = LivingMotions.valueOf(s.toUpperCase(Locale.ROOT));
        } else if(object1 instanceof LivingMotions l){
            livingMotion = l;
        } else Indestructible.LOGGER.warn(object1 + " can't be recognized");

        if(object2 instanceof String s){
            animation = AnimationManager.getInstance().byKeyOrThrow(s);
        } else if (object2 instanceof StaticAnimation a){
            animation = a;
        } else Indestructible.LOGGER.warn(object2 + " can't be recognized");

        if(livingMotion != null && animation != null) {
            this.provider.defaultAnimations.add(Pair.of(livingMotion, animation));
            this.livingMotionTag.putString(livingMotion.toString(), animation.getRegistryName().toString());
        }
        return this;
    }
    @Info(value = "mandatory or alternative method, bind animation to living motion by map, call LivingMotionHelper.getHelper() to get the map builder, bind animation and living motion to the map, and call createList() to return this map")
    public JsMobPatchProviderBuilder addLivingAnimationByList(List<Pair<LivingMotion, StaticAnimation>> list) {
        provider.defaultAnimations.addAll(list);
        list.forEach(l -> this.livingMotionTag.putString(l.getFirst().toString(), l.getSecond().getRegistryName().toString()));
        return this;
    }
    @Info(value = "mandatory or alternative method, use default present of living motion, make sure these animations are matching the armature")
    public JsMobPatchProviderBuilder initLivingAnimationByDefaultPresent(){
        List<Pair<LivingMotion, StaticAnimation>> list = new ArrayList<>();
        list.add(Pair.of(LivingMotions.IDLE, Animations.BIPED_IDLE));
        list.add(Pair.of(LivingMotions.WALK, Animations.BIPED_WALK));
        list.add(Pair.of(LivingMotions.CHASE, Animations.BIPED_WALK));
        list.add(Pair.of(LivingMotions.FALL, Animations.BIPED_FALL));
        list.add(Pair.of(LivingMotions.MOUNT, Animations.BIPED_MOUNT));
        list.add(Pair.of(LivingMotions.DEATH, Animations.BIPED_DEATH));
        return this.addLivingAnimationByList(list);
    }
    @Info(value = "mandatory if it will perform defence, set default guard motion, call GuardMotion.create() to return a guard motion, and call the method in it to define its properties")
    public JsMobPatchProviderBuilder setGuardMotion(GuardMotion guardMotions) {
        provider.defaultGuardMotion = guardMotions;
        return this;
    }
    @Info(value = "mandatory or alternative method otherwise won't be stunned, bind stun animation to stun type one by one", params = {
            @Param(name = "object1", value = "stun type, enum or name(string)"), @Param(name = "object2", value = "animation, animation instance or its registry name(String)")
    })
    public JsMobPatchProviderBuilder addStunAnimation(Object object1, Object object2){
        StunType stunType = null;
        StaticAnimation animation = null;
        if(object1 instanceof StunType t){
            stunType = t;
        } else if(object1 instanceof String s){
            stunType = StunType.valueOf(s.toUpperCase(Locale.ROOT));
        } else Indestructible.LOGGER.warn(object1 + " can't be recognized");

        if(object2 instanceof StaticAnimation a){
            animation = a;
        } else if (object2 instanceof String s) {
            animation = AnimationManager.getInstance().byKeyOrThrow(s);
        } else Indestructible.LOGGER.warn(object2 + " can't be recognized");
        if(stunType != null && animation != null)this.provider.stunAnimations.put(stunType, animation);
        return this;
    }
    @Info(value = "mandatory or alternative method otherwise won't be stunned, bind stun animation to stun type by map, call StunAnimationHelper.getHelper() to get the map builder, bind animation to sunt type, and call createMap() to return the map")
    public JsMobPatchProviderBuilder addStunAnimationByMap(Map<StunType, StaticAnimation> stunAnimations) {
        provider.stunAnimations.putAll(stunAnimations);
        return this;
    }
    @Info(value = "mandatory or alternative method, use default present of stun type, make sure these animations are matching the armature")
    public JsMobPatchProviderBuilder intiStunAnimationByDefaultPresent(){
        Map<StunType, StaticAnimation> map = Maps.newHashMap();
        map.put(StunType.SHORT, Animations.BIPED_HIT_SHORT);
        map.put(StunType.LONG, Animations.BIPED_HIT_LONG);
        map.put(StunType.KNOCKDOWN, Animations.BIPED_KNOCKDOWN);
        map.put(StunType.NEUTRALIZE, Animations.BIPED_COMMON_NEUTRALIZED);
        map.put(StunType.FALL, Animations.BIPED_FALL);
        this.addStunAnimationByMap(map);
        return this;
    }
    @Info(value = "optional, add stun event which will perform when entity is stunned by array", params = {
            @Param(name = "stunEvents", value = "StunEvent[], [] call PatchEvent.createStunEvent(BiConsumer<LivingEntityPatch<?>, Entity> event, Object object) to create an event")
    })
    public JsMobPatchProviderBuilder addStunEvents(LivingEntityPatchEvent.StunEvent[] stunEvents) {
        provider.stunEvent.addAll(List.of(stunEvents));
        return this;
    }
    @Info(value = "optional, add stun event which will perform when entity is stunned one by one", params = {
            @Param(name = "stunEvent", value = "StunEvent, [] call PatchEvent.createStunEvent(BiConsumer<LivingEntityPatch<?>, Entity> event, Object object) to create an event")
    })
    public JsMobPatchProviderBuilder addStunEvent(LivingEntityPatchEvent.StunEvent stunEvent) {
        provider.stunEvent.add(stunEvent);
        return this;
    }
    @Info(value = "mandatory, otherwise entity won't combat, set combat behavior to entity, call EFCombatBehaviors.builder() to get the builder and call method in it to define entity's combat behavior")
    public JsMobPatchProviderBuilder setCombatBehavior(CombatBehaviors.Builder<MobPatch<?>> behaviors) {
        provider.combatBehaviorsBuilder = behaviors;
        return this;
    }
    public static class JSCustomMobPatchProvider extends AdvancedMobpatchReloader.AdvancedCustomMobPatchProvider {
        @SuppressWarnings("rawtypes") @Override
        public EntityPatch<?> get(Entity entity) {
            return new JsMobPatch(faction, this);
        }
    }
}
