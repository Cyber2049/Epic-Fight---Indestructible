package com.nameless.indestructible.world.ai.task;

import com.google.common.collect.ImmutableMap;
import com.nameless.indestructible.world.capability.Utils.IAdvancedCapability;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ProjectileWeaponItem;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;

public class GuardBehavior<E extends Mob, T extends MobPatch<?>> extends Behavior<E> {


	private final T mobPatch;
	private final float radiusSqr;
	private int targetInactiontime = -1;

	public GuardBehavior(T mobPatch, float radius) {
		super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT));
		this.mobPatch = mobPatch;
		this.radiusSqr = radius  * radius;
	}
	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E mob) {
		return this.checkTargetValid() && ((IAdvancedCapability)mobPatch).isBlocking();
	}
	@Override
	protected boolean canStillUse(ServerLevel level, E mob, long p_22547_) {
		return this.checkExtraStartConditions(level, mob) && !this.targetInaction();
	}

	public void start(ServerLevel level, E mob, long p_22507_) {
		this.targetInactiontime = -1;
		((IAdvancedCapability)mobPatch).resetActionTick();
	}

	public void stop(ServerLevel level, E mob, long p_22550_) {
		this.targetInactiontime = -1;
		((IAdvancedCapability)mobPatch).setBlocking(false);
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
			return targetInactiontime > ((IAdvancedCapability)mobPatch).getBlockTick();
		}
	}

	public void tick(ServerLevel level, E mob, long p_22553_) {
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