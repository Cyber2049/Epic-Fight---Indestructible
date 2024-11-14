package com.nameless.indestructible.client.gui;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.nameless.indestructible.world.capability.AdvancedCustomHumanoidMobPatch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class BossBarGUi {
    public static final Map<UUID, AdvancedCustomHumanoidMobPatch<?>> BossBarEntities = Maps.newHashMap();
    public static final List<String> cancelBossBar = new ArrayList<>();
    //public static final Set<LivingEntityPatch<?>> StatusBarEntitier =  new HashSet<>();
    //private final StatusIndicator statusIndicator = new StatusIndicator();
    @SubscribeEvent
    public void renderBossBar(RenderGameOverlayEvent.BossInfo event){
        if(!cancelBossBar.isEmpty()){
            for (String bossEventName : cancelBossBar) {
                if (event.getBossEvent().getName().getString().equals(bossEventName)) {
                    event.setCanceled(true);
                    event.setIncrement(0);
                }
            }
        }

        BossBarEntities.values().forEach((k)->{
            if(event.getBossEvent().getName().getString().contains(k.getOriginal().getDisplayName().getString())){
                event.setCanceled(true);
                event.setIncrement(0);
            }
        });

        if(!event.getBossEvent().getName().getString().equals("advanced epic fight boss")) return;
        UUID infoID = event.getBossEvent().getId();
        AdvancedCustomHumanoidMobPatch<?> achPatch = BossBarEntities.get(infoID);
        if(achPatch != null){
            event.setCanceled(true);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, achPatch.getBossBar());
            int l = Minecraft.getInstance().font.width(achPatch.getCustomName());
            int i1 = event.getWindow().getGuiScaledWidth() / 2 - l / 2;
            int x = event.getX() - 36;
            int y = event.getY(); // + 31;
            if (event.getY() >= Minecraft.getInstance().getWindow().getGuiScaledHeight() / 3) return;
            PoseStack barPoseStack = event.getMatrixStack();
            barPoseStack.pushPose();
            barPoseStack.scale(1F,1F,1F);
            float healthRatio = Mth.clamp(achPatch.getOriginal().getHealth()/achPatch.getOriginal().getMaxHealth(),0.0F,1.0F);
            int health = (int) (256 * healthRatio);
            float staminaRatio = Mth.clamp(achPatch.getStamina() / achPatch.getMaxStamina(), 0.0F, 1.0F);
            int stamina = (int) (256 * staminaRatio);
            GuiComponent.blit(barPoseStack, x, y, 0, 0, 256, 19,255,255);
            GuiComponent.blit(barPoseStack, x, y, 0, 21, health, 19,255,255);
            GuiComponent.blit(barPoseStack, x, y + 18, 0, 42, 256, 10,255,255);
            GuiComponent.blit(barPoseStack, x, y + 18, 0, 55, stamina, 10,255,255);
            GuiComponent.blit(barPoseStack, x, y, 0, 68, 256, 29,255,255);
            barPoseStack.popPose();
            Minecraft.getInstance().font.drawShadow(event.getMatrixStack(), achPatch.getCustomName(), i1, (float) (y - 9) , 16777215);
            event.setIncrement(44);
        }


    }

    /*
    @SubscribeEvent
    public void renderLivingEvent(RenderLivingEvent.Pre<? extends LivingEntity, ? extends EntityModel<? extends LivingEntity>> event) {
        LivingEntity entity = event.getEntity();
        LivingEntityPatch<?> livingEntityPatch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
        if(livingEntityPatch != null && ClientEngine.getInstance().getPlayerPatch() != null && !ClientEngine.getInstance().renderEngine.minecraft.options.hideGui && !entity.level.getGameRules().getBoolean(EpicFightGamerules.DISABLE_ENTITY_UI)){
           if(statusIndicator.shouldDraw(entity, livingEntityPatch, ClientEngine.getInstance().getPlayerPatch())) {
               StatusBarEntitier.add(livingEntityPatch);
           }
        }
    }

    @SubscribeEvent
    public void renderWorldLast(RenderLevelStageEvent event) {
        if(event.getStage().equals(RenderLevelStageEvent.Stage.AFTER_PARTICLES)) {
        Iterator<LivingEntityPatch<?>> it = StatusBarEntitier.iterator();
        MultiBufferSource source = Minecraft.getInstance().renderBuffers().bufferSource();
        while (it.hasNext()) {
                LivingEntityPatch<?> livingEntityPatch = it.next();
                statusIndicator.drawIndicator(livingEntityPatch.getOriginal(), livingEntityPatch, ClientEngine.getInstance().getPlayerPatch(), event.getPoseStack(), source, event.getPartialTick());
                it.remove();
            }
        }
    }

     */
}
