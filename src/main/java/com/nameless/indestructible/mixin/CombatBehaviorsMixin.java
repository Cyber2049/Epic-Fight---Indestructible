package com.nameless.indestructible.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

import java.util.List;

@Mixin(CombatBehaviors.class)
public interface CombatBehaviorsMixin<T extends MobPatch<?>> {

    @Accessor(value = "currentBehaviorPointer", remap = false)
    int getCurrentBehaviorPointer();

    @Accessor(value = "behaviorSeriesList", remap = false)
    List<CombatBehaviors.BehaviorSeries<T>> getBehaviorSeriesList();
}
