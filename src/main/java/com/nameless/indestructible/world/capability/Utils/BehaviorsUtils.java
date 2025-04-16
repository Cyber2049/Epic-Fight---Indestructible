package com.nameless.indestructible.world.capability.Utils;

import com.nameless.indestructible.api.animation.types.LivingEntityPatchEvent;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.network.server.SPPlayAnimation;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.damagesource.StunType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class BehaviorsUtils {

    public static <T extends MobPatch<?>> Consumer<T> customAttackAnimation(CustomAnimationMotion motion, @Nullable DamageSourceModifier damageSourceModifier,
                                                                            @Nullable List<LivingEntityPatchEvent.TimeStampedEvent> timeEvents, @Nullable List<LivingEntityPatchEvent.BiEvent> hitEvents, @Nullable List<LivingEntityPatchEvent.BlockedEvent> blockedEvents, int phase, int hurtResist){
        return (mobpatch) -> {
            if(mobpatch instanceof IAdvancedCapability iac){
                iac.setAttackSpeed(motion.speed());
                iac.setBlocking(false);
                if(motion.stamina() != 0F) iac.setStamina(iac.getStamina() - motion.stamina());
                iac.setDamageSourceModifier(damageSourceModifier);
                if(phase >= 0)iac.setPhase(phase);
                iac.setHurtResistLevel(hurtResist);
            }
            if(mobpatch instanceof IAnimationEventCapability iec){
                iec.getEventManager().initAnimationEvent();
                if(timeEvents != null){
                    for(LivingEntityPatchEvent.TimeStampedEvent event : timeEvents){
                        iec.getEventManager().addTimeStampedEvent(event);
                    }
                }
                if(hitEvents != null){
                    for(LivingEntityPatchEvent.BiEvent event : hitEvents){
                        iec.getEventManager().addHitEvent(event);
                    }
                }
                if(blockedEvents != null){
                    for(LivingEntityPatchEvent.BlockedEvent event : blockedEvents){
                        iec.getEventManager().addBlockedEvents(event);
                    }
                }
            }
            if(!mobpatch.getEntityState().turningLocked()){mobpatch.getOriginal().lookAt(mobpatch.getTarget(),30F,30F); }
            mobpatch.playAnimationSynchronized(motion.animation(), motion.convertTime(), SPPlayAnimation::new);
        };
    }

    public static <T extends MobPatch<?>> Consumer<T> setGuardMotion(int guardTime, int parry_time, int stun_immunity_time, CounterMotion counter_motion,
                                                                     boolean cancel, @Nullable GuardMotion guard_motion, int phase, int hurtResist) {
        return (mobpatch) -> {
            if(mobpatch instanceof IAdvancedCapability iac){
                if(guard_motion != null) iac.specificGuardMotion(guard_motion);
                iac.modifyGuardMotion();
                iac.setBlocking(true);
                iac.setBlockTick(guardTime);
                iac.setMaxParryTimes(parry_time);
                iac.setStunImmunityTime(stun_immunity_time);
                iac.setCounterMotion(counter_motion);
                iac.cancelBlock(cancel);
                iac.setHurtResistLevel(hurtResist);
                if(phase >= 0)iac.setPhase(phase);
            }
        };
    }

    public static <T extends MobPatch<?>> Consumer<T> setStrafing(int strafingTime, int inactionTime, float forward, float clockwise, int phase, int hurtResist){
        return (mobpatch) -> {
            if(mobpatch instanceof IAdvancedCapability iac){
                iac.setStrafingTime(strafingTime);
                iac.setInactionTime(inactionTime);
                iac.setStrafingDirection(forward,clockwise);
                iac.setHurtResistLevel(hurtResist);
                if(phase >= 0)iac.setPhase(phase);
            }
        };
    }

    public record CustomAnimationMotion(StaticAnimation animation, float convertTime, float speed, float stamina) { }
    public static class DamageSourceModifier{
        public final float damage;
        public final float impact;
        public final float armor_negation;
        public final StunType stunType;
        public final Collider collider;
        public DamageSourceModifier(float damage, float impact, float armor_negation, @Nullable StunType stunType, @Nullable Collider collider){
            this.damage = damage;
            this.impact = impact;
            this.armor_negation = armor_negation;
            this.stunType = stunType;
            this.collider = collider;
        }
    }
    public static class CounterMotion {
        public final StaticAnimation counter;
        public final float cost;
        public final float chance;
        public final float speed;
        public CounterMotion (StaticAnimation counter, float cost, float chance, float speed){
            this.counter = counter;
            this.cost = cost;
            this.chance = chance;
            this.speed = speed;
        }
    }
    public static class GuardMotion{
        public final StaticAnimation guard_animation;
        public final boolean canBlockProjectile;
        public final float cost;
        public final float parry_cost;
        public final StaticAnimation[] parry_animation;
        public GuardMotion(StaticAnimation guard_animation, boolean canBlockProjectile, float cost, float parry_cost, StaticAnimation[] parry_animation){
            this.guard_animation = guard_animation;
            this.canBlockProjectile = canBlockProjectile;
            this.cost = cost;
            this.parry_cost = parry_cost;
            this.parry_animation = parry_animation;
        }
        public GuardMotion(StaticAnimation guard_animation, boolean canBlockProjectile, float cost){
            this.guard_animation = guard_animation;
            this.canBlockProjectile = canBlockProjectile;
            this.cost = cost;
            this.parry_cost = 0.5F;
            this.parry_animation =  new StaticAnimation[]{Animations.LONGSWORD_GUARD_ACTIVE_HIT1, Animations.LONGSWORD_GUARD_ACTIVE_HIT2};
        }
    }
}
