package com.nameless.indestructible.world.ai.CombatBehaviors;

import com.nameless.indestructible.gameasset.GuardAnimations;
import com.nameless.indestructible.main.Indestructible;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.gameasset.Animations;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuardMotion {
    public StaticAnimation guard_animation;
    public boolean can_block_projectile;
    public float cost;
    public float parry_cost;
    public List<StaticAnimation> parry_animation;
    public Boolean[] changeTag = {false, false, false, false, false};

    public GuardMotion(StaticAnimation guard_animation, boolean canBlockProjectile, float cost, float parry_cost, @Nullable List<StaticAnimation> parry_animation) {
        this.guard_animation = guard_animation;
        this.can_block_projectile = canBlockProjectile;
        this.cost = cost;
        this.parry_cost = parry_cost;
        this.parry_animation = parry_animation;
    }

    public GuardMotion(StaticAnimation guard_animation, boolean canBlockProjectile, float cost) {
        this.guard_animation = guard_animation;
        this.can_block_projectile = canBlockProjectile;
        this.cost = cost;
        this.parry_cost = 0.5F;
        this.parry_animation = List.of(Animations.LONGSWORD_GUARD_ACTIVE_HIT1, Animations.LONGSWORD_GUARD_ACTIVE_HIT2);
    }
    public static GuardMotion create(){
        return new GuardMotion(GuardAnimations.MOB_SWORD_GUARD, false, 0F);
    }
    public GuardMotion setGuardAnimation(Object object){
        StaticAnimation animation = null;
        if(object instanceof StaticAnimation a){
            animation = a;
        } else if(object instanceof String s){
            animation = AnimationManager.getInstance().byKeyOrThrow(s);
        } else Indestructible.LOGGER.info(object + " can't be recognized");
        this.guard_animation = animation;
        this.changeTag[0] = true;
        return this;
    }
    public GuardMotion canBlockProjectile(boolean canBlockProjectile){
        this.can_block_projectile = canBlockProjectile;
        this.changeTag[1] = true;
        return this;
    }
    public GuardMotion setCost(float cost){
        this.cost = cost;
        this.changeTag[2] = true;
        return this;
    }
    public GuardMotion setParryCost(float cost){
        this.parry_cost = cost;
        this.changeTag[3] = true;
        return this;
    }
    public GuardMotion setParryAnimations(Object[] objects){
        List<StaticAnimation> animations = new ArrayList<>();
        if(objects instanceof StaticAnimation[] a){
            animations.addAll(List.of(a));
        } else if(objects instanceof String[] s){
            Arrays.stream(s).forEach(string -> animations.add(AnimationManager.getInstance().byKeyOrThrow(string)));
        }
        this.parry_animation = animations;
        this.changeTag[4] = true;
        return this;
    }
}
