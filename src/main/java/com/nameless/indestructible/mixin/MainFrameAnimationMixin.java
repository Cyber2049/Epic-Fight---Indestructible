package com.nameless.indestructible.mixin;

import com.nameless.indestructible.api.animation.types.CommandEvent;
import com.nameless.indestructible.world.capability.Utils.IAdvancedCapability;
import com.nameless.indestructible.world.capability.Utils.IAnimationEventCapability;
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
        if(entitypatch instanceof IAdvancedCapability iac){
            iac.resetActionTick();
        }
    }

    @Inject(method = "tick(Lyesman/epicfight/world/capabilities/entitypatch/LivingEntityPatch;)V",at = @At("TAIL"), remap = false)
    public void onTick(LivingEntityPatch<?> entitypatch, CallbackInfo ci){
        if(!entitypatch.isLogicalClient() && entitypatch instanceof IAnimationEventCapability iec && iec.getEventManager().hasTimeEvent()){

                AnimationPlayer player = entitypatch.getAnimator().getPlayerFor(this);
                if (player != null) {
                    float prevElapsed = player.getPrevElapsedTime();
                    float elapsed = player.getElapsedTime();


                    for(CommandEvent.TimeStampedEvent event: iec.getEventManager().getTimeEventList()){
                            event.testAndExecute(entitypatch, prevElapsed, elapsed);
                            if(!entitypatch.getOriginal().isAlive() || !iec.getEventManager().hasTimeEvent()){break;}
                    }
                }
        }
    }

}
