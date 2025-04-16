package com.nameless.indestructible.mixin;

import com.nameless.indestructible.world.capability.Utils.IAdvancedCapability;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.entity.eventlistener.HurtEvent;

@Mixin(HurtEvent.class)
public class HurtEventMixin<T> {

    @Shadow @Final
    private T damageSource;
    @Inject(method = "setParried(Z)V", at = @At("TAIL"), remap = false)
    private void setParried(boolean parried, CallbackInfo ci) {
        if(damageSource instanceof DamageSource d){
            Entity directEntity = d.getDirectEntity();
            MobPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(directEntity, MobPatch.class);

            if (entitypatch instanceof IAdvancedCapability iac) {
                iac.setParried(parried);
            }
        }
    }
}
