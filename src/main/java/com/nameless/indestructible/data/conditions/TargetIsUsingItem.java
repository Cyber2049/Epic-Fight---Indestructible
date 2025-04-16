package com.nameless.indestructible.data.conditions;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.List;

public class TargetIsUsingItem implements Condition<LivingEntityPatch<?>> {
    private boolean isEdible;
    public TargetIsUsingItem(boolean isEdible){
        this.isEdible = isEdible;
    }

    public TargetIsUsingItem() {

    }

    @Override
    public TargetIsUsingItem read(CompoundTag tag) {
        this.isEdible = tag.contains("edible") && tag.getBoolean("edible");

        return this;
    }

    @Override
    public CompoundTag serializePredicate() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("edible", this.isEdible);

        return tag;
    }

    @Override
    public boolean predicate(LivingEntityPatch<?> mobpatch) {
        LivingEntity target = mobpatch.getTarget();
        if (target.isUsingItem()) {
            ItemStack item = target.getUseItem();
            if (isEdible) {
                return item.getItem() instanceof PotionItem || item.getItem().isEdible();
            } else {
                return !(item.getItem() instanceof PotionItem || item.getItem().isEdible());
            }
        }
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<ParameterEditor> getAcceptingParameters(Screen screen) {
        return null;
    }
}
