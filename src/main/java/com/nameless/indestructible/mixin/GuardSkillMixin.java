package com.nameless.indestructible.mixin;

import com.nameless.indestructible.world.capability.AdvancedCustomHumanoidMobPatch;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.guard.GuardSkill;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.entity.eventlistener.HurtEvent;

@Mixin(GuardSkill.class)
public class GuardSkillMixin {

    @Inject(method = "guard(Lyesman/epicfight/skill/SkillContainer;Lyesman/epicfight/world/capabilities/item/CapabilityItem;Lyesman/epicfight/world/entity/eventlistener/HurtEvent$Pre;FFZ)V", at = @At("TAIL"), remap = false)
    private void onGuard(SkillContainer container, CapabilityItem itemCapability, HurtEvent.Pre event, float knockback, float impact, boolean advanced, CallbackInfo ci) {
        Entity directEntity = event.getDamageSource().getDirectEntity();
        AdvancedCustomHumanoidMobPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(directEntity, AdvancedCustomHumanoidMobPatch.class);

        if (entitypatch != null) {
            entitypatch.setParried(event.isParried());
        }
    }
}
