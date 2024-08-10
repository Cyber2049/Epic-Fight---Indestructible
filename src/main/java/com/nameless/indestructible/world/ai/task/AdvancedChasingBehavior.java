package com.nameless.indestructible.world.ai.task;

import com.nameless.indestructible.world.capability.AdvancedCustomHumanoidMobPatch;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;

public class AdvancedChasingBehavior<T extends AdvancedCustomHumanoidMobPatch<?>> extends MoveToTargetSink {
	private final T mobpatch;
	protected final double attackRadiusSqr;
	private final double speed;
	public AdvancedChasingBehavior(T mobpatch, double speedModifier, double attackRadius) {
		super(150,250);
		this.mobpatch = mobpatch;
		this.speed = speedModifier;
		this.attackRadiusSqr = attackRadius * attackRadius;
	}

	@Override
	public boolean checkExtraStartConditions(ServerLevel level, Mob mob){
		return mobpatch.getStrafingTime() > 0 || super.checkExtraStartConditions(level, mob);
	}

	@Override
	public void tick(ServerLevel level, Mob mob, long p_23619_) {
		if(this.mobpatch.getInactionTime() > 0){
			mobpatch.setInactionTime(mobpatch.getInactionTime()-1);
		}

		LivingEntity target = mob.getTarget();
		if(target == null) return;
		if(!this.mobpatch.getEntityState().turningLocked()) {
			mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
		}

		if (this.mobpatch.getEntityState().movementLocked()) return;
		boolean withDistance = this.attackRadiusSqr > mob.distanceToSqr(target.getX(), target.getY(), target.getZ());

		if (mobpatch.getStrafingTime() > 0) {
			mobpatch.setStrafingTime(mobpatch.getStrafingTime() - 1);
			mob.getNavigation().stop();
			mob.lookAt(target,30F,30F);
			mob.getMoveControl().strafe(withDistance && mobpatch.getStrafingForward() > 0 ? 0 : mobpatch.getStrafingForward(), mobpatch.getStrafingClockwise());
		} else if (withDistance) {
			mob.getNavigation().stop();
		} else if (this.mobpatch.isBlocking()) {
			mob.lookAt(target,30F,30F);
			mob.getNavigation().moveTo(target, this.speed * 0.8F);
		} else {
			super.tick(level, mob, p_23619_);
		}
	}
}