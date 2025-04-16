package com.nameless.indestructible.data.conditions;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.types.LongHitAnimation;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.List;

import static com.nameless.indestructible.main.Indestructible.NEUTRALIZE_ANIMATIONS;

public class TargetIsGuardBreak implements Condition<LivingEntityPatch<?>> {
    private boolean invert;
    public TargetIsGuardBreak(boolean invert){
        this.invert = invert;
    }

    public TargetIsGuardBreak() {

    }

    @Override
    public TargetIsGuardBreak read(CompoundTag tag) {
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
        boolean targetisguardbreak = tartgetpatch.getEntityState().hurtLevel() > 1 && tartgetpatch.getAnimator().getPlayerFor(null).getAnimation() instanceof LongHitAnimation animation && animation.in(NEUTRALIZE_ANIMATIONS);
        if (!this.invert) {
            return targetisguardbreak;
        } else {
            return !targetisguardbreak;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<ParameterEditor> getAcceptingParameters(Screen screen) {
        return null;
    }
}
