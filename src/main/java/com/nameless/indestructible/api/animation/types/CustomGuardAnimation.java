package com.nameless.indestructible.api.animation.types;

import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;

public class CustomGuardAnimation extends StaticAnimation {

	public final String successAnimation;
	public final String failAnimation;

	public CustomGuardAnimation(String path, String successanimation, String failanimation, Armature armature) {
		super(0.05F,true, path, armature);
		this.successAnimation = successanimation;
		this.failAnimation = failanimation;
	}

	@Override
	public void begin(LivingEntityPatch<?> entitypatch) {
		super.begin(entitypatch);
		if (entitypatch instanceof MobPatch) {
			LivingEntity target = entitypatch.getTarget();

			if (target != null) {
				entitypatch.rotateTo(target, entitypatch.getYRotLimit(), false);
			}
		}
	}

}