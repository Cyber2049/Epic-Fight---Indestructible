package com.nameless.indestructible.world.ai.task;

import com.google.common.collect.ImmutableMap;
import com.nameless.indestructible.mixin.BehaviorSeriesMixin;
import com.nameless.indestructible.mixin.CombatBehaviorsMixin;
import com.nameless.indestructible.world.capability.Utils.IAdvancedCapability;
import com.nameless.indestructible.world.capability.Utils.IAnimationEventCapability;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ProjectileWeaponItem;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

public class AdvancedCombatBehavior<T extends MobPatch<?>> extends Behavior<Mob> {
	protected final T mobPatch;
	protected final CombatBehaviors<T> combatBehaviors;
	public AdvancedCombatBehavior(T mobPatch, CombatBehaviors<T> combatBehaviors) {
		super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT));
		this.mobPatch = mobPatch;
		this.combatBehaviors = combatBehaviors;
	}
	
	@Override
	protected void tick(ServerLevel worldIn, Mob entityIn, long gameTimeIn) {
		IAdvancedCapability iac = (IAdvancedCapability) mobPatch;
		boolean inaction = iac.isBlocking() || iac.getInactionTime() >0;
		if (mobPatch.getTarget() != null) {
			EntityState state = mobPatch.getEntityState();
			this.combatBehaviors.tick();
			if (this.combatBehaviors.hasActivatedMove()) {
				if(iac.interrupted()){
					int count =  ((CombatBehaviorsMixin<?>)combatBehaviors).getCurrentBehaviorPointer();
					CombatBehaviors.BehaviorSeries<?> currentBehaviorSeries = ((CombatBehaviorsMixin<?>)combatBehaviors).getBehaviorSeriesList().get(count);
					((BehaviorSeriesMixin)currentBehaviorSeries).setLoopFinished(true);
					((BehaviorSeriesMixin)currentBehaviorSeries).setNextBehaviorPointer(0);
					iac.setInterrupted(false);
					return;
				}
				if (state.canBasicAttack() && !inaction) {
					CombatBehaviors.Behavior<? super T> result = this.combatBehaviors.tryProceed();

					if (result != null) {
						iac.setDamageSourceModifier(null);
						iac.setBlocking(false);
						iac.setAttackSpeed(1F);
						iac.setHurtResistLevel(2);

						if(mobPatch instanceof IAnimationEventCapability iec){
							iec.getEventManager().initActiveEvent();
						}
						result.execute(mobPatch);
					}
				}
			} else {
				if (!state.inaction() && !inaction) {
					CombatBehaviors.Behavior<? super T> result = this.combatBehaviors.selectRandomBehaviorSeries();

					if (result != null) {
						iac.setDamageSourceModifier(null);
						iac.setBlocking(false);
						iac.setAttackSpeed(1F);
						iac.setHurtResistLevel(2);

						if(mobPatch instanceof IAnimationEventCapability iec){
							iec.getEventManager().initActiveEvent();
						}
						result.execute(mobPatch);
					}
				}
			}
		}
	}

	protected boolean checkExtraStartConditions(ServerLevel levelIn, Mob entityIn) {
		return !this.isHoldingRangeWeapon(entityIn) && this.isValidTarget(mobPatch.getTarget());
	}

	protected boolean canStillUse(ServerLevel levelIn, Mob entityIn, long gameTimeIn) {
		return this.checkExtraStartConditions(levelIn, entityIn) && BehaviorUtils.canSee(entityIn, mobPatch.getTarget()) && !mobPatch.getEntityState().hurt();
	}
	private boolean isHoldingRangeWeapon(Mob mob) {
		return mob.isHolding((stack) -> {
			Item item = stack.getItem();
			return item instanceof ProjectileWeaponItem && mob.canFireProjectileWeapon((ProjectileWeaponItem)item);
		});
	}

	protected boolean isValidTarget(LivingEntity attackTarget) {
		return attackTarget != null && attackTarget.isAlive() && (!(attackTarget instanceof Player) || !attackTarget.isSpectator() && !((Player)attackTarget).isCreative());
	}
}