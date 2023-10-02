package com.nameless.indestructible.utils;

import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

public interface BehaviorUtils <T extends MobPatch<?>>{
    @SuppressWarnings("all")
    CombatBehaviors.Behavior.Builder<T> customAttackAnimation(StaticAnimation motion,float convertTime, float speed, float stamina);
    @SuppressWarnings("all")
    CombatBehaviors.Behavior.Builder<T> setGuardTime(int time);
}
