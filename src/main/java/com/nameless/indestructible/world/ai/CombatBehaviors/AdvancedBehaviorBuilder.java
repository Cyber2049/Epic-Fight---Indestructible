package com.nameless.indestructible.world.ai.CombatBehaviors;

import com.nameless.indestructible.data.conditions.*;
import com.nameless.indestructible.main.Indestructible;
import com.nameless.indestructible.world.capability.Utils.IAdvancedCapability;
import org.apache.commons.compress.utils.Lists;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.data.conditions.entity.*;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

public class AdvancedBehaviorBuilder<T extends MobPatch<?>> extends CombatBehaviors.Behavior.Builder<T> {
    private final List<Consumer<T>> behaviorList = Lists.newArrayList();
    public static AdvancedBehaviorBuilder<?> Builder(){
        return new AdvancedBehaviorBuilder<>();
    }
    public AdvancedBehaviorBuilder<T> tryProcessAnimationSet(AnimationMotionSet set){
        Consumer<T> c = mobPatch -> {
            if(mobPatch instanceof IAdvancedCapability iac){
                iac.actAnimationMotion(set);
            }
        };
        this.behaviorList.add(c);
        return this;
    }
    public AdvancedBehaviorBuilder<T> tryProcessGuardMotion(GuardMotionSet set) {
        Consumer<T> c = mobPatch -> {
            if(mobPatch instanceof IAdvancedCapability iac){
                iac.actGuardMotion(set);
            }
        };
        this.behaviorList.add(c);
        return this;
    }
    public AdvancedBehaviorBuilder<T> tryProcessWanderSet(WanderMotionSet set) {
        Consumer<T> c = mobPatch -> {
            if(mobPatch instanceof IAdvancedCapability iac){
                iac.actStrafing(set);
            }
        };
        this.behaviorList.add(c);
        return this;
    }
    public AdvancedBehaviorBuilder<T> tryProcessSetPhase(int phase) {
        Consumer<T> c = mobPatch -> {
            if(mobPatch instanceof IAdvancedCapability iac){
                iac.setPhase(phase);
            }
        };
        this.behaviorList.add(c);
        return this;
    }
    public AdvancedBehaviorBuilder<T> tryProcessAnimation(Object object) {
        StaticAnimation animation;
        if(object instanceof StaticAnimation a){
            animation = a;
        } else if(object instanceof String s){
            animation = AnimationManager.getInstance().byKeyOrThrow(s);
        } else {
            animation = Animations.DUMMY_ANIMATION;
            Indestructible.LOGGER.info(object + " can't be recognized");
        }
        Consumer<T> c = mobPatch -> mobPatch.playAnimationSynchronized(animation, 0);

        this.behaviorList.add(c);
        return this;
    }
    public AdvancedBehaviorBuilder<T> tryProcessCustomBehavior(Consumer<T> behavior) {
        this.behaviorList.add(behavior);
        return this;
    }
    public AdvancedBehaviorBuilder<T> tryProcessSetHurtResistLevel(int level) {
        Consumer<T> c = mobPatch -> {
            if(mobPatch instanceof IAdvancedCapability iac){
                iac.setHurtResistLevel(level);
            }
        };
        this.behaviorList.add(c);
        return this;
    }
    public AdvancedBehaviorBuilder<T> process(){
        Consumer<T> c = mobPatch -> this.behaviorList.forEach(b -> b.accept(mobPatch));
        this.behavior(c);
        return this;
    }
    public AdvancedBehaviorBuilder<T> animationBehavior(Object object) {
        StaticAnimation animation;
        if(object instanceof StaticAnimation a){
            animation = a;
        } else if(object instanceof String s){
            animation = AnimationManager.getInstance().byKeyOrThrow(s);
        } else {
            animation = Animations.DUMMY_ANIMATION;
            Indestructible.LOGGER.info(object + " can't be recognized");
        }

        this.animationBehavior(animation);
        return this;
    }
    @Override
    public AdvancedBehaviorBuilder<T> behavior(Consumer<T> behavior) {
        return (AdvancedBehaviorBuilder<T>) super.behavior(behavior);
    }

    @Override
    public AdvancedBehaviorBuilder<T> emptyBehavior() {
        return (AdvancedBehaviorBuilder<T>) super.emptyBehavior();
    }

    @Override
    public AdvancedBehaviorBuilder<T> animationBehavior(StaticAnimation motion) {
        return (AdvancedBehaviorBuilder<T>) super.animationBehavior(motion);
    }

    public AdvancedBehaviorBuilder<T> withinPhase(int minLevel, int maxLevel){
        this.condition(new CustomPhase(minLevel, maxLevel));
        return this;
    }
    public AdvancedBehaviorBuilder<T> health(float health, Object object) {
        HealthPoint.Comparator comparator = null;
        if(object instanceof HealthPoint.Comparator c){
            comparator = c;
        } else if(object instanceof String s){
            comparator = HealthPoint.Comparator.valueOf(s.toUpperCase(Locale.ROOT));
        } else Indestructible.LOGGER.info(object + " can't be recognized");

        this.condition(new HealthPoint(health, comparator));
        return this;
    }
    public AdvancedBehaviorBuilder<T> stamina(float stamina, Object object){
        HealthPoint.Comparator comparator = null;
        if(object instanceof HealthPoint.Comparator c){
            comparator = c;
        } else if(object instanceof String s){
            comparator = HealthPoint.Comparator.valueOf(s.toUpperCase(Locale.ROOT));
        } else Indestructible.LOGGER.info(object + " can't be recognized");

        this.condition(new SelfStamina(stamina, comparator));
        return this;
    }
    public AdvancedBehaviorBuilder<T> targetIsGuardBreak(boolean invert){
        this.condition(new TargetIsGuardBreak(invert));
        return this;
    }
    public AdvancedBehaviorBuilder<T> targetIsKnockDown(boolean invert){
        this.condition(new TargetIsKnockDown(invert));
        return this;
    }
    public AdvancedBehaviorBuilder<T> targetIsUsingItem(boolean isEdible){
        this.condition(new TargetIsUsingItem(isEdible));
        return this;
    }
    public AdvancedBehaviorBuilder<T> targetAttackPhase(int minLevel, int maxLevel){
        this.condition(new TargetWithinState(minLevel, maxLevel));
        return this;
    }
    @Override
    public AdvancedBehaviorBuilder<T> withinEyeHeight() {
        this.condition(new TargetInEyeHeight());
        return this;
    }

    @Override
    public AdvancedBehaviorBuilder<T> randomChance(float chance) {
        this.condition(new RandomChance(chance));
        return this;
    }

    @Override
    public AdvancedBehaviorBuilder<T> withinDistance(double minDistance, double maxDistance) {
        this.condition(new TargetInDistance(minDistance, maxDistance));
        return this;
    }

    @Override
    public AdvancedBehaviorBuilder<T> withinAngle(double minDegree, double maxDegree) {
        this.condition(new TargetInPov(minDegree, maxDegree));
        return this;
    }

    @Override
    public AdvancedBehaviorBuilder<T> withinAngleHorizontal(double minDegree, double maxDegree) {
        this.condition(new TargetInPov.TargetInPovHorizontal(minDegree, maxDegree));
        return this;
    }

    @Override
    public AdvancedBehaviorBuilder<T> health(float health, HealthPoint.Comparator comparator) {
        this.condition(new HealthPoint(health, comparator));
        return this;
    }

    @Override
    public AdvancedBehaviorBuilder<T> custom(Function<T, Boolean> customPredicate) {
        this.condition(new CustomCondition<>(customPredicate));
        return this;
    }

    @Override
    public AdvancedBehaviorBuilder<T> predicate(Condition<T> predicate) {
        return (AdvancedBehaviorBuilder<T>) super.predicate(predicate);
    }

    @Override
    public AdvancedBehaviorBuilder<T> packetProvider(LivingEntityPatch.AnimationPacketProvider packetProvider) {
        return (AdvancedBehaviorBuilder<T>) super.packetProvider(packetProvider);
    }
}
