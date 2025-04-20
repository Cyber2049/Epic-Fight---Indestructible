package com.nameless.indestructible.compat.kubejs.Utils;

import com.google.common.collect.Maps;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

import java.util.*;

public class HumanoidCombatBehaviorsHelperHelper {
    private final Map<WeaponCategory, Map<Style, CombatBehaviors.Builder<HumanoidMobPatch<?>>>> Map = new HashMap<>();
    public static HumanoidCombatBehaviorsHelperHelper getHelper(){
        return new HumanoidCombatBehaviorsHelperHelper();
    }
    public HumanoidCombatBehaviorsHelperHelper addCombatBehaviors(String[] categories, String style, CombatBehaviors.Builder<HumanoidMobPatch<?>> builder){
        List<WeaponCategory> weaponCategories = new ArrayList<>();
        Arrays.stream(categories).forEach(string -> weaponCategories.add(CapabilityItem.WeaponCategories.valueOf(string.toUpperCase(Locale.ROOT))));
        Map<Style, CombatBehaviors.Builder<HumanoidMobPatch<?>>> map = Maps.newHashMap();
        map.put(CapabilityItem.Styles.valueOf(style.toUpperCase(Locale.ROOT)), builder);
        weaponCategories.forEach(w -> this.Map.put(w, map));
        return this;
    }
    public Map<WeaponCategory, Map<Style, CombatBehaviors.Builder<HumanoidMobPatch<?>>>> createMap(){
        return this.Map;
    }
}
