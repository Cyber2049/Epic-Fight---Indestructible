package com.nameless.indestructible.mixin;

import com.nameless.indestructible.api.animation.types.CommandEvent;
import com.nameless.indestructible.world.capability.AdvancedCustomHumanoidMobPatch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.types.MainFrameAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@Mixin(MainFrameAnimation.class)
public class MainFrameAnimationMixin extends StaticAnimation {
    @Inject(method = "begin(Lyesman/epicfight/world/capabilities/entitypatch/LivingEntityPatch;)V",at = @At("TAIL"), remap = false)
    public void onBegin(LivingEntityPatch<?> entitypatch, CallbackInfo ci){
        if(entitypatch instanceof AdvancedCustomHumanoidMobPatch<?> advancedCustomHumanoidMobPatch){
            advancedCustomHumanoidMobPatch.resetActionTick();
        }
    }

    @Inject(method = "tick(Lyesman/epicfight/world/capabilities/entitypatch/LivingEntityPatch;)V",at = @At("TAIL"), remap = false)
    public void onTick(LivingEntityPatch<?> entitypatch, CallbackInfo ci){
        if(!entitypatch.isLogicalClient() && entitypatch instanceof AdvancedCustomHumanoidMobPatch<?> advancedCustomHumanoidMobPatch && advancedCustomHumanoidMobPatch.getEventManager().hasTimeEvent()){

                AnimationPlayer player = entitypatch.getAnimator().getPlayerFor(this);
                if (player != null) {
                    float prevElapsed = player.getPrevElapsedTime();
                    float elapsed = player.getElapsedTime();


                    for(CommandEvent.TimeStampedEvent event: advancedCustomHumanoidMobPatch.getEventManager().getTimeEventList()){
                            event.testAndExecute(advancedCustomHumanoidMobPatch, prevElapsed, elapsed);
                            if(!advancedCustomHumanoidMobPatch.getOriginal().isAlive() || !advancedCustomHumanoidMobPatch.getEventManager().hasTimeEvent()){break;}
                    }
                }
        }
    }


}
