package com.nameless.indestructible.data;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.nameless.indestructible.api.animation.types.LivingEntityPatchEvent;
import com.nameless.indestructible.compat.kubejs.PatchJSPlugin;
import com.nameless.indestructible.gameasset.GuardAnimations;
import com.nameless.indestructible.main.Indestructible;
import com.nameless.indestructible.server.network.SPDatapackSync;
import com.nameless.indestructible.world.ai.CombatBehaviors.*;
import com.nameless.indestructible.world.capability.AdvancedCustomHumanoidMobPatch;
import com.nameless.indestructible.world.capability.AdvancedCustomMobPatch;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.compress.utils.Lists;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.collider.MultiOBBCollider;
import yesman.epicfight.api.collider.OBBCollider;
import yesman.epicfight.api.data.reloader.MobPatchReloadListener;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.model.armature.HumanoidArmature;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.Faction;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.capabilities.provider.EntityPatchProvider;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

import java.util.*;
import java.util.stream.Stream;

import static yesman.epicfight.api.data.reloader.MobPatchReloadListener.*;

public class AdvancedMobpatchReloader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = (new GsonBuilder()).create();
    private static final Map<EntityType<?>, CompoundTag> TAGMAP = Maps.newHashMap();
    private static final Map<EntityType<?>, MobPatchReloadListener.AbstractMobPatchProvider> ADVANCED_MOB_PATCH_PROVIDERS = Maps.newHashMap();
    public static void addProvider(EntityType<?> entityType, MobPatchReloadListener.AbstractMobPatchProvider provider){
        ADVANCED_MOB_PATCH_PROVIDERS.put(entityType, provider);
        EntityPatchProvider.putCustomEntityPatch(entityType, (entity) -> () -> ADVANCED_MOB_PATCH_PROVIDERS.get(entity.getType()).get(entity));
    }
    public static void addClientTag(EntityType<?> entityType, CompoundTag tag){
        TAGMAP.put(entityType, tag);
    }


    public AdvancedMobpatchReloader() {
        super(GSON, "advanced_mobpatch");
    }
    @Override
    protected Map<ResourceLocation, JsonElement> prepare(ResourceManager resourceManager, ProfilerFiller profileIn) {
        ADVANCED_MOB_PATCH_PROVIDERS.clear();
        TAGMAP.clear();
        return super.prepare(resourceManager, profileIn);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
        for (Map.Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet()) {
            ResourceLocation rl = entry.getKey();
            String pathString = rl.getPath();
            ResourceLocation registryName = new ResourceLocation(rl.getNamespace(), pathString);

            if (!ForgeRegistries.ENTITY_TYPES.containsKey(registryName)) {
                Indestructible.LOGGER.warn("[Custom Entity] Entity named " + registryName + " does not exist");
                continue;
            }

            EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(registryName);
            CompoundTag tag = null;

            try {
                tag = TagParser.parseTag(entry.getValue().toString());
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }

            ADVANCED_MOB_PATCH_PROVIDERS.put(entityType, deserializePatchProvider(entityType, tag, false, resourceManagerIn));

            EntityPatchProvider.putCustomEntityPatch(entityType, (entity) -> () -> ADVANCED_MOB_PATCH_PROVIDERS.get(entity.getType()).get(entity));
            TAGMAP.put(entityType, filterClientData(tag));

            if (EpicFightMod.isPhysicalClient()) {
                ClientEngine.getInstance().renderEngine.registerCustomEntityRenderer(entityType, tag.contains("preset") ? tag.getString("preset") : tag.getString("renderer"),tag);
            }
        }
        if(ModList.get().isLoaded("kubejs")){
            AdvancedMobPatchProviderEvent event = new AdvancedMobPatchProviderEvent();
            PatchJSPlugin.REGISTRY.post(event);
        }

    }
    public static AbstractMobPatchProvider deserializePatchProvider(EntityType<?> entityType, CompoundTag tag, boolean clientSide, ResourceManager resourceManager){
        boolean humanoid = tag.getBoolean("isHumanoid");
        return humanoid ? deserializeHumaniodMobPatchProvider(entityType, tag, clientSide, resourceManager) : deserializeMobPatchProvider(entityType, tag, clientSide, resourceManager);
    }

    public static AdvancedCustomMobPatchProvider deserializeMobPatchProvider(EntityType<?> entityType, CompoundTag tag, boolean clientSide, ResourceManager resourceManager) {
        AdvancedCustomMobPatchProvider provider = new AdvancedCustomHumanoidMobPatchProvider();
        provider.attributeValues = deserializeAdvancedAttributes(tag.getCompound("attributes"));
        ResourceLocation modelLocation = new ResourceLocation(tag.getString("model"));
        ResourceLocation armatureLocation = new ResourceLocation(tag.getString("armature"));

        if (EpicFightMod.isPhysicalClient()) {
            Meshes.getOrCreateAnimatedMesh(Minecraft.getInstance().getResourceManager(), modelLocation, HumanoidMesh::new);
            provider.name = tag.contains("boss_bar") && tag.contains("custom_name") ? tag.getString("custom_name") : null;
            provider.bossBar = tag.contains("boss_bar") && tag.contains("custom_texture") ? ResourceLocation.tryParse(tag.getString("custom_texture")) : null;
        }

        Armature armature = Armatures.getOrCreateArmature(resourceManager, armatureLocation, HumanoidArmature::new);
        Armatures.registerEntityTypeArmature(entityType, armature);

        provider.hasBossBar = tag.contains("boss_bar") && tag.getBoolean("boss_bar");

        provider.defaultAnimations = deserializeDefaultAnimations(tag.getCompound("default_livingmotions"));
        provider.faction = Faction.valueOf(tag.getString("faction").toUpperCase(Locale.ROOT));
        provider.scale = tag.getCompound("attributes").contains("scale") ? (float)tag.getCompound("attributes").getDouble("scale") : 1.0F;
        provider.maxStunShield = tag.getCompound("attributes").contains("max_stun_shield") ? (float)tag.getCompound("attributes").getDouble("max_stun_shield") : 0F;
        if (tag.contains("swing_sound")) {
            provider.swingSound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(tag.getString("swing_sound")));
        }

        if (tag.contains("hit_sound")) {
            provider.hitSound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(tag.getString("hit_sound")));
        }

        if (tag.contains("hit_particle")) {
            provider.hitParticle = (HitParticleType)ForgeRegistries.PARTICLE_TYPES.getValue(new ResourceLocation(tag.getString("hit_particle")));
        }
        if (!clientSide) {
            provider.stunAnimations = deserializeStunAnimations(tag.getCompound("stun_animations"));
            provider.chasingSpeed = tag.getCompound("attributes").getDouble("chasing_speed");
            provider.defaultGuardMotion = deserializeGuardMotions(tag.getCompound("custom_guard_motion"));
            provider.combatBehaviorsBuilder = deserializeAdvancedBehaviorsBuilder(tag.getList("combat_behavior", 10));
            provider.regenStaminaStandbyTime = tag.getCompound("attributes").contains("stamina_regan_delay") ? tag.getCompound("attributes").getInt("stamina_regan_delay") : 30;
            provider.hasStunReduction = !tag.getCompound("attributes").contains("has_stun_reduction") || tag.getCompound("attributes").getBoolean("has_stun_reduction");
            provider.reganShieldStandbyTime = tag.getCompound("attributes").contains("stun_shield_regan_delay") ? tag.getCompound("attributes").getInt("stun_shield_regan_delay") : 30;
            provider.reganShieldMultiply = tag.getCompound("attributes").contains("stun_shield_regan_multiply") ? (float)tag.getCompound("attributes").getDouble("stun_shield_multiply") : 1F;
            provider.staminaLoseMultiply = tag.getCompound("attributes").contains("stamina_lose_multiply") ? (float)tag.getCompound("attributes").getDouble("stamina_lose_multiply") : 0F;
            provider.attackRadius = tag.getCompound("attributes").contains("attack_radius") ? (float)tag.getCompound("attributes").getDouble("attack_radius") : 1.5F;
            provider.guardRadius = tag.getCompound("attributes").contains("guard_radius") ? (float)tag.getCompound("attributes").getDouble("guard_radius") : 3F;
            provider.stunEvent = deserializeStunCommandList(tag.getList("stun_command_list", 10));
        }
        return provider;
    }

    public static AdvancedCustomHumanoidMobPatchProvider deserializeHumaniodMobPatchProvider(EntityType<?> entityType, CompoundTag tag, boolean clientSide, ResourceManager resourceManager) {
            AdvancedCustomHumanoidMobPatchProvider provider = new AdvancedCustomHumanoidMobPatchProvider();
            provider.attributeValues = deserializeAdvancedAttributes(tag.getCompound("attributes"));
            ResourceLocation modelLocation = new ResourceLocation(tag.getString("model"));
            ResourceLocation armatureLocation = new ResourceLocation(tag.getString("armature"));

            if (EpicFightMod.isPhysicalClient()) {
                Meshes.getOrCreateAnimatedMesh(Minecraft.getInstance().getResourceManager(), modelLocation, HumanoidMesh::new);
                provider.name = tag.contains("boss_bar") && tag.contains("custom_name") ? tag.getString("custom_name") : null;
                provider.bossBar = tag.contains("boss_bar") && tag.contains("custom_texture") ? ResourceLocation.tryParse(tag.getString("custom_texture")) : null;
            }

            Armature armature = Armatures.getOrCreateArmature(resourceManager, armatureLocation, HumanoidArmature::new);
            Armatures.registerEntityTypeArmature(entityType, armature);

            provider.hasBossBar = tag.contains("boss_bar") && tag.getBoolean("boss_bar");
            provider.defaultAnimations = deserializeDefaultAnimations(tag.getCompound("default_livingmotions"));
            provider.faction = Faction.valueOf(tag.getString("faction").toUpperCase(Locale.ROOT));
            provider.scale = tag.getCompound("attributes").contains("scale") ? (float)tag.getCompound("attributes").getDouble("scale") : 1.0F;
            if (tag.contains("swing_sound")) {
                provider.swingSound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(tag.getString("swing_sound")));
            }

            if (tag.contains("hit_sound")) {
                provider.hitSound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(tag.getString("hit_sound")));
            }

            if (tag.contains("hit_particle")) {
                provider.hitParticle = (HitParticleType)ForgeRegistries.PARTICLE_TYPES.getValue(new ResourceLocation(tag.getString("hit_particle")));
            }
            if (!clientSide) {
                provider.stunAnimations = deserializeStunAnimations(tag.getCompound("stun_animations"));
                provider.chasingSpeed = tag.getCompound("attributes").getDouble("chasing_speed");
                provider.AHCombatBehaviors = deserializeAdvancedHumanoidCombatBehaviors(tag.getList("combat_behavior", 10));
                provider.AHWeaponMotions = deserializeHumanoidWeaponMotions(tag.getList("humanoid_weapon_motions", 10));
                provider.guardMotions = deserializeHumanoidGuardMotions(tag.getList("custom_guard_motion",10));
                provider.regenStaminaStandbyTime = tag.getCompound("attributes").contains("stamina_regan_delay") ? tag.getCompound("attributes").getInt("stamina_regan_delay") : 30;
                provider.hasStunReduction = !tag.getCompound("attributes").contains("has_stun_reduction") || tag.getCompound("attributes").getBoolean("has_stun_reduction");
                provider.maxStunShield = tag.getCompound("attributes").contains("max_stun_shield") ? (float)tag.getCompound("attributes").getDouble("max_stun_shield") : 0F;
                provider.reganShieldStandbyTime = tag.getCompound("attributes").contains("stun_shield_regan_delay") ? tag.getCompound("attributes").getInt("stun_shield_regan_delay") : 30;
                provider.reganShieldMultiply = tag.getCompound("attributes").contains("stun_shield_regan_multiply") ? (float)tag.getCompound("attributes").getDouble("stun_shield_multiply") : 1F;
                provider.staminaLoseMultiply = tag.getCompound("attributes").contains("stamina_lose_multiply") ? (float)tag.getCompound("attributes").getDouble("stamina_lose_multiply") : 0F;
                provider.attackRadius = tag.getCompound("attributes").contains("attack_radius") ? (float)tag.getCompound("attributes").getDouble("attack_radius") : 1.5F;
                provider.guardRadius = tag.getCompound("attributes").contains("guard_radius") ? (float)tag.getCompound("attributes").getDouble("guard_radius") : 3F;
                provider.stunEvent = deserializeStunCommandList(tag.getList("stun_command_list", 10));
            }
            return provider;
    }

    public static CompoundTag filterClientData(CompoundTag tag) {
        CompoundTag clientTag = new CompoundTag();
        return extractBranch(clientTag, tag);
    }

    public static CompoundTag extractBranch(CompoundTag extract, CompoundTag original) {
        extract.put("model", original.get("model"));
        extract.put("armature", original.get("armature"));
        extract.putBoolean("isHumanoid", original.contains("isHumanoid") ? original.getBoolean("isHumanoid") : false);
        extract.put("renderer", original.get("renderer"));
        extract.put("faction", original.get("faction"));
        extract.put("default_livingmotions", original.get("default_livingmotions"));
        extract.put("attributes", original.get("attributes"));
        if(original.contains("boss_bar")){
            extract.put("boss_bar", original.get("boss_bar"));
            if(original.contains("custom_name"))extract.put("custom_name", original.get("custom_name"));
            if(original.contains("custom_texture"))extract.put("custom_texture", original.get("custom_texture"));
        }
        return extract;
    }

    public static Stream<CompoundTag> getDataStream() {
        Stream<CompoundTag> tagStream = TAGMAP.entrySet().stream().map((entry) -> {
            entry.getValue().putString("id", ForgeRegistries.ENTITY_TYPES.getKey(entry.getKey()).toString());
            return entry.getValue();
        });

        return tagStream;
    }

    public static int getTagCount() {
        return TAGMAP.size();
    }

    @OnlyIn(Dist.CLIENT)
    public static void processServerPacket(SPDatapackSync packet) {
        for (CompoundTag tag : packet.getTags()) {
            EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(tag.getString("id")));
            ADVANCED_MOB_PATCH_PROVIDERS.put(entityType, deserializePatchProvider(entityType, tag, true, Minecraft.getInstance().getResourceManager()));
            EntityPatchProvider.putCustomEntityPatch(entityType, (entity) -> () -> ADVANCED_MOB_PATCH_PROVIDERS.get(entity.getType()).get(entity));
            Minecraft mc = Minecraft.getInstance();
            ResourceLocation armatureLocation = new ResourceLocation(tag.getString("armature"));
            boolean humanoid = tag.getBoolean("isHumanoid");
            Armature armature = Armatures.getOrCreateArmature(mc.getResourceManager(), armatureLocation, humanoid ? Armature::new : HumanoidArmature::new);
            Armatures.registerEntityTypeArmature(entityType, armature);
            ClientEngine.getInstance().renderEngine.registerCustomEntityRenderer(entityType, tag.getString("renderer"), tag);
        }
    }

    public static class AdvancedCustomMobPatchProvider extends MobPatchReloadListener.AbstractMobPatchProvider {
        protected Faction faction = Faction.NEUTRAL;
        protected CombatBehaviors.Builder<MobPatch<?>> combatBehaviorsBuilder;
        protected int regenStaminaStandbyTime = 30;
        protected boolean hasStunReduction = true;
        protected float maxStunShield = 0;
        protected int reganShieldStandbyTime = 30;
        protected float reganShieldMultiply = 1;
        protected float staminaLoseMultiply = 0;
        protected float guardRadius = 3F;
        protected float attackRadius = 1.5F;
        protected List<Pair<LivingMotion, StaticAnimation>> defaultAnimations = new ArrayList<>();
        protected Map<StunType, StaticAnimation> stunAnimations = Maps.newHashMap();
        protected Map<Attribute, Double> attributeValues = Maps.newHashMap();
        protected double chasingSpeed = 1;
        protected float scale = 1;
        protected boolean hasBossBar = false;
        protected ResourceLocation bossBar;
        protected String name;
        protected GuardMotion defaultGuardMotion;
        protected List<LivingEntityPatchEvent.StunEvent> stunEvent = new ArrayList<>();
        public AdvancedCustomMobPatchProvider(){}
        @SuppressWarnings("rawtypes")
        public EntityPatch<?> get(Entity entity) {
            return new AdvancedCustomMobPatch(faction, this);
        }
        public List<Pair<LivingMotion, StaticAnimation>> getDefaultAnimations() {
            return this.defaultAnimations;
        }
        public Map<StunType, StaticAnimation> getStunAnimations() {
            return this.stunAnimations;
        }
        public Map<Attribute, Double> getAttributeValues() {
            return this.attributeValues;
        }
        public double getChasingSpeed() {
            return this.chasingSpeed;
        }
        public float getScale() {
            return this.scale;
        }
        //stamina
        public int getRegenStaminaStandbyTime(){return this.regenStaminaStandbyTime;}
        //stun
        public boolean hasStunReduction(){return this.hasStunReduction;}
        public float getMaxStunShield(){return this.maxStunShield;}
        public int getReganShieldStandbyTime(){return this.reganShieldStandbyTime;}
        public float getReganShieldMultiply() {return this.reganShieldMultiply;}
        public float getStaminaLoseMultiply(){return this.staminaLoseMultiply;}
        public float getGuardRadius(){return this.guardRadius;}
        public float getAttackRadius(){return this.attackRadius;}
        public List<LivingEntityPatchEvent.StunEvent> getStunEvent(){
            return this.stunEvent;
        }
        public boolean hasBossBar(){return this.hasBossBar;}
        public String getName(){return this.name;}
        public ResourceLocation getBossBar(){return this.bossBar;}
        public CombatBehaviors.Builder<MobPatch<?>> getCombatBehaviorsBuilder() {
            return this.combatBehaviorsBuilder;
        }
        public GuardMotion getGuardMotion(){
            return this.defaultGuardMotion;
        }
        protected SoundEvent swingSound = EpicFightSounds.WHOOSH.get();
        protected SoundEvent hitSound = EpicFightSounds.BLUNT_HIT.get();
        protected HitParticleType hitParticle = EpicFightParticles.HIT_BLUNT.get();

        public SoundEvent getSwingSound() {
            return this.swingSound;
        }

        public SoundEvent getHitSound() {
            return this.hitSound;
        }

        public HitParticleType getHitParticle() {
            return this.hitParticle;
        }
    }


    public static class AdvancedCustomHumanoidMobPatchProvider extends AdvancedCustomMobPatchProvider {
        protected Map<WeaponCategory, Map<Style, CombatBehaviors.Builder<HumanoidMobPatch<?>>>> AHCombatBehaviors = Maps.newHashMap();
        protected Map<WeaponCategory, Map<Style, Set<Pair<LivingMotion, StaticAnimation>>>> AHWeaponMotions = Maps.newHashMap();
        protected Map<WeaponCategory, Map<Style, GuardMotion>> guardMotions = Maps.newHashMap();
        public AdvancedCustomHumanoidMobPatchProvider() {
        }

        @SuppressWarnings("rawtypes")
        public EntityPatch<?> get(Entity entity) {
            return new AdvancedCustomHumanoidMobPatch(this.faction, this);
        }

        public Map<WeaponCategory, Map<Style, Set<Pair<LivingMotion, StaticAnimation>>>> getHumanoidWeaponMotions() {
            return this.AHWeaponMotions;
        }

        public Map<WeaponCategory, Map<Style, CombatBehaviors.Builder<HumanoidMobPatch<?>>>> getHumanoidCombatBehaviors() {
            return this.AHCombatBehaviors;
        }

        public Map<WeaponCategory, Map<Style, GuardMotion>> getGuardMotions(){
            return this.guardMotions;
        }
    }
    public static Map<Attribute, Double> deserializeAdvancedAttributes(CompoundTag tag) {
        Map<Attribute, Double> attributes = Maps.newHashMap();
        attributes.put(EpicFightAttributes.WEIGHT.get(), tag.contains("weight") ? tag.getDouble("weight") : 40);
        attributes.put(EpicFightAttributes.IMPACT.get(), tag.contains("impact") ? tag.getDouble("impact") : 0.5);
        attributes.put(EpicFightAttributes.ARMOR_NEGATION.get(), tag.contains("armor_negation") ? tag.getDouble("armor_negation") : 0.0);
        attributes.put(EpicFightAttributes.MAX_STAMINA.get(), tag.contains("max_stamina") ? tag.getDouble("max_stamina") : 15.0);
        attributes.put(EpicFightAttributes.STAMINA_REGEN.get(), tag.contains("stamina_regan_multiply") ? tag.getDouble("stamina_regan_multiply") : 1.0F);
        attributes.put(EpicFightAttributes.MAX_STRIKES.get(), (double)(tag.contains("max_strikes", 3) ? tag.getInt("max_strikes") : 1));
        if (tag.contains("attack_damage", 6)) {
            attributes.put(Attributes.ATTACK_DAMAGE, tag.getDouble("attack_damage"));
        }

        return attributes;
    }

    public static Map<WeaponCategory, Map<Style, CombatBehaviors.Builder<HumanoidMobPatch<?>>>> deserializeAdvancedHumanoidCombatBehaviors(ListTag tag) {
        Map<WeaponCategory, Map<Style, CombatBehaviors.Builder<HumanoidMobPatch<?>>>> combatBehaviorsMapBuilder = Maps.newHashMap();

        for (int i = 0; i < tag.size(); i++) {
            CompoundTag combatBehavior = tag.getCompound(i);
            ListTag categories = combatBehavior.getList("weapon_categories", 8);
            Style style = Style.ENUM_MANAGER.get(combatBehavior.getString("style"));
            CombatBehaviors.Builder<HumanoidMobPatch<?>> builder = deserializeAdvancedBehaviorsBuilder(combatBehavior.getList("behavior_series", 10));

            for (int j = 0; j < categories.size(); j++) {
                WeaponCategory category = WeaponCategory.ENUM_MANAGER.get(categories.getString(j));
                combatBehaviorsMapBuilder.computeIfAbsent(category, (key) -> Maps.newHashMap());
                combatBehaviorsMapBuilder.get(category).put(style, builder);
            }
        }

        return combatBehaviorsMapBuilder;
    }

    public static Map<WeaponCategory, Map<Style, GuardMotion>> deserializeHumanoidGuardMotions(ListTag tag){
        Map<WeaponCategory, Map<Style,GuardMotion>> map = Maps.newHashMap();

        for (int i = 0; i < tag.size(); i++) {
            CompoundTag list = tag.getCompound(i);
            Style style = Style.ENUM_MANAGER.get(list.getString("style"));
            GuardMotion guardMotion = deserializeGuardMotions(list);
            Tag weponTypeTag = list.get("weapon_categories");
            if (weponTypeTag instanceof StringTag) {
                WeaponCategory weaponCategory = WeaponCategory.ENUM_MANAGER.get(weponTypeTag.getAsString());
                if (!map.containsKey(weaponCategory)) {
                    map.put(weaponCategory, Maps.newHashMap());
                }
                map.get(weaponCategory).put(style, guardMotion);

            } else if (weponTypeTag instanceof ListTag weponTypesTag) {

                for (int j = 0; j < weponTypesTag.size(); j++) {
                    WeaponCategory weaponCategory = WeaponCategory.ENUM_MANAGER.get(weponTypesTag.getString(j));
                    if (!map.containsKey(weaponCategory)) {
                        map.put(weaponCategory, Maps.newHashMap());
                    }
                    map.get(weaponCategory).put(style, guardMotion);
                }
            }
        }
        return map;
    }

    private static <T extends MobPatch<?>> CombatBehaviors.Builder<T> deserializeAdvancedBehaviorsBuilder(ListTag tag) {
        CombatBehaviors.Builder<T> builder = CombatBehaviors.builder();

        for (int i = 0; i < tag.size(); i++) {
            CompoundTag behaviorSeries = tag.getCompound(i);
            float weight = (float)behaviorSeries.getDouble("weight");
            int cooldown = behaviorSeries.contains("cooldown") ? behaviorSeries.getInt("cooldown") : 0;
            boolean canBeInterrupted = behaviorSeries.contains("canBeInterrupted") && behaviorSeries.getBoolean("canBeInterrupted");
            boolean looping = behaviorSeries.contains("looping") && behaviorSeries.getBoolean("looping");
            ListTag behaviorList = behaviorSeries.getList("behaviors", 10);
            CombatBehaviors.BehaviorSeries.Builder<T> behaviorSeriesBuilder = CombatBehaviors.BehaviorSeries.builder();
            behaviorSeriesBuilder.weight(weight).cooldown(cooldown).canBeInterrupted(canBeInterrupted).looping(looping);

            for (int j = 0; j < behaviorList.size(); j++) {
                //CombatBehaviors.Behavior.Builder<T> behaviorBuilder = CombatBehaviors.Behavior.builder();
                AdvancedBehaviorBuilder<T> behaviorBuilder = new AdvancedBehaviorBuilder<>();
                CompoundTag behavior = behaviorList.getCompound(j);
                ListTag conditionList = behavior.getList("conditions", 10);
                if(behavior.contains("set_phase")) {
                    behaviorBuilder.tryProcessSetPhase(behavior.getInt("set_phase"));
                }
                if(behavior.contains("end_by_hurt_level")){
                    behaviorBuilder.tryProcessSetHurtResistLevel(behavior.getInt("end_by_hurt_level"));
                }
                if(behavior.contains("animation")) {
                    StaticAnimation animation = AnimationManager.getInstance().byKeyOrThrow(behavior.getString("animation"));
                    AnimationMotionSet motionSet = new AnimationMotionSet(animation, 0F, 1F,0F);
                    motionSet = behavior.contains("play_speed") ? motionSet.setSpeed ((float) behavior.getDouble("play_speed")) : motionSet;
                    motionSet = behavior.contains("stamina") ? motionSet.setStaminaCost((float) behavior.getDouble("stamina")) : motionSet;
                    motionSet = behavior.contains("convert_time") ? motionSet.setConvertTime((float)behavior.getDouble("convert_time")) : motionSet;
                    motionSet = behavior.contains("damage_modifier") && !behavior.getCompound("damage_modifier").isEmpty() ? motionSet.setDamageSourceModifier(deserializeDamageModifier(behavior.getCompound("damage_modifier"))) : motionSet;
                    motionSet = behavior.contains("command_list") && !behavior.getList("command_list", 10).isEmpty() ? motionSet.addTimeStampedEvents(deserializeTimeCommandList(behavior.getList("command_list", 10))) : motionSet;
                    motionSet = behavior.contains("hit_command_list") && !behavior.getList("hit_command_list", 10).isEmpty() ?  motionSet.addHitEvents(deserializeHitCommandList(behavior.getList("hit_command_list", 10))) : motionSet;
                    motionSet = behavior.contains("blocked_command_list") && !behavior.getList("blocked_command_list",10).isEmpty() ? motionSet.addBlockedEvents(deserializeBlockedCommandList(behavior.getList("blocked_command_list",10)) ) : motionSet;
                    behaviorBuilder.tryProcessAnimationSet(motionSet);
                    //behaviorBuilder.behavior(customAttackAnimation(motionSet, hurt_level, phase));
                } else if (behavior.contains("guard")){
                    int guard_time = behavior.getInt("guard");
                    GuardMotionSet motionSet = new GuardMotionSet(guard_time, 0, 0);
                    CounterMotion counterMotion = new CounterMotion(GuardAnimations.MOB_COUNTER_ATTACK, 3F, 0.3F, 1F, 0F,true);
                    motionSet = behavior.contains("parry_times") ? motionSet.setParryTimes(behavior.getInt("parry_times")) : motionSet;
                    motionSet = behavior.contains("stun_immunity_time") ? motionSet.setStunImmunityTime(behavior.getInt("stun_immunity_time")) : motionSet;
                    counterMotion = behavior.contains("counter") ? counterMotion.setCounterAnimation(behavior.getString("counter")) : counterMotion;
                    counterMotion = behavior.contains("counter_cost") ? counterMotion.setCost((float) behavior.getDouble("counter_cost")) : counterMotion;
                    counterMotion = behavior.contains("counter_chance") ? counterMotion.setChance((float)behavior.getDouble("counter_chance")) : counterMotion;
                    counterMotion = behavior.contains("counter_speed") ? counterMotion.setSpeed((float)behavior.getDouble("counter_speed")) : counterMotion;
                    counterMotion = behavior.contains("counter_convert_time") ? counterMotion.setConvertTime((float)behavior.getDouble("counter_convert_time")) : counterMotion;
                    counterMotion = behavior.contains("cancel_after_counter") ? counterMotion.cancelBlock(behavior.getBoolean("cancel_after_counter")) : counterMotion;
                    motionSet = motionSet.setCounterMotion(counterMotion);
                    motionSet = behavior.contains("specific_guard_motion") ? motionSet.setSpecificGuardMotion(deserializeGuardMotions(behavior.getCompound("specific_guard_motion"))) : motionSet;
                    behaviorBuilder.tryProcessGuardMotion(motionSet);
                    //behaviorBuilder.behavior(setGuardMotion(motionSet, phase, hurt_level));
                } else if (behavior.contains("wander")){
                    int strafingTime = behavior.getInt("wander");
                    WanderMotionSet motionSet = new WanderMotionSet(strafingTime, strafingTime, 0,0);
                    motionSet = behavior.contains("inaction_time") ?  motionSet.setInactionTime(behavior.getInt("inaction_time"))  : motionSet;
                    motionSet = behavior.contains("z_axis") ? motionSet.setForwardDirection((float) behavior.getDouble("z_axis")) : motionSet;
                    motionSet = behavior.contains("x_axis") ? motionSet.setClockwise ((float) behavior.getDouble("x_axis")) : motionSet;
                    behaviorBuilder.tryProcessWanderSet(motionSet);
                    //behaviorBuilder.behavior(setStrafing(motionSet, phase, hurt_level));
                }
                behaviorBuilder.process();

                for (int k = 0; k < conditionList.size(); k++) {
                    CompoundTag condition = conditionList.getCompound(k);
                    Condition<T> predicate = deserializeBehaviorPredicate(condition.getString("predicate"), condition);
                    behaviorBuilder.predicate(predicate);
                }

                behaviorSeriesBuilder.nextBehavior(behaviorBuilder);
            }

            builder.newBehaviorSeries(behaviorSeriesBuilder);
        }

        return builder;
    }

    public static GuardMotion deserializeGuardMotions(CompoundTag args){
        GuardMotion guardMotion = new GuardMotion(Animations.DUMMY_ANIMATION, false, 0F);
        if(args.contains("guard")) guardMotion = guardMotion.setGuardAnimation(args.getString("guard"));
        if(args.contains("stamina_cost_multiply")) guardMotion = guardMotion.setCost((float) args.getDouble("stamina_cost_multiply"));
        if(args.contains("can_block_projectile")) guardMotion = guardMotion.canBlockProjectile(args.getBoolean("can_block_projectile"));
        if(args.contains("parry_cost_multiply")) guardMotion = guardMotion.setParryCost((float) args.getDouble("parry_cost_multiply"));
        if(args.contains("parry_animation")) {
            StaticAnimation[] parry_animations;
            ListTag animationId = args.getList("parry_animation", 8);
            parry_animations = new StaticAnimation[animationId.size()];
            for (int j = 0; j < animationId.size(); j++) {
                StaticAnimation parry_animation = AnimationManager.getInstance().byKeyOrThrow(animationId.getString(j));
                parry_animations[j] = parry_animation;
            }
            guardMotion =  guardMotion.setParryAnimations(parry_animations);
        }
        return guardMotion;
    }



    public static LivingEntityPatchEvent.TimeStampedEvent[] deserializeTimeCommandList(ListTag args){
        LivingEntityPatchEvent.TimeStampedEvent[] list = new LivingEntityPatchEvent.TimeStampedEvent[args.size()];
        for(int k = 0; k < args.size(); k++){
            CompoundTag command = args.getCompound(k);
            boolean execute_at_target = command.contains("execute_at_target") && command.getBoolean("execute_at_target");
            LivingEntityPatchEvent.TimeStampedEvent event = LivingEntityPatchEvent.TimeStampedEvent.CreateTimeCommandEvent(command.getFloat("time"), command.getString("command"), execute_at_target);
            list[k] = event;
        }
        return list;
    }

    public static LivingEntityPatchEvent.BiEvent[] deserializeHitCommandList(ListTag args){
        LivingEntityPatchEvent.BiEvent[] list = new LivingEntityPatchEvent.BiEvent[0];
        for(int k = 0; k < args.size(); k++){
            CompoundTag command = args.getCompound(k);
            boolean execute_at_target = command.contains("execute_at_target") && command.getBoolean("execute_at_target");
            LivingEntityPatchEvent.BiEvent event = LivingEntityPatchEvent.BiEvent.CreateBiCommandEvent(command.getString("command"), execute_at_target);
            list[k] = event;
        }
        return list;
    }

    public static List<LivingEntityPatchEvent.StunEvent> deserializeStunCommandList(ListTag args){
        List<LivingEntityPatchEvent.StunEvent> list = Lists.newArrayList();
        for(int k = 0; k < args.size(); k++){
            CompoundTag command = args.getCompound(k);
            boolean execute_at_target = command.contains("execute_at_target") && command.getBoolean("execute_at_target");
            LivingEntityPatchEvent.StunEvent event = LivingEntityPatchEvent.StunEvent.CreateStunCommandEvent(command.getString("command"), execute_at_target, StunType.valueOf(command.getString("stun_type").toUpperCase(Locale.ROOT)));
            list.add(event);
        }
        return list;
    }

    public static LivingEntityPatchEvent.BlockedEvent[] deserializeBlockedCommandList(ListTag args){
        LivingEntityPatchEvent.BlockedEvent[] list = new LivingEntityPatchEvent.BlockedEvent[args.size()];
        for(int k = 0; k < args.size(); k++){
            CompoundTag command = args.getCompound(k);
            boolean execute_at_target = command.contains("execute_at_target") && command.getBoolean("execute_at_target");
            LivingEntityPatchEvent.BlockedEvent event = LivingEntityPatchEvent.BlockedEvent.CreateBlockCommandEvent(command.getString("command"), execute_at_target, command.getBoolean("is_parry"));
            list[k] = event;
        }
        return list;
    }

    public static DamageSourceModifier deserializeDamageModifier(CompoundTag args){
        float damage = args.contains("damage") ? args.getFloat("damage") : 1F;
        float impact = args.contains("impact") ? args.getFloat("impact") : 1F;
        float armor_negation = args.contains("armor_negation") ? args.getFloat("armor_negation") : 1F;
        DamageSourceModifier modifier = new DamageSourceModifier(damage, impact, armor_negation);
        if(args.contains("stun_type")) modifier.setStunType(args.getString("stun_type").toUpperCase(Locale.ROOT));
        if(args.contains("collider")) modifier.setCollider(deserializeCollider(args.getCompound("collider")));
        return new DamageSourceModifier(damage, impact, armor_negation);
    }

    public static Collider deserializeCollider(CompoundTag tag) {
        int number = tag.getInt("number");

        if (number < 1) {
            EpicFightMod.LOGGER.warn("Datapack deserialization error: the number of colliders must bigger than 0! ");
            return null;
        }

        ListTag sizeVector = tag.getList("size", 6);
        ListTag centerVector = tag.getList("center", 6);

        double sizeX = sizeVector.getDouble(0);
        double sizeY = sizeVector.getDouble(1);
        double sizeZ = sizeVector.getDouble(2);

        double centerX = centerVector.getDouble(0);
        double centerY = centerVector.getDouble(1);
        double centerZ = centerVector.getDouble(2);

        if (sizeX < 0 || sizeY < 0 || sizeZ < 0) {
            EpicFightMod.LOGGER.warn("Datapack deserialization error: the size of the collider must be non-negative! ");
            return null;
        }

        if (number == 1) {
            return new OBBCollider(sizeX, sizeY, sizeZ, centerX, centerY, centerZ);
        } else {
            return new MultiOBBCollider(number, sizeX, sizeY, sizeZ, centerX, centerY, centerZ);
        }
    }
}
