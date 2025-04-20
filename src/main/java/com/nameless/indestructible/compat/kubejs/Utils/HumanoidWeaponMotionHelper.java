package com.nameless.indestructible.compat.kubejs.Utils;

import com.mojang.datafixers.util.Pair;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

import java.util.*;

public class HumanoidWeaponMotionHelper {
    private final Map<WeaponCategory, Map<Style, Set<Pair<LivingMotion, StaticAnimation>>>> Map = new HashMap<>();
    public static HumanoidWeaponMotionHelper getInstance(){
        return new HumanoidWeaponMotionHelper();
    }


    public HumanoidWeaponMotionHelper addLivingMotions(String[] categories, String style, List<Pair<LivingMotion, StaticAnimation>> list){
        List<WeaponCategory> weaponCategories = new ArrayList<>();
        Arrays.stream(categories).forEach(string -> weaponCategories.add(CapabilityItem.WeaponCategories.valueOf(string.toUpperCase(Locale.ROOT))));
        Map<Style, Set<Pair<LivingMotion, StaticAnimation>>> map = new HashMap<>();
        map.put(CapabilityItem.Styles.valueOf(style.toUpperCase(Locale.ROOT)), new HashSet<>(list));
        weaponCategories.forEach(w -> this.Map.put(w, map));
        return this;
    }
    /*
    public HumanoidWeaponMotionHelper addLivingMotions(Object object1, Object object2, List<Pair<LivingMotion, StaticAnimation>> list){
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
        weaponCategories.forEach(w -> this.Map.get(w).get(style).addAll(list));
      return this;
    }

     */
    public Map<WeaponCategory, Map<Style, Set<Pair<LivingMotion, StaticAnimation>>>> createMap(){
        return this.Map;
    }
}
