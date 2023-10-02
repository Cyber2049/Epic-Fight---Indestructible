package com.nameless.indestructible.utils;

import com.mojang.datafixers.util.Pair;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

import java.util.Map;

public interface ProviderUtils {

    float getCounterChance();
    float getCounterCost();
    float getBlockStamina();
    float getStaminaCostMultiply();
    boolean canBlockProjectile();
    Map<WeaponCategory, Map<Style,Pair<StaticAnimation,Pair<StaticAnimation, Float>>>> getGuardMotions();

    void setCounterChance(float chance);
    void setCounterCost(float cost);
    void setBlockStamina(float stamina);
    void setStaminaCostMultiply(float multiply);
    void setCanBlockProjectile(boolean can);
    void setGuardMotions(Map<WeaponCategory, Map<Style,Pair<StaticAnimation, Pair<StaticAnimation, Float>>>> list);
}
