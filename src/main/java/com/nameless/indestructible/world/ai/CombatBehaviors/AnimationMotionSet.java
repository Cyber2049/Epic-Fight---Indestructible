package com.nameless.indestructible.world.ai.CombatBehaviors;

import com.nameless.indestructible.api.animation.types.LivingEntityPatchEvent;
import com.nameless.indestructible.main.Indestructible;
import dev.latvian.mods.kubejs.typings.Info;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;

import java.util.ArrayList;
import java.util.List;

public class AnimationMotionSet {
    public StaticAnimation animation;
    public float convert_time;
    public float speed;
    public float stamina;
    public DamageSourceModifier damage_source_modifier;
    public List<LivingEntityPatchEvent.TimeStampedEvent> time_events = new ArrayList<>();
    public List<LivingEntityPatchEvent.BiEvent> hit_events = new ArrayList<>();
    public List<LivingEntityPatchEvent.BlockedEvent> blocked_events = new ArrayList<>();

    public AnimationMotionSet(StaticAnimation animation, float convertTime, float speed, float stamina) {
        this.animation = animation;
        this.convert_time = convertTime;
        this.speed = speed;
        this.stamina = stamina;
    }

    public static AnimationMotionSet create(Object object) {
        StaticAnimation animation = null;
        if(object instanceof StaticAnimation a){
            animation = a;
        } else if(object instanceof String s){
            animation = AnimationManager.getInstance().byKeyOrThrow(s);
        }  else Indestructible.LOGGER.warn(object + " can't be recognized");
        return new AnimationMotionSet(animation, 0F, 1F, 0F);
    }

    public AnimationMotionSet setSpeed(float speed) {
        this.speed = speed;
        return this;
    }

    public AnimationMotionSet setConvertTime(float convertTime) {
        this.convert_time = convertTime;
        return this;
    }

    public AnimationMotionSet setStaminaCost(float stamina) {
        this.stamina = stamina;
        return this;
    }

    @Info(value = "DamageSourceModifier, call DamageSourceModifier.create() to create a DamageSourceModifier, and call method in it to define its properties")
    public AnimationMotionSet setDamageSourceModifier(DamageSourceModifier damageSourceModifier) {
        this.damage_source_modifier = damageSourceModifier;
        return this;
    }
    @Info(value = "add TimeStampedEvent which will execute by time one by one of this motionSet, call LivingEntityPatchEvent.createTimeStampedEvent() to create event")
    public AnimationMotionSet addTimeStampedEvent(LivingEntityPatchEvent.TimeStampedEvent event) {
        this.time_events.add(event);
        return this;
    }
    @Info(value = "add TimeStampedEvents which will execute by time by array of this motionSet, call LivingEntityPatchEvent.createTimeStampedEvent() to create event")
    public AnimationMotionSet addTimeStampedEvents(LivingEntityPatchEvent.TimeStampedEvent[] events) {
        this.time_events.addAll(List.of(events));
        return this;
    }
    @Info(value = "add hit event which will execute when entity hit target one by one of this motionSet, call LivingEntityPatchEvent.createBiEvent() to create event")
    public AnimationMotionSet addHitEvent(LivingEntityPatchEvent.BiEvent hitEvent) {
        this.hit_events.add(hitEvent);
        return this;
    }
    @Info(value = "add hit events which will execute when entity hit target by array of this motionSet, call LivingEntityPatchEvent.createBiEvent() to create event")
    public AnimationMotionSet addHitEvents(LivingEntityPatchEvent.BiEvent[] hitEvents) {
        this.hit_events.addAll(List.of(hitEvents));
        return this;
    }

    @Info(value = "add blocked event which will execute when entity attack being blocked one by one of this motionSet, call LivingEntityPatchEvent.createBlockedEvent() to create event")
    public AnimationMotionSet addBlockedEvent(LivingEntityPatchEvent.BlockedEvent blockedEvent) {
        this.blocked_events.add(blockedEvent);
        return this;
    }

    @Info(value = "add blocked events which will execute when entity attack being blocked by array of this motionSet, call LivingEntityPatchEvent.createBlockedEvent() to create event")
    public AnimationMotionSet addBlockedEvents(LivingEntityPatchEvent.BlockedEvent[] blockedEvents) {
        this.blocked_events.addAll(List.of(blockedEvents));
        return this;
    }
}
