package com.nameless.indestructible.utils;

import com.mojang.datafixers.util.Pair;
import com.nameless.indestructible.api.animation.types.CustomGuardAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;

public interface CustomHumanoidMobPatchUtils {
    float getDefaultCounterChance();
    float getDefaultCounterCost();
    float getMaxStamina();
    float getStamina();
    void setStamina(float value);
    float getStaminaCostMultiply();
    boolean canBlockProjectile();
    float getAttackSpeed();
    void setAttackSpeed(float value);
    void setBlockTick(int value);
    int getBlockTick();
    CustomGuardAnimation getGuardAnimation();

    Pair<StaticAnimation, Float> getCounterMotion();
}
