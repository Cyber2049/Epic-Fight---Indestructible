package com.nameless.indestructible.world.ai.goal;

import com.nameless.indestructible.world.capability.Utils.IAdvancedCapability;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ProjectileWeaponItem;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;

public class GuardGoal<T extends MobPatch<?>> extends Goal {
    private final T mobPatch;
    private final float radiusSqr;
    private int targetInactiontime = -1;

    public GuardGoal(T mobPatch, float radius) {
        this.mobPatch = mobPatch;
        this.radiusSqr = radius  * radius;
    }

    public boolean canUse() {
        return this.checkTargetValid() && ((IAdvancedCapability)mobPatch).isBlocking();
    }
    public boolean canContinueToUse() {
        return this.canUse() && !this.targetInaction();
    }

    public void start() {
        super.start();
        this.targetInactiontime = -1;
        ((IAdvancedCapability)mobPatch).resetActionTick();
    }

    public void stop() {
        IAdvancedCapability iac = (IAdvancedCapability)mobPatch;
        super.stop();
        this.targetInactiontime = -1;
        iac.setBlocking(false);
        mobPatch.getAnimator().resetLivingAnimations();
    }

    private boolean checkTargetValid() {
        LivingEntity livingentity = mobPatch.getTarget();

        if (livingentity == null) {
            return false;
        } else if (!livingentity.isAlive()) {
            return false;
        } else {
            return !(livingentity instanceof Player) || !livingentity.isSpectator() && !((Player) livingentity).isCreative();
        }
    }

    private boolean withinDistance(){
        LivingEntity target = mobPatch.getTarget();
        return mobPatch.getOriginal().distanceToSqr(target.getX(), target.getY(), target.getZ()) <= (double)this.radiusSqr;
    }

    private boolean targetInaction(){
        LivingEntityPatch<?> target = EpicFightCapabilities.getEntityPatch(mobPatch.getTarget(),LivingEntityPatch.class);
        if (target == null){
            return true;
        } else {
            return targetInactiontime > ((IAdvancedCapability) mobPatch).getBlockTick();
        }
    }

    public void tick() {
        LivingEntity target = mobPatch.getTarget();
        if (target != null) {
            LivingEntityPatch<?> targetPatch = EpicFightCapabilities.getEntityPatch(target, LivingEntityPatch.class);
            if (targetPatch != null){
                int phase = targetPatch.getEntityState().getLevel();
                if(this.withinDistance() && phase > 0 && phase < 3) {
                    this.targetInactiontime = 0;
                } else if (((IAdvancedCapability)mobPatch).canBlockProjectile() && target.getUseItem().getItem() instanceof ProjectileWeaponItem && target.isUsingItem()) {
                    this.targetInactiontime = 0;
                } else {
                    ++this.targetInactiontime;
                }
            }
        }
    }
}
