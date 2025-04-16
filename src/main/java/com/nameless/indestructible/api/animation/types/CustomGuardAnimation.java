package com.nameless.indestructible.api.animation.types;

import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.animation.AnimationProvider;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.model.Armature;

public class CustomGuardAnimation extends StaticAnimation {

	public final AnimationProvider<StaticAnimation> successAnimation;
	public final Boolean isShield;

	public CustomGuardAnimation(String path, String successanimation, Armature armature, boolean isShield) {
		super(0.05F,true, path, armature);
		this.successAnimation =  AnimationProvider.of(new ResourceLocation(successanimation));
		this.isShield = isShield;
	}
	public CustomGuardAnimation(String path, String successanimation, Armature armature){
		this(path,successanimation,armature,false);
	}
}