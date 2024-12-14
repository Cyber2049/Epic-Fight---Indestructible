package com.nameless.indestructible.mixin;

import com.nameless.indestructible.world.capability.AdvancedCustomHumanoidMobPatch;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.entity.eventlistener.HurtEvent;

@Mixin(HurtEvent.class)
public class HurtEventMixin<T> {

    @Shadow @Final
    private T damageSource;
    @Inject(method = "setParried(Z)V", at = @At("TAIL"), remap = false)
    private void setParried(boolean parried, CallbackInfo ci) {
        if(damageSource instanceof DamageSource d){
            Entity directEntity = d.getDirectEntity();
            AdvancedCustomHumanoidMobPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(directEntity, AdvancedCustomHumanoidMobPatch.class);

            if (entitypatch != null) {
                entitypatch.setParried(parried);
            }
        }
    }
}
