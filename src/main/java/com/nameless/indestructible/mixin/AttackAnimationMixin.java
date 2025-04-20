package com.nameless.indestructible.mixin;

import com.nameless.indestructible.world.capability.Utils.IAdvancedCapability;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@Mixin(AttackAnimation.class)
public class AttackAnimationMixin {
    @Inject(method = "getPlaySpeed(Lyesman/epicfight/world/capabilities/entitypatch/LivingEntityPatch;Lyesman/epicfight/api/animation/types/DynamicAnimation;)F", at = @At("RETURN"), cancellable = true, remap = false)
    private void onGetPlaySpeed(LivingEntityPatch<?> entitypatch, DynamicAnimation animation, CallbackInfoReturnable<Float> cir) {
        if(entitypatch instanceof IAdvancedCapability iac){
            cir.setReturnValue(iac.getAttackSpeed());
        }
    }
}
