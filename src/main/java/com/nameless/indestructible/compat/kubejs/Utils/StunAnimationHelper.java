package com.nameless.indestructible.compat.kubejs.Utils;

import com.google.common.collect.Maps;
import com.nameless.indestructible.main.Indestructible;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.world.damagesource.StunType;

import java.util.Locale;
import java.util.Map;

public class StunAnimationHelper {
    private final Map<StunType, StaticAnimation> stunAnimationMap = Maps.newHashMap();
    public static StunAnimationHelper getHelper() {
        return new StunAnimationHelper();
    }
    public StunAnimationHelper addStunAnimation(Object object1, Object object2){
        StunType stunType = null;
        StaticAnimation animation = null;
        if(object1 instanceof StunType t){
            stunType = t;
        } else if(object1 instanceof String s){
            stunType = StunType.valueOf(s.toUpperCase(Locale.ROOT));
        } else Indestructible.LOGGER.info(object1 + " can't be recognized");

        if(object2 instanceof StaticAnimation a){
            animation = a;
        } else if (object2 instanceof String s) {
            animation = AnimationManager.getInstance().byKeyOrThrow(s);
        } else Indestructible.LOGGER.info(object2 + " can't be recognized");
        if(stunType != null && animation != null)this.stunAnimationMap.put(stunType, animation);
        return this;
    }
    public Map<StunType, StaticAnimation> createMap(){
        return this.stunAnimationMap;
    }
}
