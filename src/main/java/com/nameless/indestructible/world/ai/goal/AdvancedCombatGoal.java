package com.nameless.indestructible.world.ai.goal;

import com.nameless.indestructible.mixin.BehaviorSeriesMixin;
import com.nameless.indestructible.mixin.CombatBehaviorsMixin;
import com.nameless.indestructible.world.capability.AdvancedCustomHumanoidMobPatch;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.entity.ai.goal.AnimatedAttackGoal;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

public class AdvancedCombatGoal<T extends HumanoidMobPatch<?>> extends AnimatedAttackGoal<T> {

	public AdvancedCombatGoal(T mobpatch, CombatBehaviors<T> combatBehaviors) {
		super(mobpatch,combatBehaviors);
	}
	
	@Override
	public void tick() {
		if(!(this.mobpatch instanceof AdvancedCustomHumanoidMobPatch<?> ACHMobpatch)) return;

		boolean inaction =  (ACHMobpatch.isBlocking() || ACHMobpatch.getInactionTime() >0);
		if (this.mobpatch.getTarget() != null) {
			EntityState state = this.mobpatch.getEntityState();
			this.combatBehaviors.tick();
			if (this.combatBehaviors.hasActivatedMove()) {
				if(ACHMobpatch.interrupted){
					((CombatBehaviorsMixin<?>)combatBehaviors).setCurrentBehaviorPointer(-1);
					CombatBehaviors.BehaviorSeries<?> currentBehaviorSeries = ((CombatBehaviorsMixin<?>)combatBehaviors).getBehaviorSeriesList().get(0);
					((BehaviorSeriesMixin)currentBehaviorSeries).setNextBehaviorPointer(0);
					ACHMobpatch.interrupted = false;
					return;
				}

				if (state.canBasicAttack() && !inaction) {
					CombatBehaviors.Behavior<T> result = this.combatBehaviors.tryProceed();

					if (result != null) {
						ACHMobpatch.resetMotion();
						result.execute(this.mobpatch);
					}
                }
			} else {
				if (!state.inaction() && !inaction) {
					CombatBehaviors.Behavior<T> result = this.combatBehaviors.selectRandomBehaviorSeries();

					if (result != null) {
						ACHMobpatch.resetMotion();
						result.execute(this.mobpatch);
					}
				}
			}
		}
	}
}