package com.nameless.indestructible.mixin;

import com.mojang.datafixers.util.Pair;
import com.nameless.indestructible.api.animation.types.CustomGuardAnimation;
import com.nameless.indestructible.gameasset.GuardAnimations;
import com.nameless.indestructible.utils.CustomHumanoidMobPatchUtils;
import com.nameless.indestructible.utils.ProviderUtils;
import com.nameless.indestructible.world.ai.goal.RangedGuardGoal;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.data.reloader.MobPatchReloadListener;
import yesman.epicfight.world.capabilities.entitypatch.CustomHumanoidMobPatch;
import yesman.epicfight.world.capabilities.entitypatch.Faction;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

import java.util.Map;

@Mixin(CustomHumanoidMobPatch.class)
public abstract class CustomHumanoidMobPatchMixin <T extends PathfinderMob> extends HumanoidMobPatch<T> implements CustomHumanoidMobPatchUtils {
    @Mutable
    @Shadow
    @Final
    private MobPatchReloadListener.CustomHumanoidMobPatchProvider provider;
    @Unique
    private static final EntityDataAccessor<Float> BLOCK_STAMINA = new EntityDataAccessor<Float>(253, EntityDataSerializers.FLOAT);
    @Unique
    private static final EntityDataAccessor<Float> ATTACK_SPEED = new EntityDataAccessor<Float>(177, EntityDataSerializers.FLOAT);
    private int BLOCK_TICK = 0;
    public CustomHumanoidMobPatchMixin(Faction faction, MobPatchReloadListener.CustomHumanoidMobPatchProvider provider){
        super(faction);
        this.provider = provider;
        this.weaponLivingMotions = this.provider.getHumanoidWeaponMotions();
        this.weaponAttackMotions = this.provider.getHumanoidCombatBehaviors();
    }

    @Override
    public void onConstructed(T entityIn) {
        super.onConstructed(entityIn);
        entityIn.getEntityData().define(BLOCK_STAMINA, 0.0F);
        entityIn.getEntityData().define(ATTACK_SPEED, 0.0F);
    }

    @Override
    public void onJoinWorld(T entityIn, EntityJoinWorldEvent event) {
        super.onJoinWorld(entityIn, event);
        this.setStamina(this.getMaxStamina());
        this.setAttackSpeed(1F);
    }

    @Override
    public void serverTick(LivingEvent.LivingUpdateEvent event) {
        super.serverTick(event);

        float stamina = this.getStamina();
        float maxStamina = this.getMaxStamina();
        int blocktick = this.getBlockTick();

        if (stamina < maxStamina) {
            if(blocktick <= 0 && !this.state.inaction()) {
                float staminaFactor = 1.0F + (float) Math.pow((stamina / (maxStamina - stamina * 0.5F)), 2);
                this.setStamina(stamina + maxStamina * 0.01F * staminaFactor);
            } else {
                this.setStamina(stamina + 0.0015F * maxStamina);
            }
        }

        if (maxStamina < stamina) {
            this.setStamina(maxStamina);
        }
    }

    public void setBlockTick(int value){this.BLOCK_TICK = value;}

    public int getBlockTick(){return this.BLOCK_TICK;}

    public float getDefaultCounterChance(){
        return ((ProviderUtils)this.provider).getCounterChance();
    }
    public float getDefaultCounterCost() {return  ((ProviderUtils)this.provider).getCounterCost();}

    public float getMaxStamina() {
        return ((ProviderUtils)this.provider).getBlockStamina();
    }

    public float getStamina() {
        return this.getMaxStamina() == 0 ? 0 : Math.max(0,this.original.getEntityData().get(BLOCK_STAMINA));
    }

    public void setStamina(float value) {
        float f1 = Math.max(Math.min(value, this.getMaxStamina()), 0);
        this.original.getEntityData().set(BLOCK_STAMINA, f1);
    }

    public float getStaminaCostMultiply(){
        return ((ProviderUtils)this.provider).getStaminaCostMultiply();
    }

    public boolean canBlockProjectile(){
        return ((ProviderUtils)this.provider).canBlockProjectile();
    }
    public float getAttackSpeed(){return this.original.getEntityData().get(ATTACK_SPEED);}
    public void setAttackSpeed(float value){
        this.original.getEntityData().set(ATTACK_SPEED, value);
    }

    public CustomGuardAnimation getGuardAnimation(){
        return (CustomGuardAnimation) getGuardMotion().getFirst();
    }

    public Pair<StaticAnimation, Float> getCounterMotion(){
        return getGuardMotion().getSecond();
    }

    private Pair<StaticAnimation, Pair<StaticAnimation, Float>> getGuardMotion(){
        Map<WeaponCategory, Map<Style,Pair<StaticAnimation, Pair<StaticAnimation, Float>>>> list = ((ProviderUtils)this.provider).getGuardMotions();
        CapabilityItem itemCap = this.getHoldingItemCapability(InteractionHand.MAIN_HAND);
        if(list != null && itemCap != null){
            Style style = itemCap.getStyle(this);
            Map<Style,Pair<StaticAnimation, Pair<StaticAnimation, Float>>> mapByStyle = list.get(itemCap.getWeaponCategory());
            if (mapByStyle != null && (mapByStyle.containsKey(style) || mapByStyle.containsKey(CapabilityItem.Styles.COMMON))) {
                Pair<StaticAnimation, Pair<StaticAnimation, Float>> motion = mapByStyle.getOrDefault(style, mapByStyle.get(CapabilityItem.Styles.COMMON));
                return motion;
            } else {return Pair.of(GuardAnimations.MOB_LONGSWORD_GUARD,Pair.of(GuardAnimations.MOB_COUNTER_ATTACK,1F));}
        } else {return Pair.of(GuardAnimations.MOB_LONGSWORD_GUARD,Pair.of(GuardAnimations.MOB_COUNTER_ATTACK,1F));}
    }

    @Inject(method = "getHitAnimation(Lyesman/epicfight/world/damagesource/StunType;)Lyesman/epicfight/api/animation/types/StaticAnimation;", at = @At("HEAD"))
    private void onGetHitAnimation(StunType stunType, CallbackInfoReturnable<StaticAnimation> cir) {
        this.setAttackSpeed(1F);
    }

    @Inject(method = "setAIAsInfantry(Z)V", at = @At("HEAD"))
    public void setAIAsInfantry(boolean holdingRanedWeapon, CallbackInfo ci) {
        boolean isUsingBrain = this.getOriginal().getBrain().availableBehaviorsByPriority.size() > 0;

        if (!isUsingBrain) {
            if (!holdingRanedWeapon) {
                CombatBehaviors.Builder<HumanoidMobPatch<?>> builder = this.getHoldingItemWeaponMotionBuilder();

                if (builder != null) {
                    this.original.goalSelector.addGoal(0, new RangedGuardGoal<>((CustomHumanoidMobPatch<?>) (Object)this,this.provider.getChasingSpeed() * 0.9,4.0F));
                }
            }
        }
    }
}
