package com.nameless.indestructible.data.conditions;

import com.nameless.indestructible.world.capability.Utils.IAdvancedCapability;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.List;

public class CustomPhase implements Condition<LivingEntityPatch<?>> {
    private int minLevel;
    private int maxLevel;

    public CustomPhase(int minLevel, int maxLevel) {
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
    }

    public CustomPhase() {

    }

    @Override
    public CustomPhase read(CompoundTag tag) {
        if (!tag.contains("min")) {
            throw new IllegalArgumentException("custom phase condition error: min distance not specified!");
        }

        if (!tag.contains("max")) {
            throw new IllegalArgumentException("custom phase condition error: max distance not specified!");
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
        if(target instanceof IAdvancedCapability iac){
            int phase = iac.getPhase();
            return this.minLevel <= phase && phase <= this.maxLevel;
        }
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<ParameterEditor> getAcceptingParameters(Screen screen) {
        return null;
    }
}
