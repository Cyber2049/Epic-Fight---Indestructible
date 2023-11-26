package com.nameless.indestructible.utils;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.CustomHumanoidMobPatch;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
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
            EntityPatch<?> entityPatch = mobpatch.getTarget().getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null);
            if(entityPatch == null || !(entityPatch instanceof LivingEntityPatch<?> targetpatch)) return false;
            boolean targetisguardbreak = targetpatch.getAnimator().getPlayerFor(null).getAnimation() == Animations.BIPED_COMMON_NEUTRALIZED || targetpatch.getAnimator().getPlayerFor(null).getAnimation() == Animations.GREATSWORD_GUARD_BREAK;
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
            EntityPatch<?> entityPatch = mobpatch.getTarget().getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null);
            if(entityPatch == null || !(entityPatch instanceof LivingEntityPatch<?> targetpatch)) return false;
            boolean targetisknockdown = targetpatch.getEntityState().knockDown();
            if (!this.invert) {
                return targetisknockdown;
            } else {
                return !targetisknockdown;
            }
        }
    }

    public static class TargetWithinState<T extends MobPatch<?>> extends CombatBehaviors.BehaviorPredicate<T> {
        private final double minLevel;
        private final double maxLevel;

        public TargetWithinState(double minLevel, double maxLevel) {
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
        }

        public boolean test(T mobpatch) {
            EntityPatch<?> entityPatch = mobpatch.getTarget().getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null);
            if(entityPatch == null || !(entityPatch instanceof LivingEntityPatch<?> targetpatch)) return false;
            int level = targetpatch.getEntityState().getLevel();
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
            if(!(mobpatch instanceof CustomHumanoidMobPatch<?>)) return false;
            float stamina = ((CustomHumanoidMobPatchUtils) mobpatch).getStamina();
            float maxstamina = ((CustomHumanoidMobPatchUtils) mobpatch).getMaxStamina();
            return switch (this.comparator) {
                case LESS_ABSOLUTE -> this.value > stamina;
                case GREATER_ABSOLUTE -> this.value < stamina;
                case LESS_RATIO -> this.value > stamina / maxstamina;
                case GREATER_RATIO -> this.value < stamina / maxstamina;
            };

        }
    }

    public static class TargetIsUsingItem<T extends MobPatch<?>> extends CombatBehaviors.BehaviorPredicate<T> {
        private final boolean isEdible;
        public TargetIsUsingItem(boolean isEdible){
            this.isEdible = isEdible;
        }
        public boolean test(T mobpatch) {
            LivingEntity target = mobpatch.getTarget();
            ItemStack item = target.getUseItem();
            if(target.isUsingItem() && isEdible){
                return  item.getItem() instanceof PotionItem || item.getItem().isEdible();
            } else {
                return !(item.getItem() instanceof PotionItem || item.getItem().isEdible());
            }
        }
    }

    public static class IsNotBlocking<T extends MobPatch<?>> extends CombatBehaviors.BehaviorPredicate<T> {
        public IsNotBlocking(){
        }
        public boolean test(T mobpatch) {
            if(mobpatch instanceof CustomHumanoidMobPatch){
               return !(((CustomHumanoidMobPatchUtils)mobpatch).getBlockTick() > 0);
            } else {
                return true;
            }
        }
    }

}
