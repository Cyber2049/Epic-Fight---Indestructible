package com.nameless.indestructible.world.capability;

import com.google.common.collect.Lists;
import com.nameless.indestructible.api.animation.types.CommandEvent;
import com.nameless.indestructible.data.AdvancedMobpatchReloader;

import java.util.List;

public class AdvancedCustomPatchEventManger {

    private final List<CommandEvent.TimeStampedEvent> timeEvents = Lists.newArrayList();
    private final List<CommandEvent.BiEvent> hitEvents = Lists.newArrayList();
    private final List<CommandEvent.StunEvent> stunEvents = Lists.newArrayList();
    private final List<CommandEvent.BlockedEvent> blockedEvents = Lists.newArrayList();
    public boolean hasTimeEvent(){
        return !this.timeEvents.isEmpty();
    }

    public List<CommandEvent.TimeStampedEvent> getTimeEventList(){
        return this.timeEvents;
    }
    public void addTimeStampedEvent(CommandEvent.TimeStampedEvent event){
        this.timeEvents.add(event);
    }
    public boolean hasHitEvent(){
        return !this.hitEvents.isEmpty();
    }

    public List<CommandEvent.BiEvent> getHitEventList(){
        return this.hitEvents;
    }
    public void addHitEvent(CommandEvent.BiEvent event){
        this.hitEvents.add(event);
    }

    public boolean hasStunEvent(){
        return !this.stunEvents.isEmpty();
    }

    public List<CommandEvent.StunEvent> getStunEvents(){
        return this.stunEvents;
    }

    public boolean hasBlockEvents(){
        return !this.blockedEvents.isEmpty();
    }

    public List<CommandEvent.BlockedEvent> getBlockedEvents(){
        return this.blockedEvents;
    }

    public void initPassiveEvent(AdvancedMobpatchReloader.AdvancedCustomHumanoidMobPatchProvider provider){
        this.stunEvents.clear();
        if(provider.getStunEvent() != null && !provider.getStunEvent().isEmpty()){
            this.stunEvents.addAll(provider.getStunEvent());
        }
        this.blockedEvents.clear();
        if(provider.getBlockedEvent() != null && !provider.getBlockedEvent().isEmpty()){
            this.blockedEvents.addAll(provider.getBlockedEvent());
        }
    }

    public void initAnimationEvent(){
        if (this.hasTimeEvent()) this.timeEvents.clear();
        if (this.hasHitEvent()) this.hitEvents.clear();
    }
}
