package com.nameless.indestructible.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.nameless.indestructible.client.UIConfig;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import yesman.epicfight.client.gui.EntityIndicator;
import yesman.epicfight.client.gui.TargetIndicator;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import javax.annotation.Nullable;

@Mixin(TargetIndicator.class)
public abstract class TargetIndicatorMixin extends EntityIndicator {
    @Override
    public void drawIndicator(LivingEntity entityIn, @Nullable LivingEntityPatch<?> entitypatch, LocalPlayerPatch playerpatch, PoseStack matStackIn, MultiBufferSource bufferIn, float partialTicks) {
        float y = UIConfig.REPLACE_UI.get() ? 0.6F : 0.45F;
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
    }

}
