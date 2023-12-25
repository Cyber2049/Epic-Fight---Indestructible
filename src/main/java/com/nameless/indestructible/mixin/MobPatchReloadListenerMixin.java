package com.nameless.indestructible.mixin;

import com.nameless.indestructible.data.AdvancedCustomHumanoidMobPatchProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.data.reloader.MobPatchReloadListener;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.model.armature.HumanoidArmature;
import yesman.epicfight.world.capabilities.entitypatch.Faction;

import java.util.Locale;

import static com.nameless.indestructible.data.MobPatchReloadUtils.*;
import static yesman.epicfight.api.data.reloader.MobPatchReloadListener.*;

@Mixin(MobPatchReloadListener.class)
public class MobPatchReloadListenerMixin {

    @Inject(method = "deserializeMobPatchProvider(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/nbt/CompoundTag;Z)Lyesman/epicfight/api/data/reloader/MobPatchReloadListener$AbstractMobPatchProvider;", at = @At("HEAD"), cancellable = true)
    private static void onDeserializeMobPatchProvider(EntityType<?> entityType, CompoundTag tag, boolean clientSide, CallbackInfoReturnable<AbstractMobPatchProvider> cir) {
        boolean disabled = tag.contains("disabled") && tag.getBoolean("disabled");
        boolean humanoid = tag.getBoolean("isHumanoid");
        boolean advanced = tag.contains("advanced") && tag.getBoolean("advanced");
        if(!disabled && !tag.contains("preset") && humanoid && advanced){
            AdvancedCustomHumanoidMobPatchProvider provider = new AdvancedCustomHumanoidMobPatchProvider();
            provider.attributeValues = deserializeAdvancedAttributes(tag.getCompound("attributes"));
            ResourceLocation modelLocation = new ResourceLocation(tag.getString("model"));
            ResourceLocation armatureLocation = new ResourceLocation(tag.getString("armature"));

            modelLocation = new ResourceLocation(modelLocation.getNamespace(), "animmodels/" + modelLocation.getPath() + ".json");
            armatureLocation = new ResourceLocation(armatureLocation.getNamespace(), "animmodels/" + armatureLocation.getPath() + ".json");

            if (EpicFightMod.isPhysicalClient()) {
                Minecraft mc = Minecraft.getInstance();
                Meshes.getOrCreateAnimatedMesh(mc.getResourceManager(), modelLocation, HumanoidMesh::new);
                Armature armature = Armatures.getOrCreateArmature(mc.getResourceManager(), armatureLocation, HumanoidArmature::new);
                Armatures.registerEntityTypeArmature(entityType, armature);
            } else {
                Armature armature = Armatures.getOrCreateArmature(null, armatureLocation, HumanoidArmature::new);
                Armatures.registerEntityTypeArmature(entityType, armature);
            }


            provider.defaultAnimations = deserializeDefaultAnimations(tag.getCompound("default_livingmotions"));
            provider.faction = Faction.valueOf(tag.getString("faction").toUpperCase(Locale.ROOT));
            provider.scale = tag.getCompound("attributes").contains("scale") ? (float)tag.getCompound("attributes").getDouble("scale") : 1.0F;
            provider.maxStamina = tag.getCompound("attributes").contains("max_stamina") ? (float)tag.getCompound("attributes").getDouble("max_stamina") : 15.0F;
            provider.maxStunShield = tag.getCompound("attributes").contains("max_stun_shield") ? (float)tag.getCompound("attributes").getDouble("max_stun_shield") : 0F;
            if (!clientSide) {
                provider.stunAnimations = deserializeStunAnimations(tag.getCompound("stun_animations"));
                provider.chasingSpeed = tag.getCompound("attributes").getDouble("chasing_speed");
                provider.humanoidCombatBehaviors = deserializeAdvancedCombatBehaviors(tag.getList("combat_behavior", 10));
                provider.humanoidWeaponMotions = deserializeHumanoidWeaponMotions(tag.getList("humanoid_weapon_motions", 10));
                provider.guardMotions = deserializeGuardMotions(tag.getList("custom_guard_motion",10));
                provider.regenStaminaStandbyTime = tag.getCompound("attributes").contains("stamina_regan_delay") ? tag.getCompound("attributes").getInt("stamina_regan_delay") : 30;
                provider.regenStaminaMultiply = tag.getCompound("attributes").contains("stamina_regan_multiply") ? (float)tag.getCompound("attributes").getDouble("stamina_regan_multiply") : 1F;
                provider.hasStunReduction = !tag.getCompound("attributes").contains("has_stun_reduction") || tag.getCompound("attributes").getBoolean("has_stun_reduction");
                provider.reganShieldStandbyTime = tag.getCompound("attributes").contains("stun_shield_regan_delay") ? tag.getCompound("attributes").getInt("stun_shield_regan_delay") : 30;
                provider.reganShieldMultiply = tag.getCompound("attributes").contains("stun_shield_regan_multiply") ? (float)tag.getCompound("attributes").getDouble("stun_shield_multiply") : 1F;
                provider.staminaLoseMultiply = tag.getCompound("attributes").contains("stamina_lose_multiply") ? (float)tag.getCompound("attributes").getDouble("stamina_lose_multiply") : 0F;
            }
            cir.setReturnValue(provider);
        }
    }

    @Inject(method = "extractBranch(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;", at = @At("HEAD"), cancellable = true)
    private static void onExtractBranch(CompoundTag extract, CompoundTag original, CallbackInfoReturnable<CompoundTag> cir) {
        if (original.contains("disabled") && original.getBoolean("disabled")) {
            extract.put("disabled", original.get("disabled"));
        } else if (original.contains("preset")) {
            extract.put("preset", original.get("preset"));
        } else {
            extract.put("model", original.get("model"));
            extract.putBoolean("isHumanoid", original.contains("isHumanoid") && original.getBoolean("isHumanoid"));
            extract.putBoolean("advanced", original.contains("advanced") && original.getBoolean("advanced"));
            extract.put("renderer", original.get("renderer"));
            extract.put("faction", original.get("faction"));
            extract.put("default_livingmotions", original.get("default_livingmotions"));
            extract.put("attributes", original.get("attributes"));
        }

        cir.setReturnValue(extract);
    }
}
