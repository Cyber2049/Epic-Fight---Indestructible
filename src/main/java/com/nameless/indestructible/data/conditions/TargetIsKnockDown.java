package com.nameless.indestructible.data.conditions;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.List;

public class TargetIsKnockDown  implements Condition<LivingEntityPatch<?>> {
    private boolean invert;
    public TargetIsKnockDown(boolean invert){
        this.invert = invert;
    }

    public TargetIsKnockDown() {

    }

    @Override
    public TargetIsKnockDown read(CompoundTag tag) {
        this.invert = tag.contains("invert") && tag.getBoolean("invert");

        return this;
    }

    @Override
    public CompoundTag serializePredicate() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("invert", this.invert);

        return tag;
    }

    @Override
    public boolean predicate(LivingEntityPatch<?> target) {
        LivingEntityPatch<?> tartgetpatch = EpicFightCapabilities.getEntityPatch(target.getTarget(), LivingEntityPatch.class);
        if(tartgetpatch == null) return !this.invert;
        boolean targetisknockdown = tartgetpatch.getEntityState().knockDown();
        if (!this.invert) {
            return targetisknockdown;
        } else {
            return !targetisknockdown;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<ParameterEditor> getAcceptingParameters(Screen screen) {
        return null;
    }
}
