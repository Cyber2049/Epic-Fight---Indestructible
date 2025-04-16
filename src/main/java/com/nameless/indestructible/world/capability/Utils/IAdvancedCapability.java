package com.nameless.indestructible.world.capability.Utils;

import com.nameless.indestructible.api.animation.types.CustomGuardAnimation;
import com.nameless.indestructible.world.capability.Utils.BehaviorsUtils.CounterMotion;
import com.nameless.indestructible.world.capability.Utils.BehaviorsUtils.DamageSourceModifier;
import com.nameless.indestructible.world.capability.Utils.BehaviorsUtils.GuardMotion;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.damagesource.DamageSource;
import yesman.epicfight.api.animation.types.StaticAnimation;

import javax.annotation.Nullable;

public interface IAdvancedCapability {

    EntityDataAccessor<Float> STAMINA = new EntityDataAccessor<>(253, EntityDataSerializers.FLOAT);
    EntityDataAccessor<Float> ATTACK_SPEED = new EntityDataAccessor<>(177, EntityDataSerializers.FLOAT);
    EntityDataAccessor<Boolean> IS_BLOCKING = new EntityDataAccessor<>(178, EntityDataSerializers.BOOLEAN);
    void initAttributes();
    void setBlocking(boolean blocking);
    boolean isBlocking();
    float getMaxStamina();
    float getStamina();
    void setStamina(float value);
    float getAttackSpeed();
    void setAttackSpeed(float value);
    void resetActionTick();
    void modifyGuardMotion();
    void setParried(boolean isParried);
    float getGuardCostMultiply();
    CustomGuardAnimation getGuardAnimation();
    boolean canBlockProjectile();
    float getParryCostMultiply();
    StaticAnimation getParryAnimation(int times);
    boolean isBlockableSource(DamageSource damageSource);
    void specificGuardMotion(@Nullable GuardMotion guard_motion);
    void setCurrentGuardMotion(GuardMotion guardMotion);
    int getBlockTick();
    void setBlockTick(int tick);
    void setCounterMotion(CounterMotion counter_motion);
    void setMaxParryTimes(int times);
    boolean isParrying();
    void cancelBlock(boolean cancel);
    void setStunImmunityTime(int tick);
    void setDamageSourceModifier(DamageSourceModifier damageSourceModifier);
    int getPhase();
    void setPhase(int phase);
    void setHurtResistLevel(int level);
    void setInterrupted(boolean interrupted);
    boolean interrupted();
    int getStrafingTime();
    void setStrafingTime(int tick);
    int getInactionTime();
    void setInactionTime(int tick);
    float getStrafingForward();
    float getStrafingClockwise();
    void setStrafingDirection(float forward, float clockwise);
}
