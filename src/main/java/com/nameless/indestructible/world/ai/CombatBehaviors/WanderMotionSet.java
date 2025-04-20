package com.nameless.indestructible.world.ai.CombatBehaviors;

public class WanderMotionSet {
    public int strafing_time;
    public int inaction_time;
    public float forward;
    public float clockwise;
    public WanderMotionSet(int strafing_time, int inaction_time, float forward, float clockwise){
        this.strafing_time = strafing_time;
        this.inaction_time = inaction_time;
        this.forward = forward;
        this.clockwise = clockwise;
    }
    public static WanderMotionSet create(int strafing_time){
        return new WanderMotionSet(strafing_time, strafing_time, 0F, 0F);
    }
    public WanderMotionSet setInactionTime(int inaction_time){
        this.inaction_time = inaction_time;
        return this;
    }
    public WanderMotionSet setForwardDirection(float forward){
        this.forward = forward;
        return this;
    }
    public WanderMotionSet setClockwise(float clockwise){
        this.clockwise = clockwise;
        return this;
    }
}
