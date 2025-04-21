package com.nameless.indestructible.compat.kubejs.Utils;

import com.google.common.collect.Maps;
import com.nameless.indestructible.world.ai.CombatBehaviors.GuardMotion;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

import java.util.*;

public class HumanoidGuardMotionHelper {
    private final Map<WeaponCategory, Map<Style, GuardMotion>> Map = new HashMap<>();
    public static HumanoidGuardMotionHelper getHelper(){
        return new HumanoidGuardMotionHelper();
    }
    @Info(value = "define guard motion with specific weapon categories and style in map", params = {
            @Param(name = "categories", value = "String[], array of weapon categories name"), @Param(name = "style", value = "String, style name"),
            @Param(name = "motion", value = "GuardMotion, call GuardMotion.create() to return a guard motion, and call the method in it to define its properties")
    })
    public HumanoidGuardMotionHelper addGuardMotions(String[] categories, String style, GuardMotion motion){
        List<WeaponCategory> weaponCategories = new ArrayList<>();
        Arrays.stream(categories).forEach(string -> weaponCategories.add(CapabilityItem.WeaponCategories.valueOf(string.toUpperCase(Locale.ROOT))));
        Map<Style, GuardMotion> map = Maps.newHashMap();
        map.put(CapabilityItem.Styles.valueOf(style.toUpperCase(Locale.ROOT)), motion);
        weaponCategories.forEach(w -> this.Map.put(w, map));
        return this;
    }
    /*
    public HumanoidGuardMotionHelper addGuardMotions(Object object1, Object object2, GuardMotion motion){
        List<WeaponCategory> weaponCategories = new ArrayList<>();
        Style style;
        if(object1 instanceof String[] strings){
            Arrays.stream(strings).forEach(string -> weaponCategories.add(CapabilityItem.WeaponCategories.valueOf(string.toUpperCase(Locale.ROOT))));
        } else if (object1 instanceof WeaponCategory[] categories){
            weaponCategories.addAll(Arrays.asList(categories));
        } else throw new IllegalArgumentException(object1 + " can't be recognized");
        if (object2 instanceof String string) {
            style = CapabilityItem.Styles.valueOf(string);
        } else if (object2 instanceof Style s){
            style = s;
        } else throw new IllegalArgumentException(object2 + " can't be recognized");
        weaponCategories.forEach(w -> this.Map.get(w).put(style, motion));
      return this;
    }

     */
    public Map<WeaponCategory, Map<Style, GuardMotion>> createMap(){
        return this.Map;
    }
}
