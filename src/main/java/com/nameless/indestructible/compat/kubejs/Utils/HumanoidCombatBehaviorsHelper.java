package com.nameless.indestructible.compat.kubejs.Utils;

import com.google.common.collect.Maps;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

import java.util.*;

public class HumanoidCombatBehaviorsHelper {
    private final Map<WeaponCategory, Map<Style, CombatBehaviors.Builder<HumanoidMobPatch<?>>>> Map = new HashMap<>();
    public static HumanoidCombatBehaviorsHelper getHelper(){
        return new HumanoidCombatBehaviorsHelper();
    }
    @Info(value = "define combat behaviors with specific weapon categories and style in map", params = {
            @Param(name = "categories", value = "String[], array of weapon categories' name"),
            @Param(name = "style", value = "String, style name"),
            @Param(name = "builder", value = "CombatBehaviors.Builder<?>, CombatBehaviors.Builder<?>, call EFCombatBehaviors.builder() to get the builder and call method in it to define entity's combat behavior")
    })
    public HumanoidCombatBehaviorsHelper addCombatBehaviors(String[] categories, String style, CombatBehaviors.Builder<HumanoidMobPatch<?>> builder){
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
