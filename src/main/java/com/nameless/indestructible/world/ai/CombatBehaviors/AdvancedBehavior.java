package com.nameless.indestructible.world.ai.CombatBehaviors;

import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

public class AdvancedBehavior<T extends MobPatch<?>> extends CombatBehaviors.Behavior.Builder<T> {
    public AdvancedBehavior<T> customAttackAnimation(AnimationMotionSet motionSet, int phase, int hurtResist) {
        this.behavior(BehaviorsUtils.customAttackAnimation(motionSet, phase, hurtResist));
        return this;
    }
    public AdvancedBehavior<T> setGuardMotion(GuardMotionSet motionSet, int phase, int hurtResist) {
        this.behavior(BehaviorsUtils.setGuardMotion(motionSet, phase, hurtResist));
        return this;
    }
    public AdvancedBehavior<T> setStrafing(WanderMotionSet wanderMotionSet, int phase, int hurtResist) {
        this.behavior(BehaviorsUtils.setStrafing(wanderMotionSet, phase, hurtResist));
        return this;
    }
}
