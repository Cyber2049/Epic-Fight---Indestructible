package com.nameless.indestructible.world.ai.goal;

import com.nameless.indestructible.api.animation.types.CustomGuardAnimation;
import com.nameless.indestructible.utils.CustomHumanoidMobPatchUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.CustomHumanoidMobPatch;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.EnumSet;

public class RangedGuardGoal <T extends CustomHumanoidMobPatch<?>> extends Goal {
    private final T mobpatch;
    private final double speedModifier;
    private final float radiusSqr;
    private int seeTime;
    private boolean keepDistance;
    private int targetInactiontime = -1;

    public RangedGuardGoal(T customHumanoidMobPatch, double speedModifier, float radius) {
        this.mobpatch = customHumanoidMobPatch;
        this.speedModifier = speedModifier;
        this.radiusSqr = radius  * radius;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public boolean canUse() {
        return this.checkTargetValid() && this.isBlocking() && !this.mobpatch.getOriginal().isUsingItem();
    }
    public boolean canContinueToUse() {
        return this.canUse() && !(this.targetInaction() && this.keepDistance);
    }

    public void start() {
        super.start();
        this.targetInactiontime = -1;
        this.mobpatch.getOriginal().setAggressive(true);
    }

    public void stop() {
        super.stop();
        this.seeTime = 0;
        this.targetInactiontime = -1;
        ((CustomHumanoidMobPatchUtils)mobpatch).setBlockTick(0);
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
    private boolean isBlocking(){
        return ((CustomHumanoidMobPatchUtils)mobpatch).getBlockTick() > 0;
    }

    private boolean targetInaction(){
        EntityPatch<?> target = this.mobpatch.getTarget().getCapability(EpicFightCapabilities.CAPABILITY_ENTITY,null).orElse(null);
        if (target == null || !(target instanceof LivingEntityPatch<?>)){
            return true;
        } else {
            return targetInactiontime > 20;
        }
    }

    public void tick() {
        LivingEntity target = this.mobpatch.getTarget();
        CustomGuardAnimation animation = ((CustomHumanoidMobPatchUtils)mobpatch).getGuardAnimation();
        int blocktick = ((CustomHumanoidMobPatchUtils)mobpatch).getBlockTick();
        this.mobpatch.playAnimationSynchronized(animation,0F);
        Mob mob = this.mobpatch.getOriginal();
        if (target != null) {
            EntityPatch<?> targetEntityPatch = target.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY,null).orElse(null);
            if (targetEntityPatch != null && targetEntityPatch instanceof LivingEntityPatch<?> targetPatch){
                if(targetPatch.getEntityState().getLevel() == 0) {
                    ++this.targetInactiontime;
                } else {
                    this.targetInactiontime = 0;
                }
                if(blocktick > 0 && !targetPatch.getEntityState().attacking() && this.keepDistance){
                    ((CustomHumanoidMobPatchUtils)mobpatch).setBlockTick(blocktick - 1);
                }
            } else {
                ((CustomHumanoidMobPatchUtils)mobpatch).setBlockTick(blocktick - 1);
            }
            double d0 = mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
            boolean flag = mob.getSensing().hasLineOfSight(target);
            boolean flag1 = this.seeTime > 0;
            if (flag != flag1) {
                this.seeTime = 0;
            }

            if (flag) {
                ++this.seeTime;
            } else {
                --this.seeTime;
            }

            if (!(d0 > (double)this.radiusSqr) && this.seeTime >= 10) {
                mob.getNavigation().stop();
                if(!this.keepDistance) this.keepDistance = true;
            } else {
                mob.getNavigation().moveTo(target, this.speedModifier);
                if(this.keepDistance) this.keepDistance = false;
            }
        }
    }
}
