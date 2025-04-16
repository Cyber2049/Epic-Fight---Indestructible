package com.nameless.indestructible.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class ClientBossInfo {
    private final String displayName;
    private final ResourceLocation rl;
    private final UUID id;
    private final String originalName;

    private float healthRatio = 1;
    private float staminaRatio = 1;
    public ClientBossInfo(String displayName, String originalName, ResourceLocation rl, UUID id){
        this.displayName = displayName;
        this.rl = rl;
        this.id = id;
        this.originalName = originalName;
    }

    public void setHealthRatio(float ratio){
        this.healthRatio = ratio;
    }
    public void setStaminaRatio(float ratio){
        this.staminaRatio = ratio;
    }

    public float getHealthRatio(){
        return this.healthRatio;
    }

    public float getStaminaRatio(){
        return this.staminaRatio;
    }
    public String getDisplayName(){
        return this.displayName;
    }
    public String getOriginalName(){
        return this.originalName;
    }
    public UUID getId() {
        return id;
    }
    public ResourceLocation getRl(){
        return this.rl;
    }
}
