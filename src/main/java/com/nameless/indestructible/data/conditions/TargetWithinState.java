package com.nameless.indestructible.data.conditions;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.List;

public class TargetWithinState implements Condition<LivingEntityPatch<?>> {
    private int minLevel;
    private int maxLevel;
    public TargetWithinState(int minLevel, int maxLevel) {
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
    }

    public TargetWithinState() {

    }

    @Override
    public TargetWithinState read(CompoundTag tag) {
        if (!tag.contains("min")) {
            throw new IllegalArgumentException("attack level condition error: min distance not specified!");
        }

        if (!tag.contains("max")) {
            throw new IllegalArgumentException("attack level condition error: max distance not specified!");
        }

        this.minLevel = tag.getInt("min");
        this.maxLevel = tag.getInt("max");

        return this;
    }

    @Override
    public CompoundTag serializePredicate() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("min", this.minLevel);
        tag.putInt("max", this.maxLevel);

        return tag;
    }

    @Override
    public boolean predicate(LivingEntityPatch<?> target) {
        LivingEntityPatch<?> tartgetpatch = EpicFightCapabilities.getEntityPatch(target.getTarget(), LivingEntityPatch.class);
        if(tartgetpatch == null) return false;
        int level = tartgetpatch.getEntityState().getLevel();
        return this.minLevel <= level && level <= this.maxLevel;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<ParameterEditor> getAcceptingParameters(Screen screen) {
        return null;
    }
}
