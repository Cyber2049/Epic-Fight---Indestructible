package com.nameless.indestructible.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.nameless.indestructible.api.animation.types.LivingEntityPatchEvent;
import com.nameless.indestructible.gameasset.GuardAnimations;
import com.nameless.indestructible.main.Indestructible;
import com.nameless.indestructible.network.SPDatapackSync;
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
import net.minecraftforge.registries.ForgeRegistries;
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

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static com.nameless.indestructible.world.capability.Utils.BehaviorsUtils.*;
import static yesman.epicfight.api.data.reloader.MobPatchReloadListener.*;

public class AdvancedMobpatchReloader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = (new GsonBuilder()).create();
    private static final Map<EntityType<?>, CompoundTag> TAGMAP = Maps.newHashMap();
    private static final Map<EntityType<?>, MobPatchReloadListener.AbstractMobPatchProvider> ADVANCED_MOB_PATCH_PROVIDERS = Maps.newHashMap();

    public AdvancedMobpatchReloader() {
        super(GSON, "advanced_mobpatch");
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
        }

        Armature armature = Armatures.getOrCreateArmature(resourceManager, armatureLocation, HumanoidArmature::new);
        Armatures.registerEntityTypeArmature(entityType, armature);

        provider.hasBossBar = tag.contains("boss_bar") && tag.getBoolean("boss_bar");
        provider.name = tag.contains("boss_bar") && tag.contains("custom_name") ? tag.getString("custom_name") : null;
        provider.bossBar = tag.contains("boss_bar") && tag.contains("custom_texture") ? ResourceLocation.tryParse(tag.getString("custom_texture")) : null;


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
            }

            Armature armature = Armatures.getOrCreateArmature(resourceManager, armatureLocation, HumanoidArmature::new);
            Armatures.registerEntityTypeArmature(entityType, armature);

            provider.hasBossBar = tag.contains("boss_bar") && tag.getBoolean("boss_bar");
            provider.name = tag.contains("boss_bar") && tag.contains("custom_name") ? tag.getString("custom_name") : null;
            provider.bossBar = tag.contains("boss_bar") && tag.contains("custom_texture") ? ResourceLocation.tryParse(tag.getString("custom_texture")) : null;


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
                provider.AHCombatBehaviors = deserializeAdvancedHumanoidCombatBehaviors(tag.getList("combat_behavior", 10));
                provider.AHWeaponMotions = deserializeHumanoidWeaponMotions(tag.getList("humanoid_weapon_motions", 10));
                provider.guardMotions = deserializeHumanoidGuardMotions(tag.getList("custom_guard_motion",10));
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

    public static CompoundTag filterClientData(CompoundTag tag) {
        CompoundTag clientTag = new CompoundTag();
        extractBranch(clientTag, tag);
        return clientTag;
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
        protected Faction faction;
        protected CombatBehaviors.Builder<MobPatch<?>> combatBehaviorsBuilder;
        protected int regenStaminaStandbyTime;
        protected boolean hasStunReduction;
        protected float maxStunShield;
        protected int reganShieldStandbyTime;
        protected float reganShieldMultiply;
        protected float staminaLoseMultiply;
        protected float guardRadius;
        protected float attackRadius;
        protected List<Pair<LivingMotion, StaticAnimation>> defaultAnimations;
        protected Map<StunType, StaticAnimation> stunAnimations;
        protected Map<Attribute, Double> attributeValues;
        protected double chasingSpeed;
        protected float scale;
        protected boolean hasBossBar;
        protected ResourceLocation bossBar;
        protected String name;
        protected GuardMotion defaultGuardMotion;
        protected List<LivingEntityPatchEvent.StunEvent> stunEvent;
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
        protected Map<WeaponCategory, Map<Style, CombatBehaviors.Builder<HumanoidMobPatch<?>>>> AHCombatBehaviors;
        protected Map<WeaponCategory, Map<Style, Set<Pair<LivingMotion, StaticAnimation>>>> AHWeaponMotions;
        protected Map<WeaponCategory, Map<Style, GuardMotion>> guardMotions;
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
    public static GuardMotion deserializeGuardMotions(CompoundTag tag){
        StaticAnimation guard = tag.contains("guard") ? AnimationManager.getInstance().byKeyOrThrow(tag.getString("guard")) : GuardAnimations.MOB_LONGSWORD_GUARD;
        float guard_cost = tag.contains("stamina_cost_multiply") ? (float)tag.getDouble("stamina_cost_multiply") : 1F;
        boolean canBlockProjectile = tag.contains("can_block_projectile") && tag.getBoolean("can_block_projectile");
        float parry_cost = tag.contains("parry_cost_multiply") ? (float)tag.getDouble("parry_cost_multiply") : 0.5F;
        StaticAnimation[] parry_animations = null;
        if(tag.contains("parry_animation")){
            ListTag animationId = tag.getList("parry_animation", 8);
            parry_animations = new StaticAnimation[animationId.size()];
            for (int j = 0; j < animationId.size(); j++) {
                StaticAnimation parry_animation = AnimationManager.getInstance().byKeyOrThrow(animationId.getString(j));
                parry_animations[j] = parry_animation;
            }
        }
        return new GuardMotion(guard, canBlockProjectile, guard_cost, parry_cost, parry_animations);
    }

    public static Map<WeaponCategory, Map<Style, GuardMotion>> deserializeHumanoidGuardMotions(ListTag tag){
        Map<WeaponCategory, Map<Style,GuardMotion>> map = Maps.newHashMap();

        for (int i = 0; i < tag.size(); i++) {
            CompoundTag list = tag.getCompound(i);
            Style style = Style.ENUM_MANAGER.get(list.getString("style"));
            StaticAnimation guard = list.contains("guard") ? AnimationManager.getInstance().byKeyOrThrow(list.getString("guard")) : GuardAnimations.MOB_LONGSWORD_GUARD;
            float guard_cost = list.contains("stamina_cost_multiply") ? (float)list.getDouble("stamina_cost_multiply") : 1F;
            boolean canBlockProjectile = list.contains("can_block_projectile") && list.getBoolean("can_block_projectile");
            float parry_cost = list.contains("parry_cost_multiply") ? (float)list.getDouble("parry_cost_multiply") : 0.5F;
            StaticAnimation[] parry_animations = null;
            if(list.contains("parry_animation")){
                ListTag animationId = list.getList("parry_animation", 8);
                parry_animations = new StaticAnimation[animationId.size()];
                for (int j = 0; j < animationId.size(); j++) {
                    StaticAnimation parry_animation = AnimationManager.getInstance().byKeyOrThrow(animationId.getString(j));
                    parry_animations[j] = parry_animation;
                }
            }

            Tag weponTypeTag = list.get("weapon_categories");

            GuardMotion guardMotion = new GuardMotion(guard, canBlockProjectile, guard_cost, parry_cost, parry_animations);
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
                CombatBehaviors.Behavior.Builder<T> behaviorBuilder = CombatBehaviors.Behavior.builder();
                CompoundTag behavior = behaviorList.getCompound(j);
                ListTag conditionList = behavior.getList("conditions", 10);
                int phase = behavior.contains("set_phase") ? behavior.getInt("set_phase") : -1;
                int hurt_level = behavior.contains("end_by_hurt_level") ? behavior.getInt("end_by_hurt_level") : 2;
                if(behavior.contains("animation")) {
                    StaticAnimation animation = AnimationManager.getInstance().byKeyOrThrow(behavior.getString("animation"));
                    float speed = behavior.contains("play_speed") ? (float) behavior.getDouble("play_speed") : 1F;
                    float stamina = behavior.contains("stamina") ? (float) behavior.getDouble("stamina") : 0F;
                    float convertTime = behavior.contains("convert_time") ? (float)behavior.getDouble("convert_time") : 0F;
                    CustomAnimationMotion motion = new CustomAnimationMotion(animation,convertTime,speed,stamina);
                    List<LivingEntityPatchEvent.TimeStampedEvent> timeCommandList = behavior.contains("command_list") ? deserializeTimeCommandList(behavior.getList("command_list", 10)) : null;
                    List<LivingEntityPatchEvent.BiEvent> hitCommandList = behavior.contains("hit_command_list") ? deserializeHitCommandList(behavior.getList("hit_command_list", 10)) : null;
                    DamageSourceModifier modifier = behavior.contains("damage_modifier") ? deserializeDamageModifier(behavior.getCompound("damage_modifier")) : null;
                    List<LivingEntityPatchEvent.BlockedEvent> blockedEvents = behavior.contains("blocked_command_list") ? deserializeBlockedCommandList(behavior.getList("blocked_command_list",10)) : null;
                    behaviorBuilder.behavior(customAttackAnimation(motion, modifier, timeCommandList, hitCommandList, blockedEvents, phase, hurt_level));
                } else if (behavior.contains("guard")){
                    int guardTime = behavior.getInt("guard");
                    StaticAnimation counter = behavior.contains("counter") ? AnimationManager.getInstance().byKeyOrThrow(behavior.getString("counter")) : GuardAnimations.MOB_COUNTER_ATTACK;
                    int parry_times = behavior.contains("parry_times") ? behavior.getInt("parry_times") : 0;
                    int stun_immunity_time = behavior.contains("stun_immunity_time") ? behavior.getInt("stun_immunity_time") : 0;
                    float cost = behavior.contains("counter_cost") ? (float) behavior.getDouble("counter_cost") : 3.0F;
                    float chance = behavior.contains("counter_chance") ? (float)behavior.getDouble("counter_chance") : 0.3F;
                    float speed = behavior.contains("counter_speed") ? (float)behavior.getDouble("counter_speed") : 1F;
                    CounterMotion counterMotion = new CounterMotion(counter, cost, chance, speed);
                    boolean cancel = !behavior.contains("cancel_after_counter") || behavior.getBoolean("cancel_after_counter");
                    GuardMotion guardMotion = behavior.contains("specific_guard_motion") ? deserializeSpecificGuardMotion(behavior.getCompound("specific_guard_motion")) : null;
                    behaviorBuilder.behavior(setGuardMotion(guardTime, parry_times, stun_immunity_time, counterMotion, cancel, guardMotion, phase, hurt_level));
                } else if (behavior.contains("wander")){
                    int strafingTime = behavior.getInt("wander");
                    int inactionTime = behavior.contains("inaction_time") ?  behavior.getInt("inaction_time") : behavior.getInt("wander");
                    float forward = behavior.contains("z_axis") ? (float) behavior.getDouble("z_axis") : 0F;
                    float clockwise = behavior.contains("x_axis") ? (float) behavior.getDouble("x_axis") : 0F;
                    behaviorBuilder.behavior(setStrafing(strafingTime, inactionTime, forward, clockwise, phase, hurt_level));
                }

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

    public static GuardMotion deserializeSpecificGuardMotion(CompoundTag args){
        GuardMotion guardMotion = null;
        if(args.contains("guard") && args.contains("stamina_cost_multiply") && args.contains("can_block_projectile") && args.contains("parry_cost_multiply") && args.contains("parry_animation")) {
            StaticAnimation guard = AnimationManager.getInstance().byKeyOrThrow(args.getString("guard"));
            float guard_cost = (float) args.getDouble("stamina_cost_multiply");
            boolean canBlockProjectile = args.getBoolean("can_block_projectile");
            float parry_cost = (float) args.getDouble("parry_cost_multiply");
            StaticAnimation[] parry_animations = null;
            if (args.contains("parry_animation")) {
                ListTag animationId = args.getList("parry_animation", 8);
                parry_animations = new StaticAnimation[animationId.size()];
                for (int j = 0; j < animationId.size(); j++) {
                    StaticAnimation parry_animation = AnimationManager.getInstance().byKeyOrThrow(animationId.getString(j));
                    parry_animations[j] = parry_animation;
                }
            }
            guardMotion = new GuardMotion(guard, canBlockProjectile, guard_cost, parry_cost, parry_animations);
        }
        return guardMotion;
    }



    public static List<LivingEntityPatchEvent.TimeStampedEvent> deserializeTimeCommandList(ListTag args){
        List<LivingEntityPatchEvent.TimeStampedEvent> list = Lists.newArrayList();
        for(int k = 0; k < args.size(); k++){
            CompoundTag command = args.getCompound(k);
            boolean execute_at_target = command.contains("execute_at_target") && command.getBoolean("execute_at_target");
            LivingEntityPatchEvent.TimeStampedEvent event = LivingEntityPatchEvent.TimeStampedEvent.CreateTimeCommandEvent(command.getFloat("time"), command.getString("command"), execute_at_target);
            list.add(event);
        }
        return list;
    }

    public static List<LivingEntityPatchEvent.BiEvent> deserializeHitCommandList(ListTag args){
        List<LivingEntityPatchEvent.BiEvent> list = Lists.newArrayList();
        for(int k = 0; k < args.size(); k++){
            CompoundTag command = args.getCompound(k);
            boolean execute_at_target = command.contains("execute_at_target") && command.getBoolean("execute_at_target");
            LivingEntityPatchEvent.BiEvent event = LivingEntityPatchEvent.BiEvent.CreateBiCommandEvent(command.getString("command"), execute_at_target);
            list.add(event);
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

    public static List<LivingEntityPatchEvent.BlockedEvent> deserializeBlockedCommandList(ListTag args){
        List<LivingEntityPatchEvent.BlockedEvent> list = Lists.newArrayList();
        for(int k = 0; k < args.size(); k++){
            CompoundTag command = args.getCompound(k);
            boolean execute_at_target = command.contains("execute_at_target") && command.getBoolean("execute_at_target");
            LivingEntityPatchEvent.BlockedEvent event = LivingEntityPatchEvent.BlockedEvent.CreateBlockCommandEvent(command.getString("command"), execute_at_target, command.getBoolean("is_parry"));
            list.add(event);
        }
        return list;
    }

    public static DamageSourceModifier deserializeDamageModifier(CompoundTag args){
        float damage = args.contains("damage") ? args.getFloat("damage") : 1F;
        float impact = args.contains("impact") ? args.getFloat("impact") : 1F;
        float armor_negation = args.contains("armor_negation") ? args.getFloat("armor_negation") : 1F;
        StunType stunType = args.contains("stun_type") ? StunType.valueOf(args.getString("stun_type").toUpperCase(Locale.ROOT)) : null;
        Collider collider = args.contains("collider") ?  deserializeCollider(args.getCompound("collider")) : null;
        return new DamageSourceModifier(damage, impact, armor_negation, stunType, collider);
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
