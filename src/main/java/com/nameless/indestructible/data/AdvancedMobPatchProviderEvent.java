package com.nameless.indestructible.data;

import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;


public class AdvancedMobPatchProviderEvent extends EventJS {
    public void addHumanoidMobPatch(String entity_type, JSCustomHumanoidMobPatchProviderBuilder builder){
        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(entity_type));
        AdvancedMobpatchReloader.addProvider(entityType, builder.build());
        AdvancedMobpatchReloader.addClientTag(entityType, builder.buildClientTag());
    }

    /*
    public void addMobPatch(EntityType<? extends PathfinderMob> entity_type, AdvancedCustomMobPatchProviderBuilderJS builder){
        AdvancedMobpatchReloader.addProvider(entity_type, builder.build());
        AdvancedMobpatchReloader.addClientTag(entity_type, builder.buildClientTag());
    }
     */
}
