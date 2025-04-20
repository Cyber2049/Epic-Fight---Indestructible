package com.nameless.indestructible.world.ai.CombatBehaviors;

import com.nameless.indestructible.gameasset.GuardAnimations;
import com.nameless.indestructible.main.Indestructible;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;

public class CounterMotion {
    public StaticAnimation counter;
    public float cost;
    public float chance;
    public float speed;
    public float convert_time;
    public boolean cancel_block;

    public CounterMotion(StaticAnimation counter, float cost, float chance, float speed, float convert_time, boolean cancel_block) {
        this.counter = counter;
        this.cost = cost;
        this.chance = chance;
        this.speed = speed;
        this.convert_time = convert_time;
        this.cancel_block = cancel_block;
    }
    public static CounterMotion create(){
        return new CounterMotion(GuardAnimations.MOB_COUNTER_ATTACK, 3F, 0.3F, 1F,0F, true);
    }

    public CounterMotion setCounterAnimation(Object object){
        StaticAnimation animation = null;
        if(object instanceof StaticAnimation a){
            animation = a;
        } else if(object instanceof String s){
            animation = AnimationManager.getInstance().byKeyOrThrow(s);
        } Indestructible.LOGGER.info(object + " can't be recognized");
        this.counter = animation;
        return this;
    }
    public CounterMotion setCost(float cost){
        this.cost = cost;
        return this;
    }
    public CounterMotion setChance(float chance){
        this.chance = chance;
        return this;
    }
    public CounterMotion setSpeed(float speed){
        this.speed = speed;
        return this;
    }
    public CounterMotion setConvertTime(float convert_time){
        this.convert_time = convert_time;
        return this;
    }

    public CounterMotion cancelBlock(boolean cancel){
        this.cancel_block = cancel;
        return this;
    }

}
