package com.nameless.indestructible.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

@Mixin(CombatBehaviors.class)
public interface CombatBehaviorsMixin {

    @Accessor(value = "currentBehaviorPointer", remap = false)
    void setCurrentBehaviorPointer(int currentBehaviorPointer);
}
