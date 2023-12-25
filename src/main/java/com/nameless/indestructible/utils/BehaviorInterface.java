package com.nameless.indestructible.utils;

import com.nameless.indestructible.api.animation.types.AnimationEvent;
import com.nameless.indestructible.world.capability.AdvancedCustomHumanoidMobPatch;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

import javax.annotation.Nullable;
import java.util.List;

public interface BehaviorInterface<T extends MobPatch<?>>{
    @SuppressWarnings("all")
    CombatBehaviors.Behavior.Builder<T> customAttackAnimation(AdvancedCustomHumanoidMobPatch.CustomAnimationMotion motion, @Nullable AdvancedCustomHumanoidMobPatch.DamageSourceModifier damageSourceModifier, @Nullable List<AnimationEvent.TimeStampedEvent> events, @Nullable List<AnimationEvent.HitEvent> hitEvents, int phase);
    @SuppressWarnings("all")
    CombatBehaviors.Behavior.Builder<T> setGuardMotion(int guardTime, StaticAnimation counter, float cost, float chance, float speed);

    @SuppressWarnings("all")
    CombatBehaviors.Behavior.Builder<T> setStrafing(int strafingTime, int inactionTime, float foward, float clockwise);
}
