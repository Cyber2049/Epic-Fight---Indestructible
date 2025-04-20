package com.nameless.indestructible.world.capability.Utils;

import com.google.common.collect.Lists;
import com.nameless.indestructible.api.animation.types.LivingEntityPatchEvent;

import java.util.List;

public class AdvancedCustomPatchEventManger {
    private final List<LivingEntityPatchEvent.TimeStampedEvent> timeEvents = Lists.newArrayList();
    private final List<LivingEntityPatchEvent.BiEvent> hitEvents = Lists.newArrayList();
    private final List<LivingEntityPatchEvent.StunEvent> stunEvents = Lists.newArrayList();
    private final List<LivingEntityPatchEvent.BlockedEvent> blockedEvents = Lists.newArrayList();
    public boolean hasTimeEvent(){
        return !this.timeEvents.isEmpty();
    }

    public List<LivingEntityPatchEvent.TimeStampedEvent> getTimeEventList(){
        return this.timeEvents;
    }
    public void addTimeStampedEvent(LivingEntityPatchEvent.TimeStampedEvent event){
        this.timeEvents.add(event);
    }
    public boolean hasHitEvent(){
        return !this.hitEvents.isEmpty();
    }

    public List<LivingEntityPatchEvent.BiEvent> getHitEventList(){
        return this.hitEvents;
    }
    public void addHitEvent(LivingEntityPatchEvent.BiEvent event){
        this.hitEvents.add(event);
    }

    public boolean hasStunEvent(){
        return !this.stunEvents.isEmpty();
    }

    public List<LivingEntityPatchEvent.StunEvent> getStunEvents(){
        return this.stunEvents;
    }

    public boolean hasBlockedEvents(){
        return !this.blockedEvents.isEmpty();
    }

    public List<LivingEntityPatchEvent.BlockedEvent> getBlockedEvents(){
        return this.blockedEvents;
    }
    public void addBlockedEvents(LivingEntityPatchEvent.BlockedEvent event){
        this.blockedEvents.add(event);
    }

    public void initPassiveEvent(List<LivingEntityPatchEvent.StunEvent> eventList){
        this.stunEvents.clear();
        if(eventList != null && !eventList.isEmpty()){
            this.stunEvents.addAll(eventList);
        }
    }

    public void initActiveEvent(){
        if (this.hasTimeEvent()) this.timeEvents.clear();
        if (this.hasHitEvent()) this.hitEvents.clear();
        if (this.hasBlockedEvents()) this.blockedEvents.clear();
    }
}
