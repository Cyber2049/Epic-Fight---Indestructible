package com.nameless.indestructible.world.ai.CombatBehaviors;

public class GuardMotionSet {
    public int guard_time;
    public int parry_times;
    public int stun_immunity_time;
    public CounterMotion counter_motion = null;
    public GuardMotion guard_motion = null;
    public GuardMotionSet(int guard_time, int parry_times, int stun_immunity_time){
        this.guard_time = guard_time;
        this.parry_times = parry_times;
        this.stun_immunity_time = stun_immunity_time;
    }
    public GuardMotionSet create(int guard_time){
        return new GuardMotionSet(guard_time, 0, 0);
    }
    public GuardMotionSet setParryTimes(int parry_times){
        this.parry_times = parry_times;
        return this;
    }
    public GuardMotionSet setStunImmunityTime(int time){
        this.stun_immunity_time = time;
        return this;
    }
    public GuardMotionSet setCounterMotion(CounterMotion motion){
        this.counter_motion = motion;
        return this;
    }
    public GuardMotionSet setSpecificGuardMotion(GuardMotion motion){
        this.guard_motion = motion;
        return this;
    }
}
