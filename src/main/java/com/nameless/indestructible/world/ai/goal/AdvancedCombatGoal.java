package com.nameless.indestructible.world.ai.goal;

import com.nameless.indestructible.mixin.BehaviorSeriesMixin;
import com.nameless.indestructible.mixin.CombatBehaviorsMixin;
import com.nameless.indestructible.world.capability.AdvancedCustomHumanoidMobPatch;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

public class AdvancedCombatGoal<T extends AdvancedCustomHumanoidMobPatch<?>> extends Goal {
	protected final AdvancedCustomHumanoidMobPatch<?> mobpatch;
	protected final CombatBehaviors<HumanoidMobPatch<?>> combatBehaviors;
	public AdvancedCombatGoal(AdvancedCustomHumanoidMobPatch<?> mobpatch, CombatBehaviors<HumanoidMobPatch<?>> combatBehaviors) {
		this.mobpatch = mobpatch;
		this.combatBehaviors = combatBehaviors;
	}
	@Override
	public boolean canUse() {
		LivingEntity livingentity = this.mobpatch.getTarget();
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

		boolean inaction =  (mobpatch.isBlocking() || mobpatch.getInactionTime() >0);
		if (this.mobpatch.getTarget() != null) {
			EntityState state = this.mobpatch.getEntityState();
			this.combatBehaviors.tick();
			if (this.combatBehaviors.hasActivatedMove()) {
				if(mobpatch.interrupted){
					int count =  ((CombatBehaviorsMixin<?>)combatBehaviors).getCurrentBehaviorPointer();
					CombatBehaviors.BehaviorSeries<?> currentBehaviorSeries = ((CombatBehaviorsMixin<?>)combatBehaviors).getBehaviorSeriesList().get(count);
					((BehaviorSeriesMixin)currentBehaviorSeries).setLoopFinished(true);
					((BehaviorSeriesMixin)currentBehaviorSeries).setNextBehaviorPointer(0);
					mobpatch.interrupted = false;
					return;
				}

				if (state.canBasicAttack() && !inaction) {
					CombatBehaviors.Behavior<HumanoidMobPatch<?>> result = this.combatBehaviors.tryProceed();

					if (result != null) {
						mobpatch.resetMotion();
						result.execute(this.mobpatch);
					}
                }
			} else {
				if (!state.inaction() && !inaction) {
					CombatBehaviors.Behavior<HumanoidMobPatch<?>> result = this.combatBehaviors.selectRandomBehaviorSeries();

					if (result != null) {
						mobpatch.resetMotion();
						result.execute(this.mobpatch);
					}
				}
			}
		}
	}
}