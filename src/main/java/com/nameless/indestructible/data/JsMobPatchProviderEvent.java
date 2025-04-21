package com.nameless.indestructible.data;

import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;


public class JsMobPatchProviderEvent extends EventJS {
    @Info(value = "define the entity as EFM-like",
            params = {
            @Param(name = "entity_type", value = "String, registry name of the entity type"),
                    @Param(name = "builder", value = "builder of Humanoid mob patch, call JsHumanoidMobPatchBuilder.builder() to get this builder, and call the method in it to define its properties")})
    public void addHumanoidMobPatch(String entity_type, JsHumanoidMobPatchProviderBuilder builder){
        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(entity_type));
        if(entityType == null) throw new IllegalArgumentException("can't find entity type: " + entity_type);
        AdvancedMobpatchReloader.addProvider(entityType, builder.build());
        AdvancedMobpatchReloader.addClientTag(entityType, builder.buildClientTag());
    }
    @Info(value = "define the entity as EFM-like",
            params = {
            @Param(name = "entity_type", value = "String, registry name of the entity type"),
                    @Param(name = "builder", value = "builder of mob patch, call JsMobPatchBuilder.builder() to get this builder, and call the method in it to define its properties")})
    public void addMobPatch(String entity_type, JsMobPatchProviderBuilder builder){
        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(entity_type));
        if(entityType == null) throw new IllegalArgumentException("can't find entity type: " + entity_type);
        AdvancedMobpatchReloader.addProvider(entityType, builder.build());
        AdvancedMobpatchReloader.addClientTag(entityType, builder.buildClientTag());
    }
}
