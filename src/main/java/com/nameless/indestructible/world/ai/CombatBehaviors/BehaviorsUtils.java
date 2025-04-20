package com.nameless.indestructible.world.ai.CombatBehaviors;

import com.nameless.indestructible.world.capability.Utils.IAdvancedCapability;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;

import java.util.function.Consumer;

public class BehaviorsUtils {

    public static <T extends MobPatch<?>> Consumer<T> customAttackAnimation(AnimationMotionSet motionSet, int phase, int hurtResist){
        return (mobpatch) -> {
            if(mobpatch instanceof IAdvancedCapability iac) {
                setPhase(phase).accept(iac);
                setHurtResistLevel(hurtResist).accept(iac);
                iac.actAnimationMotion(motionSet);
            }
        };
    }

    public static <T extends MobPatch<?>> Consumer<T> setGuardMotion(GuardMotionSet motionSet, int phase, int hurtResist) {
        return (mobpatch) -> {
            if(mobpatch instanceof IAdvancedCapability iac){
                setPhase(phase).accept(iac);
                setHurtResistLevel(hurtResist).accept(iac);
                iac.actGuardMotion(motionSet);
            }
        };
    }

    public static <T extends MobPatch<?>> Consumer<T> setStrafing(WanderMotionSet wanderMotionSet, int phase, int hurtResist){
        return (mobpatch) -> {
            if(mobpatch instanceof IAdvancedCapability iac){
                setPhase(phase).accept(iac);
                setHurtResistLevel(hurtResist).accept(iac);
                iac.actStrafing(wanderMotionSet);
            }
        };
    }
    public static <T extends IAdvancedCapability> Consumer<T> setPhase(int phase){
        return (iac) -> {
                if(phase >= 0)iac.setPhase(phase);
        };
    }
    public static <T extends IAdvancedCapability> Consumer<T> setHurtResistLevel(int hurtResist){
        return (iac) -> {
            iac.setHurtResistLevel(hurtResist);
        };
    }

}
