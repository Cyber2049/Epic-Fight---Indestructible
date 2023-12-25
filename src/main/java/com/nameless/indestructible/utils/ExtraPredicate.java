package com.nameless.indestructible.utils;

import com.nameless.indestructible.world.capability.AdvancedCustomHumanoidMobPatch;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

public class ExtraPredicate {
    public static class TargetIsGuardBreak<T extends MobPatch<?>> extends CombatBehaviors.BehaviorPredicate<T> {
        private final boolean invert;
        public TargetIsGuardBreak(boolean invert){
            this.invert = invert;
        }
        public boolean test(T mobpatch) {
            LivingEntityPatch<?> tartgetpatch = (LivingEntityPatch<?>)mobpatch.getTarget().getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null);
            if(tartgetpatch == null) return false;
            boolean targetisguardbreak = tartgetpatch.getAnimator().getPlayerFor(null).getAnimation() == Animations.BIPED_COMMON_NEUTRALIZED || tartgetpatch.getAnimator().getPlayerFor(null).getAnimation() == Animations.GREATSWORD_GUARD_BREAK;
            if (!this.invert) {
                return targetisguardbreak;
            } else {
                return !targetisguardbreak;
            }
        }
    }

    public static class TargetIsKnockDown<T extends MobPatch<?>> extends CombatBehaviors.BehaviorPredicate<T> {
        private final boolean invert;
        public TargetIsKnockDown(boolean invert){
            this.invert = invert;
        }
        public boolean test(T mobpatch) {
            LivingEntityPatch<?> tartgetpatch = (LivingEntityPatch<?>)mobpatch.getTarget().getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null);
            if(tartgetpatch == null) return false;
            boolean targetisknockdown = tartgetpatch.getEntityState().knockDown();
            if (!this.invert) {
                return targetisknockdown;
            } else {
                return !targetisknockdown;
            }
        }
    }

    public static class TargetWithinState<T extends MobPatch<?>> extends CombatBehaviors.BehaviorPredicate<T> {
        private final int minLevel;
        private final int maxLevel;

        public TargetWithinState(int minLevel, int maxLevel) {
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
        }

        public boolean test(T mobpatch) {
            LivingEntityPatch<?> tartgetpatch = (LivingEntityPatch<?>)mobpatch.getTarget().getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null);
            if(tartgetpatch == null) return false;
            int level = tartgetpatch.getEntityState().getLevel();
            return this.minLevel <= level && level <= this.maxLevel;
        }
    }

    public static class SelfStamina<T extends MobPatch<?>> extends CombatBehaviors.BehaviorPredicate<T> {
        private final float value;
        private final CombatBehaviors.Health.Comparator comparator;

        public SelfStamina(float value, CombatBehaviors.Health.Comparator comparator) {
            this.value = value;
            this.comparator = comparator;
        }

        public boolean test(T mobpatch) {
            if(!(mobpatch instanceof AdvancedCustomHumanoidMobPatch)) return false;
            float stamina = ((AdvancedCustomHumanoidMobPatch<?>) mobpatch).getStamina();
            float maxstamina = ((AdvancedCustomHumanoidMobPatch<?>) mobpatch).getMaxStamina();
            switch (this.comparator) {
                case LESS_ABSOLUTE:
                    return this.value > stamina;
                case GREATER_ABSOLUTE:
                    return this.value < stamina;
                case LESS_RATIO:
                    return this.value > stamina / maxstamina;
                case GREATER_RATIO:
                    return this.value < stamina / maxstamina;
            }

            return true;
        }
    }

    public static class TargetIsUsingItem<T extends MobPatch<?>> extends CombatBehaviors.BehaviorPredicate<T> {
        private final boolean isEdible;
        public TargetIsUsingItem(boolean isEdible){
            this.isEdible = isEdible;
        }
        public boolean test(T mobpatch) {
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
    }

    public static class Phase<T extends MobPatch<?>> extends CombatBehaviors.BehaviorPredicate<T> {
        private final int minLevel;
        private final int maxLevel;

        public Phase(int minLevel, int maxLevel) {
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
        }

        public boolean test(T mobpatch) {
            if(mobpatch instanceof AdvancedCustomHumanoidMobPatch<?> advancedCustomHumanoidMobPatch){
                int phase = advancedCustomHumanoidMobPatch.getPhase();
                return this.minLevel <= phase && phase <= this.maxLevel;
            }
            return false;
        }
    }

}
