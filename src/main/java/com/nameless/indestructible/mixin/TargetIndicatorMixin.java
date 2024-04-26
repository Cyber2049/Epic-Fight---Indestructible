package com.nameless.indestructible.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.nameless.indestructible.client.UIConfig;
import com.nameless.indestructible.world.capability.AdvancedCustomHumanoidMobPatch;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.client.gui.EntityIndicator;
import yesman.epicfight.client.gui.TargetIndicator;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@Mixin(TargetIndicator.class)
public abstract class TargetIndicatorMixin extends EntityIndicator {
    @Inject(method = "drawIndicator(Lnet/minecraft/world/entity/LivingEntity;Lyesman/epicfight/world/capabilities/entitypatch/LivingEntityPatch;Lyesman/epicfight/client/world/capabilites/entitypatch/player/LocalPlayerPatch;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;F)V", at = @At("HEAD"), cancellable = true, remap = false)
    public void drawIndicator(LivingEntity entityIn, LivingEntityPatch<?> entitypatch, LocalPlayerPatch playerpatch, PoseStack matStackIn, MultiBufferSource bufferIn, float partialTicks, CallbackInfo ci) {
        boolean adjustUI = entitypatch instanceof AdvancedCustomHumanoidMobPatch<?> && UIConfig.REPLACE_UI.get();
        float y = adjustUI ? 0.65F : 0.5F;
        Matrix4f mvMatrix = super.getMVMatrix(matStackIn, entityIn, 0.0F, entityIn.getBbHeight() + y, 0.0F, true, partialTicks);

        if (entitypatch == null) {
            this.drawTexturedModalRect2DPlane(mvMatrix, bufferIn.getBuffer(EpicFightRenderTypes.entityIndicator(BATTLE_ICON)), -0.1F, -0.1F, 0.1F, 0.1F, 97, 2, 128, 33);
        } else {
            if (entityIn.tickCount % 2 == 0 && !entitypatch.flashTargetIndicator(playerpatch)) {
                this.drawTexturedModalRect2DPlane(mvMatrix, bufferIn.getBuffer(EpicFightRenderTypes.entityIndicator(BATTLE_ICON)), -0.1F, -0.1F, 0.1F, 0.1F, 132, 0, 167, 36);
            } else {
                this.drawTexturedModalRect2DPlane(mvMatrix, bufferIn.getBuffer(EpicFightRenderTypes.entityIndicator(BATTLE_ICON)), -0.1F, -0.1F, 0.1F, 0.1F, 97, 2, 128, 33);
            }
        }
        ci.cancel();
    }

}
