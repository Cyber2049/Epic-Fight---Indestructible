package com.nameless.indestructible.world.capability;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.nameless.indestructible.api.animation.types.CommandEvent;
import com.nameless.indestructible.api.animation.types.CustomGuardAnimation;
import com.nameless.indestructible.client.gui.BossBarGUi;
import com.nameless.indestructible.data.AdvancedMobpatchReloader.AdvancedCustomHumanoidMobPatchProvider;
import com.nameless.indestructible.gameasset.GuardAnimations;
import com.nameless.indestructible.main.Indestructible;
import com.nameless.indestructible.server.AdvancedBossInfo;
import com.nameless.indestructible.world.ai.goal.AdvancedChasingGoal;
import com.nameless.indestructible.world.ai.goal.AdvancedCombatGoal;
import com.nameless.indestructible.world.ai.goal.GuardGoal;
import com.nameless.indestructible.world.ai.task.AdvancedChasingBehavior;
import com.nameless.indestructible.world.ai.task.AdvancedCombatBehavior;
import com.nameless.indestructible.world.ai.task.GuardBehavior;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RunIf;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.client.animation.ClientAnimator;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPChangeLivingMotion;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.world.capabilities.entitypatch.Faction;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.SourceTags;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.effect.EpicFightMobEffects;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
import yesman.epicfight.world.entity.ai.brain.BrainRecomposer;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class AdvancedCustomHumanoidMobPatch<T extends PathfinderMob> extends HumanoidMobPatch<T> {

    private final AdvancedCustomHumanoidMobPatchProvider provider;
    private final Map<WeaponCategory, Map<Style,GuardMotion>> weaponGuardMotions;
    private static final EntityDataAccessor<Float> STAMINA = new EntityDataAccessor<Float>(253, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ATTACK_SPEED = new EntityDataAccessor<Float>(177, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_BLOCKING = new EntityDataAccessor<Boolean>(178, EntityDataSerializers.BOOLEAN);
    public static final ResourceLocation BOSS_BAR = new ResourceLocation(Indestructible.MOD_ID, "textures/gui/boss_bar.png");
    //stamina
    private final int regenStaminaStandbyTime;
    //stun shield
    private final boolean hasStunReduction;
    private final float maxStunShield;
    private final int reganShieldStandbyTime;
    private final float reganShieldMultiply;
    //block
    private final float staminaLoseMultiply;
    private int block_tick;
    private boolean cancel_block;
    private int maxParryTimes;
    private int tickSinceLastAction;
    private int tickSinceBreakShield;
    private CounterMotion counterMotion;
    //parry
    private boolean isParry;
    private int parryCounter = 0;
    private int parryTimes = 0;
    private int stun_immunity_time;
    private final float guardRadius;
    private final float attackRadius;
    //event
    private DamageSourceModifier damageSourceModifier = null;
    private final List<CommandEvent.TimeStampedEvent> timeEvents = Lists.newArrayList();
    private final List<CommandEvent.HitEvent> hitEvents = Lists.newArrayList();
    private final List<CommandEvent.StunEvent> stunEvents = Lists.newArrayList();
    //private final List<CommandEvent.HitEvent> blockedEvents = Lists.newArrayList();
    private int phase;
    //wandering
    private float strafingForward;
    private float strafingClockwise;
    private int strafingTime;
    private int inactionTime;
    private int convertTick = 0;
    private boolean isRunning = false;
    private Entity lastAttacker;
    private float lastGetImpact;
    private GuardMotion specificGuardMotion;
    private AdvancedBossInfo bossInfo;
    public  boolean hasBossBar;
    private Component customName;
    private final ResourceLocation bossBar;

    public AdvancedCustomHumanoidMobPatch(Faction faction, AdvancedCustomHumanoidMobPatchProvider provider) {
        super(faction);
        this.provider = provider;
        this.regenStaminaStandbyTime = this.provider.getRegenStaminaStandbyTime();
        this.hasStunReduction = this.provider.hasStunReduction();
        this.maxStunShield = this.provider.getMaxStunShield();
        this.reganShieldStandbyTime = this.provider.getReganShieldStandbyTime();
        this.reganShieldMultiply = this.provider.getReganShieldMultiply();
        this.staminaLoseMultiply = this.provider.getStaminaLoseMultiply();
        this.weaponLivingMotions = this.provider.getHumanoidWeaponMotions();
        this.weaponAttackMotions = this.provider.getHumanoidCombatBehaviors();
        this.weaponGuardMotions = this.provider.getGuardMotions();
        this.guardRadius = this.provider.getGuardRadius();
        this.attackRadius = this.provider.getAttackRadius();
        this.hasBossBar = this.provider.hasBossBar();
        this.bossBar = this.provider.getBossBar() == null ? BOSS_BAR : this.provider.getBossBar();
    }

    @Override
    public void onConstructed(T entityIn) {
        super.onConstructed(entityIn);
        entityIn.getEntityData().define(STAMINA, 0.0F);
        entityIn.getEntityData().define(ATTACK_SPEED, 1.0F);
        entityIn.getEntityData().define(IS_BLOCKING, false);
        if(this.hasBossBar) {
            this.bossInfo = new AdvancedBossInfo(this);
            BossBarGUi.BossBarEntities.put(bossInfo.getId(), this);
            this.customName = this.provider.getName() == null ? this.getOriginal().getType().getDescription() : new TranslatableComponent(this.provider.getName());
        }
    }

    @Override
    public void onJoinWorld(T entityIn, EntityJoinWorldEvent event) {
        this.initialized = true;
        this.original.getAttributes().supplier = new AttributeSupplier(putEpicFightAttributes(this.original.getAttributes().supplier.instances));
        this.initAttributes();
        if (!entityIn.level.isClientSide() && !this.original.isNoAi()) {
            this.initAI();
        }
        this.tickSinceLastAction = 0;
        this.tickSinceBreakShield = 0;
        this.block_tick = 30;
        this.setStamina(this.getMaxStamina());
        this.setAttackSpeed(1F);
        this.setPhase(0);
        if(this.maxStunShield > 0) {
            this.setMaxStunShield(this.maxStunShield);
            this.setStunShield(this.maxStunShield);
        }
        if(!this.isLogicalClient()){
            this.initStunEvent(provider);
            this.resetMotion();
        }
    }

    private Map<Attribute, AttributeInstance> putEpicFightAttributes(Map<Attribute, AttributeInstance> originalMap) {
        Map<Attribute, AttributeInstance> newMap = Maps.newHashMap();
        AttributeSupplier supplier = AttributeSupplier.builder()
                .add(Attributes.ATTACK_DAMAGE)
                .add(EpicFightAttributes.WEIGHT.get())
                .add(EpicFightAttributes.IMPACT.get())
                .add(EpicFightAttributes.ARMOR_NEGATION.get())
                .add(EpicFightAttributes.MAX_STRIKES.get())
                .add(EpicFightAttributes.STUN_ARMOR.get())
                .add(EpicFightAttributes.MAX_EXECUTION_RESISTANCE.get())
                .add(EpicFightAttributes.OFFHAND_ARMOR_NEGATION.get())
                .add(EpicFightAttributes.OFFHAND_IMPACT.get())
                .add(EpicFightAttributes.OFFHAND_MAX_STRIKES.get())
                .add(EpicFightAttributes.OFFHAND_ATTACK_SPEED.get())
                .add(EpicFightAttributes.MAX_STAMINA.get())
                .add(EpicFightAttributes.STAMINA_REGEN.get())
                .build();
        newMap.putAll(supplier.instances);
        newMap.putAll(originalMap);
        return ImmutableMap.copyOf(newMap);
    }

    @Override
    public void serverTick(LivingEvent.LivingUpdateEvent event) {
        if (this.hasBossBar && this.getOriginal().tickCount % 4 == 0) bossInfo.update();
        if(this.hasStunReduction) {
            super.serverTick(event);
        }

        if (!this.state.inaction() && this.isBlocking()) {
            this.tickSinceLastAction++;
        }

        float stamina = this.getStamina();
        float maxStamina = this.getMaxStamina();
        float staminaRegen = (float)this.original.getAttributeValue(EpicFightAttributes.STAMINA_REGEN.get());

        if (stamina < maxStamina) {
            if(this.tickSinceLastAction > this.regenStaminaStandbyTime) {
                float staminaFactor = 1.0F + (float) Math.pow((stamina / (maxStamina - stamina * 0.5F)), 2);
                this.setStamina(stamina + maxStamina * (0.01F * staminaRegen) * staminaFactor);
            } else {
                this.setStamina(stamina + 0.0015F * staminaRegen * maxStamina);
            }
        }

        if (maxStamina < stamina) {
            this.setStamina(maxStamina);
        }

        if(this.maxStunShield > 0){
            float stunShield = this.getStunShield();
            float maxStunShield = this.getMaxStunShield();
            if(stunShield > 0){
                if(stunShield < maxStunShield && !this.getEntityState().hurt() && !this.getEntityState().knockDown()){
                    this.setStunShield(stunShield + 0.0015F * this.reganShieldMultiply * maxStunShield);
                }
                if(this.tickSinceBreakShield > 0) this.tickSinceBreakShield = 0;
            }

            if(stunShield == 0){
                this.tickSinceBreakShield++;
                if(tickSinceBreakShield > this.reganShieldStandbyTime){
                    this.setStunShield(this.getMaxStunShield());
                }
            }

            if(stunShield > maxStunShield){
                this.setStunShield(this.getMaxStunShield());
            }
        }
    }

    @Override
    protected void clientTick(LivingEvent.LivingUpdateEvent event) {
        boolean shouldRunning = this.original.animationSpeed >= 0.7F;
        if(shouldRunning != isRunning){
            this.convertTick++;
            if(convertTick > 4){
                isRunning = shouldRunning;
            }

        } else {
            this.convertTick = 0;
        }
    }

    public boolean hasTimeEvent(){
        return !this.timeEvents.isEmpty();
    }

    public List<CommandEvent.TimeStampedEvent> getTimeEventList(){
        return this.timeEvents;
    }
    public void addEvent(CommandEvent.TimeStampedEvent event){
        this.timeEvents.add(event);
    }
    public boolean hasHitEvent(){
        return !this.hitEvents.isEmpty();
    }

    public List<CommandEvent.HitEvent> getHitEventList(){
        return this.hitEvents;
    }
    public void addEvent(CommandEvent.HitEvent event){
        this.hitEvents.add(event);
    }

    private void initStunEvent(AdvancedCustomHumanoidMobPatchProvider provider){
        this.stunEvents.clear();
        if(provider.getStunEvent() != null && !provider.getStunEvent().isEmpty()){
            this.stunEvents.addAll(this.provider.getStunEvent());
        }
    }

    public void resetMotion(){
        if (this.hasTimeEvent()) this.timeEvents.clear();
        if (this.hasHitEvent()) this.hitEvents.clear();
        if (this.damageSourceModifier != null) this.damageSourceModifier = null;
    }
    public void setBlockTick(int value){this.block_tick = value;}
    public int getBlockTick(){return this.block_tick;}
    public void setBlocking(boolean blocking){this.original.getEntityData().set(IS_BLOCKING, blocking);}
    public boolean isBlocking(){return this.original.getEntityData().get(IS_BLOCKING);}
    public void cancelBlock(boolean setCancel){
        this.cancel_block = setCancel;
    }
    public void setParry(boolean isParry){this.isParry = isParry;}
    public float getMaxStamina() {
        AttributeInstance maxStamina = this.original.getAttribute(EpicFightAttributes.MAX_STAMINA.get());
        return (float)(maxStamina == null ? 0 : maxStamina.getValue());
    }

    public float getStamina() {
        return this.getMaxStamina() == 0 ? 0 : this.original.getEntityData().get(STAMINA);
    }
    public void setStamina(float value) {
        float f1 = Math.max(Math.min(value, this.getMaxStamina()), 0);
        this.original.getEntityData().set(STAMINA, f1);
    }
    public float getAttackSpeed(){return this.original.getEntityData().get(ATTACK_SPEED);}
    public void setAttackSpeed(float value){
        this.original.getEntityData().set(ATTACK_SPEED, Math.abs(value));
    }

    public void setParryCounter(int counter){
        this.parryCounter = counter;
    }
    public void setStunImmunityTime(int time){
        this.stun_immunity_time = time;
    }

    public void setMaxParryTimes(int times){
        this.maxParryTimes = times;
    }

    public void setCounterMotion(CounterMotion counter_motion){
        this.counterMotion = counter_motion;
    }
    public StaticAnimation getCounter(){
        return this.counterMotion.counter;
    }

    public float getCounterChance(){
        return this.counterMotion.chance;
    }

    public float getCounterStamina(){
        return this.counterMotion.cost;
    }

    public float getCounterSpeed(){
        return this.counterMotion.speed;
    }
    public void specificGuardMotion(GuardMotion specific_guard_motion){
        this.specificGuardMotion = specific_guard_motion;
    }
    public void resetActionTick() {
        this.tickSinceLastAction = 0;
    }
    public int getTickSinceLastAction() {
        return this.tickSinceLastAction;
    }
    public void setDamageSourceModifier(DamageSourceModifier damageSourceModifier) {
        this.damageSourceModifier = damageSourceModifier;
    }

    public int getPhase(){return this.phase;}
    public void setPhase(int phase){
        this.phase = Math.min(Math.max(0, phase), 20);
    }
    public int getStrafingTime(){return this.strafingTime;}
    public void setStrafingTime(int time){this.strafingTime = time;}
    public float getStrafingForward(){return this.strafingForward;}
    public float getStrafingClockwise(){return this.strafingClockwise;}
    public int getInactionTime(){return this.inactionTime;}
    public void setInactionTime(int time){this.inactionTime = time;}
    public void setStrafingDirection(float forward, float clockwise){
        this.strafingForward = forward;
        this.strafingClockwise = clockwise;
    }

    public Component getCustomName() {
        return customName;
    }

    public ResourceLocation getBossBar(){
        return this.bossBar;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setAIAsInfantry(boolean holdingRangedWeapon) {
        boolean isUsingBrain = !this.getOriginal().getBrain().availableBehaviorsByPriority.isEmpty();

        if (isUsingBrain) {
            if (!holdingRangedWeapon) {
                CombatBehaviors.Builder<HumanoidMobPatch<?>> builder = this.getHoldingItemWeaponMotionBuilder();

                if (builder != null) {
                    BrainRecomposer.replaceBehaviors((Brain<T>)this.original.getBrain(), Activity.FIGHT, MeleeAttack.class, new AdvancedCombatBehavior<>(this, builder.build(this)));
                    BrainRecomposer.replaceBehaviors((Brain<T>)this.original.getBrain(), Activity.FIGHT, RunIf.class, new RunIf<>((entity) -> !entity.isHolding(is -> is.getItem() instanceof ProjectileWeaponItem), new GuardBehavior<>(this,this.guardRadius)));
                }
                BrainRecomposer.replaceBehaviors((Brain<T>)this.original.getBrain(), Activity.CORE, MoveToTargetSink.class, new AdvancedChasingBehavior<>(this, this.provider.getChasingSpeed(), this.attackRadius));
            }
        } else {
            if (!holdingRangedWeapon) {
                CombatBehaviors.Builder<HumanoidMobPatch<?>> builder = this.getHoldingItemWeaponMotionBuilder();

                if (builder != null) {
                    this.original.goalSelector.addGoal(0, new AdvancedCombatGoal<>(this, builder.build(this)));
                    this.original.goalSelector.addGoal(0, new GuardGoal<>(this,this.guardRadius));
                    this.original.goalSelector.addGoal(1, new AdvancedChasingGoal<>(this, this.getOriginal(), this.provider.getChasingSpeed(), true,this.attackRadius));
                }
            }
        }
    }

    @Override
    protected void setWeaponMotions() {
        if (this.weaponAttackMotions == null) {
            super.setWeaponMotions();
        }
    }

    @Override
    protected void initAttributes() {
        this.original.getAttribute(EpicFightAttributes.WEIGHT.get()).setBaseValue(this.provider.getAttributeValues().get(EpicFightAttributes.WEIGHT.get()));
        this.original.getAttribute(EpicFightAttributes.MAX_STRIKES.get()).setBaseValue(this.provider.getAttributeValues().get(EpicFightAttributes.MAX_STRIKES.get()));
        this.original.getAttribute(EpicFightAttributes.ARMOR_NEGATION.get()).setBaseValue(this.provider.getAttributeValues().get(EpicFightAttributes.ARMOR_NEGATION.get()));
        this.original.getAttribute(EpicFightAttributes.IMPACT.get()).setBaseValue(this.provider.getAttributeValues().get(EpicFightAttributes.IMPACT.get()));
        this.original.getAttribute(EpicFightAttributes.MAX_STAMINA.get()).setBaseValue(this.provider.getAttributeValues().get(EpicFightAttributes.MAX_STAMINA.get()));
        this.original.getAttribute(EpicFightAttributes.STAMINA_REGEN.get()).setBaseValue(this.provider.getAttributeValues().get(EpicFightAttributes.STAMINA_REGEN.get()));
        this.original.getAttribute(EpicFightAttributes.OFFHAND_IMPACT.get()).setBaseValue(0.5F);
        this.original.getAttribute(EpicFightAttributes.OFFHAND_ARMOR_NEGATION.get()).setBaseValue(0F);
        this.original.getAttribute(EpicFightAttributes.OFFHAND_ATTACK_SPEED.get()).setBaseValue(1.2F);
        this.original.getAttribute(EpicFightAttributes.OFFHAND_MAX_STRIKES.get()).setBaseValue(1);

        if (this.provider.getAttributeValues().containsKey(Attributes.ATTACK_DAMAGE)) {
            this.original.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(this.provider.getAttributeValues().get(Attributes.ATTACK_DAMAGE));
        }
    }

    @Override
    public void initAnimator(ClientAnimator clientAnimator) {
        for (Pair<LivingMotion, StaticAnimation> pair : this.provider.getDefaultAnimations()) {
            clientAnimator.addLivingAnimation(pair.getFirst(), pair.getSecond());
        }

        clientAnimator.setCurrentMotionsAsDefault();
    }

    @Override
    public void updateMotion(boolean considerInaction) {
        if (this.original.getHealth() <= 0.0F) {
            this.currentLivingMotion = LivingMotions.DEATH;
        } else if (this.state.inaction() && considerInaction) {
            this.currentLivingMotion = LivingMotions.IDLE;
        } else if (this.original.getVehicle() != null) {
            this.currentLivingMotion = LivingMotions.MOUNT;
        } else if (this.original.getDeltaMovement().y < -0.550000011920929) {
            this.currentLivingMotion = LivingMotions.FALL;
        } else if (this.original.animationSpeed > 0.01F) {
            if (this.isRunning) {
                this.currentLivingMotion = LivingMotions.CHASE;
            } else {
                this.currentLivingMotion = LivingMotions.WALK;
            }
        } else {
            this.currentLivingMotion = LivingMotions.IDLE;
        }

        this.currentCompositeMotion = this.currentLivingMotion;

        if (this.original.isUsingItem()) {
            CapabilityItem activeItem = this.getHoldingItemCapability(this.original.getUsedItemHand());
            UseAnim useAnim = this.original.getItemInHand(this.original.getUsedItemHand()).getUseAnimation();
            UseAnim secondUseAnim = activeItem.getUseAnimation(this);

            if (useAnim == UseAnim.BLOCK || secondUseAnim == UseAnim.BLOCK)
                if (activeItem.getWeaponCategory() == CapabilityItem.WeaponCategories.SHIELD)
                    currentCompositeMotion = LivingMotions.BLOCK_SHIELD;
                else
                    currentCompositeMotion = LivingMotions.BLOCK;
            else if (useAnim == UseAnim.BOW || useAnim == UseAnim.SPEAR)
                currentCompositeMotion = LivingMotions.AIM;
            else if (useAnim == UseAnim.CROSSBOW)
                currentCompositeMotion = LivingMotions.RELOAD;
            else
                currentCompositeMotion = currentLivingMotion;
        } else {
            if(this.isBlocking()) currentCompositeMotion = LivingMotions.BLOCK;
            else if (CrossbowItem.isCharged(this.original.getMainHandItem()))
                currentCompositeMotion = LivingMotions.AIM;
            else if (this.getClientAnimator().getCompositeLayer(Layer.Priority.MIDDLE).animationPlayer.getAnimation().isReboundAnimation())
                currentCompositeMotion = LivingMotions.NONE;
            else if (this.original.swinging && this.original.getSleepingPos().isEmpty())
                currentCompositeMotion = LivingMotions.DIGGING;
            else
                currentCompositeMotion = currentLivingMotion;

            if (this.getClientAnimator().isAiming() && currentCompositeMotion != LivingMotions.AIM) {
                this.playReboundAnimation();
            }
        }
    }

    @Override
    public AttackResult attack(EpicFightDamageSource damageSource, Entity target, InteractionHand hand) {
        AttackResult result = super.attack(damageSource, target, hand);
        if(result.resultType.dealtDamage() && this.hasHitEvent()){
            for(CommandEvent.HitEvent event: this.getHitEventList()) {
                {
                    event.testAndExecute(this, target);
                    if(!this.getOriginal().isAlive() || !this.hasHitEvent()){break;}
                }
            }
        }
        return result;
    }

    @Override
    public OpenMatrix4f getModelMatrix(float partialTicks) {
        float scale = this.provider.getScale();
        return super.getModelMatrix(partialTicks).scale(scale, scale, scale);
    }
    @Override
    public void setStunReductionOnHit(StunType stunType) {
        if(this.hasStunReduction){
            super.setStunReductionOnHit(stunType);
        }
    }
    @Override
    public float getStunReduction() {
        if(this.hasStunReduction){
            return super.getStunReduction();
        }
        return 0;
    }
    @Override
    public void modifyLivingMotionByCurrentItem() {
        this.getAnimator().resetLivingAnimations();

        CapabilityItem mainhandCap = this.getHoldingItemCapability(InteractionHand.MAIN_HAND);
        CapabilityItem offhandCap = this.getAdvancedHoldingItemCapability(InteractionHand.OFF_HAND);
        Map<LivingMotion, StaticAnimation> motionModifier = Maps.newHashMap();

        motionModifier.putAll(offhandCap.getLivingMotionModifier(this, InteractionHand.OFF_HAND));
        motionModifier.putAll(mainhandCap.getLivingMotionModifier(this, InteractionHand.MAIN_HAND));

        for (Map.Entry<LivingMotion, StaticAnimation> entry : motionModifier.entrySet()) {
            this.getAnimator().addLivingAnimation(entry.getKey(), entry.getValue());
        }

        if (this.weaponLivingMotions != null && this.weaponLivingMotions.containsKey(mainhandCap.getWeaponCategory())) {
            Map<Style, Set<Pair<LivingMotion, StaticAnimation>>> mapByStyle = this.weaponLivingMotions.get(mainhandCap.getWeaponCategory());
            Style style = mainhandCap.getStyle(this);

            if (mapByStyle.containsKey(style) || mapByStyle.containsKey(CapabilityItem.Styles.COMMON)) {
                Set<Pair<LivingMotion, StaticAnimation>> animModifierSet = mapByStyle.getOrDefault(style, mapByStyle.get(CapabilityItem.Styles.COMMON));

                for (Pair<LivingMotion, StaticAnimation> pair : animModifierSet) {
                    this.animator.addLivingAnimation(pair.getFirst(), pair.getSecond());
                }
            }
        }

        if(this.weaponGuardMotions != null && this.weaponGuardMotions.containsKey(mainhandCap.getWeaponCategory())){
            Map<Style, GuardMotion> motionByStyle = this.weaponGuardMotions.get(mainhandCap.getWeaponCategory());
            Style style = mainhandCap.getStyle(this);
            if(motionByStyle.containsKey(style) || motionByStyle.containsKey(CapabilityItem.Styles.COMMON)){
                StaticAnimation guard = motionByStyle.getOrDefault(style, motionByStyle.get(CapabilityItem.Styles.COMMON)).guard_animation;
                this.animator.addLivingAnimation(LivingMotions.BLOCK, guard);
            }
        }

        SPChangeLivingMotion msg = new SPChangeLivingMotion(this.original.getId());
        msg.putEntries(this.getAnimator().getLivingAnimationEntrySet());
        EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(msg, this.original);
    }

    private GuardMotion getCurrentGuardMotion(){
        if(this.specificGuardMotion != null){
            return this.specificGuardMotion;
        }

        CapabilityItem itemCap = this.getHoldingItemCapability(InteractionHand.MAIN_HAND);
        if(this.weaponGuardMotions != null && itemCap != null){
            Style style = itemCap.getStyle(this);
            Map<Style, GuardMotion> mapByStyle = this.weaponGuardMotions.get(itemCap.getWeaponCategory());
            if (mapByStyle != null && (mapByStyle.containsKey(style) || mapByStyle.containsKey(CapabilityItem.Styles.COMMON))) {
                return mapByStyle.getOrDefault(style, mapByStyle.get(CapabilityItem.Styles.COMMON));
            }
        }
        return new GuardMotion(GuardAnimations.MOB_LONGSWORD_GUARD, false,1);
    }

    public float getGuardCostMultiply(){ return this.getCurrentGuardMotion().cost;}

    public CustomGuardAnimation getGuardAnimation(){
        if(this.getCurrentGuardMotion().guard_animation instanceof CustomGuardAnimation guardAnimation){
            return guardAnimation;
        }
        return (CustomGuardAnimation) GuardAnimations.MOB_LONGSWORD_GUARD;
    }
    public boolean canBlockProjectile(){
        return this.getCurrentGuardMotion().canBlockProjectile;
    }
    public float getParryCostMultiply(){return this.getCurrentGuardMotion().parry_cost;}
    public StaticAnimation getParryAnimation(){
        StaticAnimation[] parry_animation = this.getCurrentGuardMotion().parry_animation != null ? this.getCurrentGuardMotion().parry_animation :  new StaticAnimation[]{Animations.LONGSWORD_GUARD_ACTIVE_HIT1, Animations.LONGSWORD_GUARD_ACTIVE_HIT2};
        return parry_animation[this.parryTimes % parry_animation.length];
    }

    @Override
    public AttackResult tryHurt(DamageSource damageSource, float amount) {
        AttackResult result = AttackResult.of(this.getEntityState().attackResult(damageSource), amount);
        if(result.resultType.dealtDamage()){
           result = damageSource.getDirectEntity() != this.getOriginal() ? this.tryProcess(damageSource, amount) : result;
           if(result.resultType.dealtDamage()){
               this.lastAttacker = damageSource.getDirectEntity();
           }
        }
        return result;
    }

    private AttackResult tryProcess(DamageSource damageSource, float amount){
        //TRY BLOCK
        if (this.isBlocking()) {
            CustomGuardAnimation animation = this.getGuardAnimation();
            StaticAnimation success = animation.successAnimation != null ? EpicFightMod.getInstance().animationManager.findAnimationByPath(animation.successAnimation) : Animations.SWORD_GUARD_HIT;
            boolean isFront = false;
            boolean canBlockSource = !damageSource.isExplosion() && !damageSource.isMagic() && !damageSource.isBypassInvul() && (!damageSource.isProjectile() || this.canBlockProjectile());
            Vec3 sourceLocation = damageSource.getSourcePosition();

            if (sourceLocation != null) {
                Vec3 viewVector = this.getOriginal().getViewVector(1.0F);
                Vec3 toSourceLocation = sourceLocation.subtract(this.getOriginal().position()).normalize();

                if (toSourceLocation.dot(viewVector) > 0.0D) {
                    isFront = true;
                }
            }
            if (canBlockSource && isFront) {
                float impact;
                float knockback;
                if (damageSource instanceof EpicFightDamageSource efDamageSource) {
                    impact = amount / 4F * (1F + efDamageSource.getImpact() / 2F);
                    if(efDamageSource.hasTag(SourceTags.GUARD_PUNCTURE)){
                        impact = Float.MAX_VALUE;
                    }
                } else {
                    impact = amount / 3;
                }
                knockback = 0.25F + Math.min(impact * 0.1F, 1.0F);
                if (damageSource.getDirectEntity() instanceof LivingEntity targetEntity) {
                    knockback += EnchantmentHelper.getKnockbackBonus(targetEntity) * 0.1F;
                }
                float cost = this.isParry ? this.getParryCostMultiply() : this.getGuardCostMultiply();
                float stamina = this.getStamina() - impact * cost;
                this.setStamina(stamina);
                EpicFightParticles.HIT_BLUNT.get().spawnParticleWithArgument(((ServerLevel) this.getOriginal().level), HitParticleType.FRONT_OF_EYES, HitParticleType.ZERO, this.getOriginal(), damageSource.getDirectEntity());
               //success
                if (stamina >= 0F) {
                    float counter_cost = this.getCounterStamina();
                    Random random = this.getOriginal().getRandom();
                    this.rotateTo(damageSource.getDirectEntity(),30F,true);
                    if (random.nextFloat() < this.getCounterChance() && stamina >= counter_cost) {
                        if(this.stun_immunity_time > 0){
                            this.getOriginal().addEffect(new MobEffectInstance(EpicFightMobEffects.STUN_IMMUNITY.get(), this.stun_immunity_time));
                        }
                        this.setAttackSpeed(this.getCounterSpeed());
                        this.playAnimationSynchronized(this.getCounter(),0);
                        this.playSound(EpicFightSounds.CLASH, -0.05F, 0.1F);
                        //this.knockBackEntity(damageSource.getDirectEntity().position(), 0.1F);
                        if(this.cancel_block){this.setBlocking(false);}
                        this.setStamina(this.getStamina() - counter_cost);
                        //counter
                    } else if (this.isParry){
                        if(this.parryCounter + 1 >= this.maxParryTimes) {
                            this.setBlocking(false);
                            if(this.stun_immunity_time > 0){
                                this.getOriginal().addEffect(new MobEffectInstance(EpicFightMobEffects.STUN_IMMUNITY.get(), this.stun_immunity_time));
                            }
                        }
                        this.playAnimationSynchronized(this.getParryAnimation(), 0F);
                        this.parryCounter += 1;
                        this.parryTimes += 1;
                        this.playSound(EpicFightSounds.CLASH, -0.05F, 0.1F);
                        this.knockBackEntity(damageSource.getDirectEntity().position(), 0.4F * knockback);
                    } else {
                        this.playAnimationSynchronized(success, 0.1F);
                        this.playSound(animation.isShield ? SoundEvents.SHIELD_BLOCK : EpicFightSounds.CLASH, -0.05F, 0.1F);
                        this.knockBackEntity(damageSource.getDirectEntity().position(), knockback);
                    }
                    return new AttackResult(AttackResult.ResultType.BLOCKED, amount);
                    //break
                } else {
                    this.setBlocking(false);
                    this.applyStun(StunType.NEUTRALIZE,2.0F);
                    this.playSound(EpicFightSounds.NEUTRALIZE_MOBS, -0.05F, 0.1F);
                    this.setStamina(this.getMaxStamina());
                    return new AttackResult(AttackResult.ResultType.SUCCESS, amount/2);
                }
            }
        }

        if(damageSource instanceof EpicFightDamageSource efDamageSource) {
            this.lastGetImpact = efDamageSource.getImpact();
        } else {
            this.lastGetImpact = amount/3;
        }
        return new AttackResult(AttackResult.ResultType.SUCCESS, amount);
    }

    @Override
    public EpicFightDamageSource getDamageSource(StaticAnimation animation, InteractionHand hand) {
        EpicFightDamageSource damagesource = EpicFightDamageSource.commonEntityDamageSource("mob", this.original, animation);
        damagesource.setImpact(this.getImpact(hand));
        damagesource.setArmorNegation(this.getArmorNegation(hand));
        damagesource.setHurtItem(this.original.getItemInHand(hand));

        if(this.damageSourceModifier != null){
            damagesource.setImpact(this.getImpact(hand) * damageSourceModifier.impact());
            damagesource.setArmorNegation(Math.min(100, this.getArmorNegation(hand) * damageSourceModifier.armor_negation()));
        }
        return damagesource;
    }


    @Override
    public void onDeath(LivingDeathEvent event) {
        this.resetMotion();
        this.setBlocking(false);
        this.getAnimator().playDeathAnimation();
        this.currentLivingMotion = LivingMotions.DEATH;
        if (this.hasBossBar && !this.getOriginal().isRemoved()) {
            bossInfo.update();
        }
    }
    @Override
    public float getModifiedBaseDamage(float baseDamage) {
        if(this.damageSourceModifier != null) {
            baseDamage *= damageSourceModifier.damage();
        }
        return baseDamage;
    }

    @Override
    public boolean applyStun(StunType stunType, float time){
        DynamicAnimation animation = this.getAnimator().getPlayerFor(null).getAnimation();
        if(animation == Animations.BIPED_COMMON_NEUTRALIZED || animation == Animations.GREATSWORD_GUARD_BREAK) {
            stunType = stunType == StunType.KNOCKDOWN ? stunType : StunType.NONE;
        } else if (this.staminaLoseMultiply > 0 && this.lastGetImpact > 0 && this.getStunShield() <= 0){
            this.setStamina(this.getStamina() - this.lastGetImpact * this.staminaLoseMultiply);
        }

        if (this.getStamina() < this.lastGetImpact) {
            stunType = StunType.NEUTRALIZE;
            this.setStamina(this.getMaxStamina());
        }

        if(!this.stunEvents.isEmpty()){
            if(this.getHitAnimation(stunType) != null){
                for(CommandEvent.StunEvent event: this.stunEvents) {
                    event.testAndExecute(this, lastAttacker, stunType.ordinal());
                    if(!this.getOriginal().isAlive() || this.stunEvents.isEmpty() /* stellaris || (stunType.ordinal() == 5 && this.getStamina() > 0) */){break;}
                 }
            }
        }
        this.setAttackSpeed(1F);
        this.resetActionTick();
        this.resetMotion();
        return super.applyStun(stunType, time);
    }

    @Override
    public void onFall(LivingFallEvent event) {
        super.onFall(event);
        this.setAttackSpeed(1F);
        this.resetActionTick();
        this.resetMotion();
        if(!this.stunEvents.isEmpty()){
                for(CommandEvent.StunEvent stunEvent: this.stunEvents) {
                    stunEvent.testAndExecute(this, lastAttacker, StunType.FALL.ordinal());
                    if(!this.getOriginal().isAlive() || this.stunEvents.isEmpty()){break;}
                }
        }
    }
    @Override
    public void knockBackEntity(Vec3 sourceLocation, float power) {
        DynamicAnimation animation = this.getAnimator().getPlayerFor(null).getAnimation();
        if(animation == Animations.BIPED_COMMON_NEUTRALIZED || animation == Animations.GREATSWORD_GUARD_BREAK) {
            return;
        }
        super.knockBackEntity(sourceLocation,power);
    }
    @Override
    public StaticAnimation getHitAnimation(StunType stunType) {
        return this.provider.getStunAnimations().get(stunType);
    }

    @Override
    public void onStartTracking(ServerPlayer trackingPlayer) {
        super.onStartTracking(trackingPlayer);
        if(this.hasBossBar) this.bossInfo.addPlayer(trackingPlayer);
    }

    public void onStopTracking(ServerPlayer trackingPlayer) {
        if(this.hasBossBar) this.bossInfo.removePlayer(trackingPlayer);
    }

    public record CustomAnimationMotion(StaticAnimation animation, float convertTime, float speed, float stamina) { }
    public record CounterMotion(StaticAnimation counter, float cost, float chance, float speed) {}
    public record DamageSourceModifier(float damage, float impact, float armor_negation){ }

    public static class GuardMotion{
        private final StaticAnimation guard_animation;
        private final boolean canBlockProjectile;
        private final float cost;
        private final float parry_cost;
        private final StaticAnimation[] parry_animation;
        public GuardMotion(StaticAnimation guard_animation, boolean canBlockProjectile, float cost, float parry_cost, StaticAnimation[] parry_animation){
            this.guard_animation = guard_animation;
            this.canBlockProjectile = canBlockProjectile;
            this.cost = cost;
            this.parry_cost = parry_cost;
            this.parry_animation = parry_animation;
        }
        public GuardMotion(StaticAnimation guard_animation, boolean canBlockProjectile, float cost){
            this.guard_animation = guard_animation;
            this.canBlockProjectile = canBlockProjectile;
            this.cost = cost;
            this.parry_cost = 0.5F;
            this.parry_animation =  new StaticAnimation[]{Animations.LONGSWORD_GUARD_ACTIVE_HIT1, Animations.LONGSWORD_GUARD_ACTIVE_HIT2};
        }
    }
}
