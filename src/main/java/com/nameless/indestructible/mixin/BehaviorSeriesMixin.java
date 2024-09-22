package com.nameless.indestructible.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

@Mixin(CombatBehaviors.BehaviorSeries.class)
public interface BehaviorSeriesMixin {

    @Accessor(value = "nextBehaviorPointer", remap = false)
    void setNextBehaviorPointer(int nextBehaviorPointer);
}
