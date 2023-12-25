package com.nameless.indestructible.world.ai.goal;

import com.nameless.indestructible.world.capability.AdvancedCustomHumanoidMobPatch;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class GuardGoal<T extends AdvancedCustomHumanoidMobPatch<?>> extends Goal {
    private final T mobpatch;
    private final float radiusSqr;
    private int targetInactiontime = -1;

    public GuardGoal(T customHumanoidMobPatch, float radius) {
        this.mobpatch = customHumanoidMobPatch;
        this.radiusSqr = radius  * radius;
    }

    public boolean canUse() {
        return this.checkTargetValid() && this.mobpatch.getBlockTick() > 0;
    }
    public boolean canContinueToUse() {
        return this.canUse() && !this.targetInaction();
    }

    public void start() {
        super.start();
        this.targetInactiontime = -1;
        mobpatch.setBlocking(true);
        this.mobpatch.resetActionTick();
    }

    public void stop() {
        super.stop();
        this.targetInactiontime = -1;
        mobpatch.setBlockTick(0);
        mobpatch.setBlocking(false);
        mobpatch.getAnimator().resetLivingAnimations();
    }

    private boolean checkTargetValid() {
        LivingEntity livingentity = this.mobpatch.getTarget();

        if (livingentity == null) {
            return false;
        } else if (!livingentity.isAlive()) {
            return false;
        } else {
            return !(livingentity instanceof Player) || !livingentity.isSpectator() && !((Player) livingentity).isCreative();
        }
    }

    private boolean keepDistance(){
        LivingEntity target = this.mobpatch.getTarget();
        return this.mobpatch.getOriginal().distanceToSqr(target.getX(), target.getY(), target.getZ()) <= (double)this.radiusSqr;
    }

    private boolean targetInaction(){
        LivingEntityPatch<?> target = (LivingEntityPatch<?>) this.mobpatch.getTarget().getCapability(EpicFightCapabilities.CAPABILITY_ENTITY,null).orElse(null);
        if (target == null){
            return true;
        } else {
            return targetInactiontime > 30;
        }
    }

    public void tick() {
        LivingEntity target = this.mobpatch.getTarget();
        int blocktick = mobpatch.getBlockTick();
        if(!mobpatch.isBlocking()) mobpatch.setBlocking(true);
        if (target != null) {
            this.mobpatch.rotateTo(target, this.mobpatch.getYRotLimit(), false);
            LivingEntityPatch<?> targetPatch = (LivingEntityPatch<?>) target.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY,null).orElse(null);
            if (targetPatch != null){
                if(targetPatch.getEntityState().getLevel() == 0 && this.keepDistance()) {
                    ++this.targetInactiontime;
                    if(blocktick > 0)mobpatch.setBlockTick(blocktick - 1);
                } else {
                    this.targetInactiontime = 0;
                }
            } else {
                mobpatch.setBlockTick(blocktick - 1);
            }
        }
    }
}
