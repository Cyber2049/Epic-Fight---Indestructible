package com.nameless.indestructible.mixin;

import com.mojang.datafixers.util.Pair;
import com.nameless.indestructible.utils.ProviderUtils;
import org.spongepowered.asm.mixin.Mixin;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.data.reloader.MobPatchReloadListener;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

import java.util.Map;

@Mixin(MobPatchReloadListener.CustomHumanoidMobPatchProvider.class)
public class CustomMobPatchProviderMixin implements ProviderUtils {
    protected float counter_chance;
    protected float block_stamina;
    protected float counter_cost;
    protected float stamina_cost_multiply;
    protected boolean canBlockProjectile;
    protected Map<WeaponCategory, Map<Style,Pair<StaticAnimation,Pair<StaticAnimation, Float>>>> defaultGuardAnimations;
    public float getCounterChance(){return Math.max(Math.min(1, this.counter_chance), 0);}
    public float getCounterCost(){return Math.min(this.block_stamina, this.counter_cost);}
    public float getBlockStamina(){return this.block_stamina;}
    public float getStaminaCostMultiply(){return this.stamina_cost_multiply;}
    public boolean canBlockProjectile(){return this.canBlockProjectile;}
    public Map<WeaponCategory, Map<Style,Pair<StaticAnimation, Pair<StaticAnimation, Float>>>> getGuardMotions(){
        return this.defaultGuardAnimations;
    }


    public void setCounterChance(float chance){
        this.counter_chance = chance;
    }
    public void setCounterCost(float cost) {this.counter_cost = cost;}
    public void setBlockStamina(float stamina){
        this.block_stamina = stamina;
    }

    public void setStaminaCostMultiply(float multiply){
        this.stamina_cost_multiply = multiply;
    }

    public void setCanBlockProjectile(boolean can){
        this.canBlockProjectile = can;
    }
    public void setGuardMotions(Map<WeaponCategory, Map<Style,Pair<StaticAnimation,Pair<StaticAnimation, Float>>>> list){
        this.defaultGuardAnimations = list;
    }
}
