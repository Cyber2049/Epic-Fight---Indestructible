package com.nameless.indestructible.world.ai.goal;

import com.nameless.indestructible.world.capability.AdvancedCustomHumanoidMobPatch;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

public class AdvancedChasingGoal<T extends AdvancedCustomHumanoidMobPatch<?>> extends MeleeAttackGoal {
	private final T mobpatch;
	protected final double attackRadiusSqr;
	private final double speed;
	public AdvancedChasingGoal(T mobpatch, PathfinderMob pathfinderMob, double speedModifier, boolean longMemory, double attackRadius) {
		super(pathfinderMob, speedModifier, longMemory);
		this.mobpatch = mobpatch;
		this.speed = speedModifier;
		this.attackRadiusSqr = attackRadius * attackRadius;
	}

	@Override
	public boolean canUse(){
		return mobpatch.getStrafingTime() > 0 || super.canUse();
	}

	@Override
	public void tick() {
		if(this.mobpatch.getInactionTime() > 0){
			mobpatch.setInactionTime(mobpatch.getInactionTime()-1);
		}

		LivingEntity target = this.mob.getTarget();
		if(target == null) return;
		if(!this.mobpatch.getEntityState().turningLocked()) {
			this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
		}

		if (this.mobpatch.getEntityState().movementLocked()) return;
		boolean withDistance = this.attackRadiusSqr > this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());

		if (mobpatch.getStrafingTime() > 0) {
			mobpatch.setStrafingTime(mobpatch.getStrafingTime() - 1);
			this.mob.getNavigation().stop();
			this.mob.lookAt(target,30F,30F);
			this.mob.getMoveControl().strafe(withDistance && mobpatch.getStrafingForward() > 0 ? 0 : mobpatch.getStrafingForward(), mobpatch.getStrafingClockwise());
		} else if (withDistance) {
			this.mob.getNavigation().stop();
		} else if (this.mobpatch.isBlocking()) {
			this.mob.lookAt(target,30F,30F);
			this.mob.getNavigation().moveTo(target, this.speed * 0.8F);
		} else {
			super.tick();
		}
	}


	public void stop() {
		super.stop();
	}
	
	@Override
	protected void checkAndPerformAttack(LivingEntity target, double p_25558_) {
		
	}
	@Override
	protected double getAttackReachSqr(LivingEntity p_25556_) {
		return this.attackRadiusSqr;
	}
}