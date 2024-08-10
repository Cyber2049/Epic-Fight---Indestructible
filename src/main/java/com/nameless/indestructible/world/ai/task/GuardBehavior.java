package com.nameless.indestructible.world.ai.task;

import com.nameless.indestructible.world.capability.AdvancedCustomHumanoidMobPatch;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BackUpIfTooClose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ProjectileWeaponItem;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class GuardBehavior<E extends Mob, T extends AdvancedCustomHumanoidMobPatch<?>> extends BackUpIfTooClose<E> {


	private final T mobpatch;
	private final float radiusSqr;
	private int targetInactiontime = -1;

	public GuardBehavior(T customHumanoidMobPatch, float radius) {
		super(1, 1);
		this.mobpatch = customHumanoidMobPatch;
		this.radiusSqr = radius  * radius;
	}
	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E mob) {
		return this.checkTargetValid() && this.mobpatch.isBlocking();
	}
	@Override
	protected boolean canStillUse(ServerLevel level, E mob, long p_22547_) {
		return this.checkExtraStartConditions(level, mob) && !this.targetInaction();
	}

	public void start(ServerLevel level, E mob, long p_22507_) {
		this.targetInactiontime = -1;
		this.mobpatch.resetActionTick();
	}

	public void stop(ServerLevel level, E mob, long p_22550_) {
		this.targetInactiontime = -1;
		this.mobpatch.setParryCounter(0);
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

	private boolean withinDistance(){
		LivingEntity target = this.mobpatch.getTarget();
		return this.mobpatch.getOriginal().distanceToSqr(target.getX(), target.getY(), target.getZ()) <= (double)this.radiusSqr;
	}

	private boolean targetInaction(){
		LivingEntityPatch<?> target = EpicFightCapabilities.getEntityPatch(mobpatch.getTarget(),LivingEntityPatch.class);
		if (target == null){
			return true;
		} else {
			return targetInactiontime > this.mobpatch.getBlockTick();
		}
	}

	public void tick(ServerLevel level, E mob, long p_22553_) {
		LivingEntity target = this.mobpatch.getTarget();
		if (target != null) {
			LivingEntityPatch<?> targetPatch = EpicFightCapabilities.getEntityPatch(target, LivingEntityPatch.class);
			if (targetPatch != null){
				int phase = targetPatch.getEntityState().getLevel();
				if(this.withinDistance() && phase > 0 && phase < 3) {
					this.targetInactiontime = 0;
				} else if (this.mobpatch.canBlockProjectile() && target.getUseItem().getItem() instanceof ProjectileWeaponItem && target.isUsingItem()) {
					this.targetInactiontime = 0;
				} else {
					++this.targetInactiontime;
				}
			}
		}
	}
}