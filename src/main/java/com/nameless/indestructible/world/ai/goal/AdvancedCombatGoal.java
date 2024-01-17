package com.nameless.indestructible.world.ai.goal;

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

		boolean inaction = this.mobpatch instanceof AdvancedCustomHumanoidMobPatch<?> ACHMobpatch && (ACHMobpatch.getBlockTick() > 0 || ACHMobpatch.getInactionTime() >0);
		if (this.mobpatch.getTarget() != null && !inaction) {
			EntityState state = this.mobpatch.getEntityState();
			this.combatBehaviors.tick();
			if (this.combatBehaviors.hasActivatedMove()) {
				if (state.canBasicAttack()) {
					CombatBehaviors.Behavior<T> result = this.combatBehaviors.tryProceed();

					if (result != null) {
						if(this.mobpatch instanceof AdvancedCustomHumanoidMobPatch<?> ACHMobpatch) ACHMobpatch.resetMotion();
						result.execute(this.mobpatch);
					}
				}
			} else {
				if (!state.inaction()) {
					CombatBehaviors.Behavior<T> result = this.combatBehaviors.selectRandomBehaviorSeries();

					if (result != null) {
						if(this.mobpatch instanceof AdvancedCustomHumanoidMobPatch<?> ACHMobpatch) ACHMobpatch.resetMotion();
						result.execute(this.mobpatch);
					}
				}
			}
		}
	}
}