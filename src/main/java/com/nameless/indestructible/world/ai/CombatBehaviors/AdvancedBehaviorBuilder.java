package com.nameless.indestructible.world.ai.CombatBehaviors;

import com.nameless.indestructible.data.conditions.*;
import com.nameless.indestructible.main.Indestructible;
import com.nameless.indestructible.world.capability.Utils.IAdvancedCapability;
import dev.latvian.mods.kubejs.typings.Info;
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
    @Info(value = "add animation motion set to the To-be-executed list, call process() later to fulfill it, call AnimationMotionSet.create() to create an Animation motion set, and call method in it to define its properties")
    public AdvancedBehaviorBuilder<T> tryProcessAnimationSet(AnimationMotionSet set){
        Consumer<T> c = mobPatch -> {
            if(mobPatch instanceof IAdvancedCapability iac){
                iac.actAnimationMotion(set);
            }
        };
        this.behaviorList.add(c);
        return this;
    }
    @Info(value = "add guard motion set to the To-be-executed list, call process() later to fulfill it, call GuardMotionSet.create() to create a guard motion set, and call method in it to define its properties")
    public AdvancedBehaviorBuilder<T> tryProcessGuardMotion(GuardMotionSet set) {
        Consumer<T> c = mobPatch -> {
            if(mobPatch instanceof IAdvancedCapability iac){
                iac.actGuardMotion(set);
            }
        };
        this.behaviorList.add(c);
        return this;
    }
    @Info(value = "add wander motion set to the To-be-executed list, call process() later to fulfill it, call WanderMotionSet.create() to create a wander motion set, and call method in it to define its properties")
    public AdvancedBehaviorBuilder<T> tryProcessWanderSet(WanderMotionSet set) {
        Consumer<T> c = mobPatch -> {
            if(mobPatch instanceof IAdvancedCapability iac){
                iac.actStrafing(set);
            }
        };
        this.behaviorList.add(c);
        return this;
    }
    @Info(value = "add behavior of setting entity phase to the To-be-executed list, call process() later to fulfill it")
    public AdvancedBehaviorBuilder<T> tryProcessSetPhase(int phase) {
        Consumer<T> c = mobPatch -> {
            if(mobPatch instanceof IAdvancedCapability iac){
                iac.setPhase(phase);
            }
        };
        this.behaviorList.add(c);
        return this;
    }
    @Info(value = "add behavior of setting entity hurtResist level to the To-be-executed list, call process() later to fulfill it")
    public AdvancedBehaviorBuilder<T> tryProcessSetHurtResistLevel(int level) {
        Consumer<T> c = mobPatch -> {
            if(mobPatch instanceof IAdvancedCapability iac){
                iac.setHurtResistLevel(level);
            }
        };
        this.behaviorList.add(c);
        return this;
    }
    @Info(value = "add behavior of playing animation to the To-be-executed list which , call process() later to fulfill it")
    public AdvancedBehaviorBuilder<T> tryProcessAnimation(Object object) {
        StaticAnimation animation;
        if(object instanceof StaticAnimation a){
            animation = a;
        } else if(object instanceof String s){
            animation = AnimationManager.getInstance().byKeyOrThrow(s);
        } else {
            animation = Animations.DUMMY_ANIMATION;
            Indestructible.LOGGER.warn(object + " can't be recognized");
        }
        Consumer<T> c = mobPatch -> mobPatch.playAnimationSynchronized(animation, 0);

        this.behaviorList.add(c);
        return this;
    }
    @Info(value = "add custom behavior to the To-be-executed list which , call process() later to fulfill it")
    public AdvancedBehaviorBuilder<T> tryProcessCustomBehavior(Consumer<T> behavior) {
        this.behaviorList.add(behavior);
        return this;
    }
    public AdvancedBehaviorBuilder<T> process(){
        Consumer<T> c = mobPatch -> this.behaviorList.forEach(b -> b.accept(mobPatch));
        this.behavior(c);
        return this;
    }
    @Info(value = "behavior of playing animation")
    public AdvancedBehaviorBuilder<T> animationBehavior(Object object) {
        StaticAnimation animation;
        if(object instanceof StaticAnimation a){
            animation = a;
        } else if(object instanceof String s){
            animation = AnimationManager.getInstance().byKeyOrThrow(s);
        } else {
            animation = Animations.DUMMY_ANIMATION;
            Indestructible.LOGGER.warn(object + " can't be recognized");
        }

        this.animationBehavior(animation);
        return this;
    }
    @Info(value = "custom behavior")
    @Override
    public AdvancedBehaviorBuilder<T> behavior(Consumer<T> behavior) {
        return (AdvancedBehaviorBuilder<T>) super.behavior(behavior);
    }
    @Info(value = "empty behavior")
    @Override
    public AdvancedBehaviorBuilder<T> emptyBehavior() {
        return (AdvancedBehaviorBuilder<T>) super.emptyBehavior();
    }
    @Info(value = "behavior of playing animation, only allow instance of animation as parameter")
    @Override
    public AdvancedBehaviorBuilder<T> animationBehavior(StaticAnimation motion) {
        return (AdvancedBehaviorBuilder<T>) super.animationBehavior(motion);
    }
    @Info(value = "condition of entity within current phase")
    public AdvancedBehaviorBuilder<T> withinPhase(int minLevel, int maxLevel){
        this.condition(new CustomPhase(minLevel, maxLevel));
        return this;
    }
    @Info(value = "condition of entity's health in current state")
    public AdvancedBehaviorBuilder<T> health(float health, Object object) {
        HealthPoint.Comparator comparator = null;
        if(object instanceof HealthPoint.Comparator c){
            comparator = c;
        } else if(object instanceof String s){
            comparator = HealthPoint.Comparator.valueOf(s.toUpperCase(Locale.ROOT));
        } else Indestructible.LOGGER.warn(object + " can't be recognized");

        this.condition(new HealthPoint(health, comparator));
        return this;
    }
    @Info(value = "condition of entity's stamina in current state")
    public AdvancedBehaviorBuilder<T> stamina(float stamina, Object object){
        HealthPoint.Comparator comparator = null;
        if(object instanceof HealthPoint.Comparator c){
            comparator = c;
        } else if(object instanceof String s){
            comparator = HealthPoint.Comparator.valueOf(s.toUpperCase(Locale.ROOT));
        } else Indestructible.LOGGER.warn(object + " can't be recognized");

        this.condition(new SelfStamina(stamina, comparator));
        return this;
    }
    @Info(value = "condition of entity's target is neutralized")
    public AdvancedBehaviorBuilder<T> targetIsGuardBreak(boolean invert){
        this.condition(new TargetIsGuardBreak(invert));
        return this;
    }
    @Info(value = "condition of entity's target is knockdown")
    public AdvancedBehaviorBuilder<T> targetIsKnockDown(boolean invert){
        this.condition(new TargetIsKnockDown(invert));
        return this;
    }
    @Info(value = "condition of entity's target is using item")
    public AdvancedBehaviorBuilder<T> targetIsUsingItem(boolean isEdible){
        this.condition(new TargetIsUsingItem(isEdible));
        return this;
    }
    @Info(value = "condition of entity's target is in current attack phase")
    public AdvancedBehaviorBuilder<T> targetAttackPhase(int minLevel, int maxLevel){
        this.condition(new TargetWithinState(minLevel, maxLevel));
        return this;
    }
    @Info(value = "condition of y distance of the entity's target is smaller than entity's eye height")
    @Override
    public AdvancedBehaviorBuilder<T> withinEyeHeight() {
        this.condition(new TargetInEyeHeight());
        return this;
    }

    @Info(value = "condition of random value is higher than the given argument")
    @Override
    public AdvancedBehaviorBuilder<T> randomChance(float chance) {
        this.condition(new RandomChance(chance));
        return this;
    }
    @Info(value = "condition of the distance between target and entity are within the given arguments")
    @Override
    public AdvancedBehaviorBuilder<T> withinDistance(double minDistance, double maxDistance) {
        this.condition(new TargetInDistance(minDistance, maxDistance));
        return this;
    }
    @Info(value = "condition of entity's angle towards target is within the given arguments")
    @Override
    public AdvancedBehaviorBuilder<T> withinAngle(double minDegree, double maxDegree) {
        this.condition(new TargetInPov(minDegree, maxDegree));
        return this;
    }
    @Info(value = "condition of entity's Y-axis angle towards target is within the given arguments")
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

    @Info(value = "custom condition, Function<T extend MobPatch, Boolean>")
    @Override
    public AdvancedBehaviorBuilder<T> custom(Function<T, Boolean> customPredicate) {
        this.condition(new CustomCondition<>(customPredicate));
        return this;
    }
    @Info(value = "custom condition, condition<T extend MobPatch> instance")
    @Override
    public AdvancedBehaviorBuilder<T> predicate(Condition<T> predicate) {
        return (AdvancedBehaviorBuilder<T>) super.predicate(predicate);
    }

    @Override
    public AdvancedBehaviorBuilder<T> packetProvider(LivingEntityPatch.AnimationPacketProvider packetProvider) {
        return (AdvancedBehaviorBuilder<T>) super.packetProvider(packetProvider);
    }
}
