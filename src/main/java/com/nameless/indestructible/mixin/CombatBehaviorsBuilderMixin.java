package com.nameless.indestructible.mixin;

import com.nameless.indestructible.api.animation.types.AnimationEvent;
import com.nameless.indestructible.utils.BehaviorInterface;
import com.nameless.indestructible.world.capability.AdvancedCustomHumanoidMobPatch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.network.server.SPPlayAnimation;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

@Mixin(CombatBehaviors.Behavior.Builder.class)
public class CombatBehaviorsBuilderMixin <T extends MobPatch<?>> implements BehaviorInterface<T> {
    @Shadow
    private Consumer<T> behavior;
    @Unique @SuppressWarnings("unchecked")
    public CombatBehaviors.Behavior.Builder<T> customAttackAnimation(AdvancedCustomHumanoidMobPatch.CustomAnimationMotion motion, @Nullable AdvancedCustomHumanoidMobPatch.DamageSourceModifier damageSourceModifier, @Nullable List<AnimationEvent.TimeStampedEvent> timeEvents, @Nullable List<AnimationEvent.HitEvent> hitEvents, int phase) {
        this.behavior = (mobpatch) -> {
            if(mobpatch instanceof AdvancedCustomHumanoidMobPatch<?> advancedCustomHumanoidMobPatch){
                advancedCustomHumanoidMobPatch.setAttackSpeed(motion.speed());
                advancedCustomHumanoidMobPatch.setStrafingTime(0);
               if(motion.stamina() != 0F) advancedCustomHumanoidMobPatch.setStamina(advancedCustomHumanoidMobPatch.getStamina() - motion.stamina());
               if(timeEvents != null){
                    for(AnimationEvent.TimeStampedEvent event : timeEvents){
                        advancedCustomHumanoidMobPatch.addEvent(event);
                    }
               }
                if(hitEvents != null){
                    for(AnimationEvent.HitEvent event : hitEvents){
                        advancedCustomHumanoidMobPatch.addEvent(event);
                    }
                }
                advancedCustomHumanoidMobPatch.setDamageSourceModifier(damageSourceModifier);
                if(phase >= 0)advancedCustomHumanoidMobPatch.setPhase(phase);
            }
            mobpatch.playAnimationSynchronized(motion.animation(), motion.convertTime(), SPPlayAnimation::new);
        };
        return (CombatBehaviors.Behavior.Builder<T>)((Object)this);
    }

    @Unique @SuppressWarnings("unchecked")
    public CombatBehaviors.Behavior.Builder<T> setGuardMotion(int guardTime, StaticAnimation counter, float cost, float chance, float speed) {
        this.behavior = (mobpatch) -> {
            if(mobpatch instanceof AdvancedCustomHumanoidMobPatch<?> advancedCustomHumanoidMobPatch){
                advancedCustomHumanoidMobPatch.setBlockTick(guardTime);
                advancedCustomHumanoidMobPatch.setCounterMotion(counter,cost,chance,speed);
            }
        };
        return (CombatBehaviors.Behavior.Builder<T>)(Object)this;
    }

    @Unique @SuppressWarnings("unchecked")
    public CombatBehaviors.Behavior.Builder<T> setStrafing(int strafingTime, int inactionTime, float forward, float clockwise){
        this.behavior = (mobpatch) -> {
            if(mobpatch instanceof AdvancedCustomHumanoidMobPatch<?> advancedCustomHumanoidMobPatch){
                advancedCustomHumanoidMobPatch.setStrafingTime(strafingTime);
                advancedCustomHumanoidMobPatch.setInactionTime(inactionTime);
                advancedCustomHumanoidMobPatch.setStrafingDirection(forward, clockwise);
            }
        };
        return (CombatBehaviors.Behavior.Builder<T>)(Object)this;
    }
}
