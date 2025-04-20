package com.nameless.indestructible.world.ai.goal;

import com.nameless.indestructible.mixin.BehaviorSeriesMixin;
import com.nameless.indestructible.mixin.CombatBehaviorsMixin;
import com.nameless.indestructible.world.capability.Utils.IAdvancedCapability;
import com.nameless.indestructible.world.capability.Utils.IAnimationEventCapability;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

public class AdvancedCombatGoal<T extends MobPatch<?>> extends Goal {
	protected final T mobPatch;
	protected final CombatBehaviors<T> combatBehaviors;
	public AdvancedCombatGoal(T mobPatch, CombatBehaviors<T> combatBehaviors) {
		this.mobPatch = mobPatch;
		this.combatBehaviors = combatBehaviors;
	}

	@Override
	public boolean canUse() {
		LivingEntity livingentity = mobPatch.getTarget();
		if (livingentity == null) {
			return false;
		} else if (!livingentity.isAlive()) {
			return false;
		} else {
			return !(livingentity instanceof Player) || !livingentity.isSpectator() && !((Player)livingentity).isCreative();
		}
	}

	@Override
	public void tick() {
		IAdvancedCapability iac = (IAdvancedCapability) mobPatch;
		boolean inaction =  (iac.isBlocking() || iac.getInactionTime() >0);
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
					CombatBehaviors.Behavior<T> result = this.combatBehaviors.tryProceed();

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
					CombatBehaviors.Behavior<T> result = this.combatBehaviors.selectRandomBehaviorSeries();

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
}