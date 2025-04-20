package com.nameless.indestructible.compat.kubejs;

import com.nameless.indestructible.api.animation.types.LivingEntityPatchEvent;
import com.nameless.indestructible.compat.kubejs.Utils.*;
import com.nameless.indestructible.data.JSCustomHumanoidMobPatchProviderBuilder;
import com.nameless.indestructible.world.ai.CombatBehaviors.*;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

public class PatchJSPlugin extends KubeJSPlugin {
    public static final EventGroup GROUP = EventGroup.of("IndestructibleEvents");
    public static final EventHandler REGISTRY = GROUP.server("PatchRegistry", () -> AdvancedMobPatchProviderEvent.class);
    @Override
    public void registerEvents(){
        GROUP.register();
    }
    @Override
    public void registerBindings(BindingsEvent event) {
        //provider builder
        event.add("JSHumanoidBuilder", JSCustomHumanoidMobPatchProviderBuilder.class);
        //helper
        event.add("AttributeMapHelper", AttributeMapHelper.class);
        event.add("LivingMotionHelper", DefaultAnimationHelper.class);
        event.add("WeaponMotionHelper", HumanoidWeaponMotionHelper.class);
        event.add("GuardMotionHelper", HumanoidGuardMotionHelper.class);
        event.add("CombatBehaviorHelper", HumanoidCombatBehaviorsHelperHelper.class);
        event.add("StunAnimationHelper", StunAnimationHelper.class);

        event.add("EFCombatBehaviors", CombatBehaviors.class);
        event.add("EFBehaviorSeries", CombatBehaviors.BehaviorSeries.class);
        event.add("EFBehavior", AdvancedBehavior.class);
        event.add("EFBehaviorUtils", BehaviorsUtils.class);

        event.add("AnimationMotionSet", AnimationMotionSet.class);
        event.add("GuardMotionSet", GuardMotionSet.class);
        event.add("WanderMotionSet", WanderMotionSet.class);
        event.add("GuardMotion", GuardMotion.class);
        event.add("DamageSourceModifier", DamageSourceModifier.class);
        event.add("CounterMotion", CounterMotion.class);

        //event
        event.add("PatchEvent", LivingEntityPatchEvent.class);

        event.add("JSCustomHumanoidPatch", JsCustomHumanoidMobPatch.class);
        //event.add("JSCustomMobPatch", AdvancedCustomMobPatch.class);
    }


}
