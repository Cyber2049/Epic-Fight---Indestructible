package com.nameless.indestructible.compat.kubejs.Utils;

import com.mojang.datafixers.util.Pair;
import com.nameless.indestructible.main.Indestructible;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DefaultAnimationHelper {
    private final List<Pair<LivingMotion, StaticAnimation>> living_animation_list = new ArrayList<>();
    public static DefaultAnimationHelper getHelper(){
        return new DefaultAnimationHelper();
    }
    public DefaultAnimationHelper addLivingAnimation(Object object1, Object object2){
        LivingMotion livingMotion = null;
        StaticAnimation animation = null;

        if(object1 instanceof String s){
            livingMotion = LivingMotions.valueOf(s.toUpperCase(Locale.ROOT));
        } else if(object1 instanceof LivingMotions l){
            livingMotion = l;
        } else Indestructible.LOGGER.info(object1 + " can't be recognized");

        if(object2 instanceof String s){
            animation = AnimationManager.getInstance().byKeyOrThrow(s);
        } else if (object2 instanceof StaticAnimation a){
            animation = a;
        } else Indestructible.LOGGER.info(object2 + " can't be recognized");

        if(livingMotion != null && animation != null) this.living_animation_list.add(Pair.of(livingMotion, animation));
        return this;
    }
    public List<Pair<LivingMotion, StaticAnimation>> createList(){
        return this.living_animation_list;
    }

}
