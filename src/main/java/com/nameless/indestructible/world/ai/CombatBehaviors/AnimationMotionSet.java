package com.nameless.indestructible.world.ai.CombatBehaviors;

import com.nameless.indestructible.api.animation.types.LivingEntityPatchEvent;
import com.nameless.indestructible.main.Indestructible;
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
        }  else Indestructible.LOGGER.info(object + " can't be recognized");
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

    public AnimationMotionSet setDamageSourceModifier(DamageSourceModifier damageSourceModifier) {
        this.damage_source_modifier = damageSourceModifier;
        return this;
    }

    public AnimationMotionSet addTimeStampedEvent(LivingEntityPatchEvent.TimeStampedEvent event) {
        this.time_events.add(event);
        return this;
    }

    public AnimationMotionSet addTimeStampedEvents(LivingEntityPatchEvent.TimeStampedEvent[] events) {
        this.time_events.addAll(List.of(events));
        return this;
    }

    public AnimationMotionSet addHitEvent(LivingEntityPatchEvent.BiEvent hitEvent) {
        this.hit_events.add(hitEvent);
        return this;
    }

    public AnimationMotionSet addHitEvents(LivingEntityPatchEvent.BiEvent[] hitEvents) {
        this.hit_events.addAll(List.of(hitEvents));
        return this;
    }

    public AnimationMotionSet addBlockedEvent(LivingEntityPatchEvent.BlockedEvent blockedEvent) {
        this.blocked_events.add(blockedEvent);
        return this;
    }

    public AnimationMotionSet addBlockedEvents(LivingEntityPatchEvent.BlockedEvent[] blockedEvents) {
        this.blocked_events.addAll(List.of(blockedEvents));
        return this;
    }
}
