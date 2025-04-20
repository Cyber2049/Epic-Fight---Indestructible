package com.nameless.indestructible.compat.kubejs.Utils;

import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

import java.util.*;

public class HumanoidCombatBehaviorsHelperHelper {
    private final Map<WeaponCategory, Map<Style, CombatBehaviors.Builder<HumanoidMobPatch<?>>>> Map = new HashMap<>();
    public static HumanoidCombatBehaviorsHelperHelper getInstance(){
        return new HumanoidCombatBehaviorsHelperHelper();
    }
    public HumanoidCombatBehaviorsHelperHelper addCombatBehaviors(String[] categories, String style, CombatBehaviors.Builder<HumanoidMobPatch<?>> builder){
        List<WeaponCategory> weaponCategories = new ArrayList<>();
        Arrays.stream(categories).forEach(string -> weaponCategories.add(CapabilityItem.WeaponCategories.valueOf(string.toUpperCase(Locale.ROOT))));
        weaponCategories.forEach(w -> this.Map.get(w).put(CapabilityItem.Styles.valueOf(style.toUpperCase(Locale.ROOT)), builder));
      return this;
    }
    public Map<WeaponCategory, Map<Style, CombatBehaviors.Builder<HumanoidMobPatch<?>>>> createMap(){
        return this.Map;
    }
}
