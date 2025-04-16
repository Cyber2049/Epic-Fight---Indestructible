package com.nameless.indestructible.world.ai.goal;

import com.nameless.indestructible.world.capability.Utils.IAdvancedCapability;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;

public class AdvancedChasingGoal<T extends MobPatch<?>> extends MeleeAttackGoal {
	private final T mobPatch;
	protected final double attackRadiusSqr;
	private final double speed;
	public AdvancedChasingGoal(T mobPatch, PathfinderMob pathfinderMob, double speedModifier, boolean longMemory, double attackRadius) {
		super(pathfinderMob, speedModifier, longMemory);
		this.mobPatch = mobPatch;
		this.speed = speedModifier;
		this.attackRadiusSqr = attackRadius * attackRadius;
	}

	@Override
	public boolean canUse(){
		return ((IAdvancedCapability)mobPatch).getStrafingTime() > 0 || super.canUse();
	}

	@Override
	public void tick() {
		IAdvancedCapability iac = (IAdvancedCapability) mobPatch;
		if(iac.getInactionTime() > 0){
			iac.setInactionTime(iac.getInactionTime()-1);
		}
		LivingEntity target = this.mob.getTarget();
		if(target == null) return;
		if(!mobPatch.getEntityState().turningLocked()) {
			this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
		}

		if (mobPatch.getEntityState().movementLocked()) return;
		boolean withDistance = this.attackRadiusSqr > this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());

		if (iac.getStrafingTime() > 0) {
			iac.setStrafingTime(iac.getStrafingTime() - 1);
			this.mob.getNavigation().stop();
			this.mob.lookAt(target,30F,30F);
			this.mob.getMoveControl().strafe(withDistance && iac.getStrafingForward() > 0 ? 0 : iac.getStrafingForward(), iac.getStrafingClockwise());
		} else if (withDistance) {
			this.mob.getNavigation().stop();
		} else if (iac.isBlocking()) {
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