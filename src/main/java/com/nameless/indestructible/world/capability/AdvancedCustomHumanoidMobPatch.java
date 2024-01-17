package com.nameless.indestructible.world.capability;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.nameless.indestructible.api.animation.types.AnimationEvent;
import com.nameless.indestructible.api.animation.types.CustomGuardAnimation;
import com.nameless.indestructible.data.AdvancedMobpatchReloader.AdvancedCustomHumanoidMobPatchProvider;
import com.nameless.indestructible.gameasset.GuardAnimations;
import com.nameless.indestructible.world.ai.goal.AdvancedChasingGoal;
import com.nameless.indestructible.world.ai.goal.AdvancedCombatGoal;
import com.nameless.indestructible.world.ai.goal.GuardGoal;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RunIf;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
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
import yesman.epicfight.world.entity.ai.brain.task.AnimatedCombatBehavior;
import yesman.epicfight.world.entity.ai.brain.task.BackUpIfTooCloseStopInaction;
import yesman.epicfight.world.entity.ai.brain.task.MoveToTargetSinkStopInaction;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class AdvancedCustomHumanoidMobPatch<T extends PathfinderMob> extends HumanoidMobPatch<T> {

    private final AdvancedCustomHumanoidMobPatchProvider provider;
    private final Map<WeaponCategory, Map<Style,Pair<StaticAnimation,Pair<Float, Boolean>>>> guardMotions;
    private static final EntityDataAccessor<Float> STAMINA = new EntityDataAccessor<Float>(253, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ATTACK_SPEED = new EntityDataAccessor<Float>(177, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_BLOCKING = new EntityDataAccessor<Boolean>(178, EntityDataSerializers.BOOLEAN);
    //stamina
    private final int regenStaminaStandbyTime;
    private final float regenStaminaMultiply;
    private final float maxStamina;
    //stun shield
    private final boolean hasStunReduction;
    private final float maxStunShield;
    private final int reganShieldStandbyTime;
    private final float reganShieldMultiply;
    //block
    private final float staminaLoseMultiply;
    private int block_tick;
    private int tickSinceLastAction;
    private int tickSinceBreakShield;
    private CounterMotion counterMotion;
    //event
    private DamageSourceModifier damageSourceModifier = null;
    private final List<AnimationEvent.TimeStampedEvent> timeEvents = Lists.newArrayList();
    private final List<AnimationEvent.HitEvent> hitEvents = Lists.newArrayList();
    private int phase;
    //wandering
    private float strafingForward;
    private float strafingClockwise;
    private int strafingTime;
    private int inactionTime;
    public AdvancedCustomHumanoidMobPatch(Faction faction, AdvancedCustomHumanoidMobPatchProvider provider) {
        super(faction);
        this.provider = provider;
        this.regenStaminaStandbyTime = provider.getRegenStaminaStandbyTime();
        this.regenStaminaMultiply = provider.getRegenStaminaMultiply();
        this.maxStamina = provider.getMaxStamina();
        this.hasStunReduction = provider.hasStunReduction();
        this.maxStunShield = provider.getMaxStunShield();
        this.reganShieldStandbyTime = provider.getReganShieldStandbyTime();
        this.reganShieldMultiply = provider.getReganShieldMultiply();
        this.staminaLoseMultiply = provider.getStaminaLoseMultiply();
        this.weaponLivingMotions = provider.getHumanoidWeaponMotions();
        this.weaponAttackMotions = provider.getHumanoidCombatBehaviors();
        this.guardMotions = provider.getGuardMotions();
    }

    @Override
    public void onConstructed(T entityIn) {
        super.onConstructed(entityIn);
        entityIn.getEntityData().define(STAMINA, 0.0F);
        entityIn.getEntityData().define(ATTACK_SPEED, 1.0F);
        entityIn.getEntityData().define(IS_BLOCKING, false);
    }

    @Override
    public void onJoinWorld(T entityIn, EntityJoinWorldEvent event) {
        super.onJoinWorld(entityIn, event);
        this.tickSinceLastAction = 0;
        this.tickSinceBreakShield = 0;
        this.block_tick = 0;
        this.setStamina(this.getMaxStamina());
        this.resetMotion();
        this.setAttackSpeed(1F);
        this.setPhase(0);
        if(this.maxStunShield > 0) {
            this.setMaxStunShield(this.maxStunShield);
            this.setStunShield(this.maxStunShield);
        }
    }

    @Override
    public void serverTick(LivingEvent.LivingUpdateEvent event) {
        if(this.hasStunReduction) {
            super.serverTick(event);
        }

        if (!this.state.inaction() && this.getBlockTick() <= 0) {
            this.tickSinceLastAction++;
        }

        float stamina = this.getStamina();
        float maxStamina = this.getMaxStamina();

        if (stamina < maxStamina) {
            if(this.tickSinceLastAction > this.regenStaminaStandbyTime) {
                float staminaFactor = 1.0F + (float) Math.pow((stamina / (maxStamina - stamina * 0.5F)), 2);
                this.setStamina(stamina + maxStamina * (0.01F * this.regenStaminaMultiply) * staminaFactor);
            } else {
                this.setStamina(stamina + 0.0015F * this.regenStaminaMultiply * maxStamina);
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

    public boolean hasTimeEvent(){
        return !this.timeEvents.isEmpty();
    }

    public List<AnimationEvent.TimeStampedEvent> getTimeEventList(){
        return this.timeEvents;
    }
    public void addEvent(AnimationEvent.TimeStampedEvent event){
        this.timeEvents.add(event);
    }
    public boolean hasHitEvent(){
        return !this.hitEvents.isEmpty();
    }

    public List<AnimationEvent.HitEvent> getHitEventList(){
        return this.hitEvents;
    }
    public void addEvent(AnimationEvent.HitEvent event){
        this.hitEvents.add(event);
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
    public float getMaxStamina() {
        return this.maxStamina;
    }

    public float getStamina() {
        return this.getMaxStamina() == 0 ? 0 : Math.max(0,this.original.getEntityData().get(STAMINA));
    }
    public void setStamina(float value) {
        float f1 = Math.max(Math.min(value, this.getMaxStamina()), 0);
        this.original.getEntityData().set(STAMINA, f1);
    }
    public float getAttackSpeed(){return this.original.getEntityData().get(ATTACK_SPEED);}
    public void setAttackSpeed(float value){
        this.original.getEntityData().set(ATTACK_SPEED, Math.abs(value));
    }

    public void setCounterMotion(StaticAnimation counter, float cost, float chance, float speed){
        this.counterMotion = new CounterMotion(counter,cost, chance,speed);
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


    @SuppressWarnings("unchecked")
    @Override
    public void setAIAsInfantry(boolean holdingRanedWeapon) {
        boolean isUsingBrain = !this.getOriginal().getBrain().availableBehaviorsByPriority.isEmpty();

        if (isUsingBrain) {
            if (!holdingRanedWeapon) {
                CombatBehaviors.Builder<HumanoidMobPatch<?>> builder = this.getHoldingItemWeaponMotionBuilder();

                if (builder != null) {
                    BrainRecomposer.replaceBehaviors((Brain<T>)this.original.getBrain(), Activity.FIGHT, MeleeAttack.class, new AnimatedCombatBehavior<>(this, builder.build(this)));
                }

                BrainRecomposer.replaceBehaviors((Brain<T>)this.original.getBrain(), Activity.FIGHT, RunIf.class, new RunIf<>((entity) -> entity.isHolding(is -> is.getItem() instanceof CrossbowItem), new BackUpIfTooCloseStopInaction<>(5, 0.75F)));
                BrainRecomposer.replaceBehaviors((Brain<T>)this.original.getBrain(), Activity.CORE, MoveToTargetSink.class, new MoveToTargetSinkStopInaction());
            }
        } else {
            if (!holdingRanedWeapon) {
                CombatBehaviors.Builder<HumanoidMobPatch<?>> builder = this.getHoldingItemWeaponMotionBuilder();

                if (builder != null) {
                    this.original.goalSelector.addGoal(0, new AdvancedCombatGoal<>(this, builder.build(this)));
                    this.original.goalSelector.addGoal(0, new GuardGoal<>(this,4F));
                    this.original.goalSelector.addGoal(1, new AdvancedChasingGoal<>(this, this.getOriginal(), this.provider.getChasingSpeed(), true,0));
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
            if (this.original.getSpeed() > 1.5F) {
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
            for(AnimationEvent.HitEvent event: this.getHitEventList()) {
                {
                    event.testAndExecute(this, target);
                }
            }
        }
        return result;
    }


    @Override
    public StaticAnimation getHitAnimation(StunType stunType) {
        this.setAttackSpeed(1F);
        this.resetActionTick();
        this.resetMotion();

        return this.provider.getStunAnimations().get(stunType);
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

        if(this.guardMotions != null && this.guardMotions.containsKey(mainhandCap.getWeaponCategory())){
            Map<Style, Pair<StaticAnimation,Pair<Float,Boolean>>> motionByStyle = this.guardMotions.get(mainhandCap.getWeaponCategory());
            Style style = mainhandCap.getStyle(this);
            if(motionByStyle.containsKey(style) || motionByStyle.containsKey(CapabilityItem.Styles.COMMON)){
                StaticAnimation guard = motionByStyle.getOrDefault(style, motionByStyle.get(CapabilityItem.Styles.COMMON)).getFirst();
                this.animator.addLivingAnimation(LivingMotions.BLOCK, guard);
            }
        }

        SPChangeLivingMotion msg = new SPChangeLivingMotion(this.original.getId());
        msg.putEntries(this.getAnimator().getLivingAnimationEntrySet());
        EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(msg, this.original);
    }

    private Pair<StaticAnimation,Pair<Float,Boolean>> getCurrentGuardMotion(){
        CapabilityItem itemCap = this.getHoldingItemCapability(InteractionHand.MAIN_HAND);
        if(this.guardMotions != null && itemCap != null){
            Style style = itemCap.getStyle(this);
            Map<Style, Pair<StaticAnimation,Pair<Float,Boolean>>> mapByStyle = this.guardMotions.get(itemCap.getWeaponCategory());
            if (mapByStyle != null && (mapByStyle.containsKey(style) || mapByStyle.containsKey(CapabilityItem.Styles.COMMON))) {
                return mapByStyle.getOrDefault(style, mapByStyle.get(CapabilityItem.Styles.COMMON));
            }
        }
        return Pair.of(GuardAnimations.MOB_LONGSWORD_GUARD,Pair.of(1F, false));
    }

    public Float getStaminaCostMultiply(){
        return this.getCurrentGuardMotion().getSecond().getFirst();
    }

    public CustomGuardAnimation getGuardAnimation(){
        if(this.getCurrentGuardMotion().getFirst() instanceof CustomGuardAnimation guardAnimation){
            return guardAnimation;
        }
        return (CustomGuardAnimation) GuardAnimations.MOB_LONGSWORD_GUARD;
    }
    public boolean canBlockProjectile(){
        return this.getCurrentGuardMotion().getSecond().getSecond();
    }

    @Override
    public AttackResult tryHurt(DamageSource damageSource, float amount) {
        AttackResult result = AttackResult.of(this.getEntityState().attackResult(damageSource), amount);
        if(result.resultType.dealtDamage()){
           result = this.tryGuard(damageSource, amount);
        }
        return result;
    }

    private AttackResult tryGuard(DamageSource damageSource, float amount){
        if (this.getBlockTick() > 0) {
            CustomGuardAnimation animation = this.getGuardAnimation();
            StaticAnimation success = animation.successAnimation != null ? EpicFightMod.getInstance().animationManager.findAnimationByPath(animation.successAnimation) : Animations.SWORD_GUARD_HIT;
            StaticAnimation fail = animation.failAnimation != null ? EpicFightMod.getInstance().animationManager.findAnimationByPath(animation.failAnimation) : Animations.BIPED_COMMON_NEUTRALIZED;
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

                float stamina = this.getStamina() - impact * this.getStaminaCostMultiply();
                this.setStamina(stamina);
                EpicFightParticles.HIT_BLUNT.get().spawnParticleWithArgument(((ServerLevel) this.getOriginal().level), HitParticleType.FRONT_OF_EYES, HitParticleType.ZERO, this.getOriginal(), damageSource.getDirectEntity());
                if (stamina >= 0F) {
                    float counter_cost = this.getCounterStamina();
                    Random random = this.getOriginal().getRandom();
                    if (random.nextFloat() < this.getCounterChance() && stamina >= counter_cost) {
                        this.getOriginal().addEffect(new MobEffectInstance(EpicFightMobEffects.STUN_IMMUNITY.get(), 25));
                        this.setAttackSpeed(this.getCounterSpeed());
                        this.playAnimationSynchronized(this.getCounter(),0);
                        this.playSound(EpicFightSounds.CLASH, -0.05F, 0.1F);
                        this.knockBackEntity(damageSource.getDirectEntity().position(), 0.1F);
                        this.setBlockTick(0);
                        this.setStamina(this.getStamina() - counter_cost);
                    } else {
                        this.playAnimationSynchronized(success, 0.1F);
                        this.playSound(EpicFightSounds.CLASH, -0.05F, 0.1F);
                        this.knockBackEntity(damageSource.getDirectEntity().position(), knockback);
                    }
                    return new AttackResult(AttackResult.ResultType.BLOCKED, amount);
                } else {
                    this.setBlockTick(0);
                    if (damageSource instanceof EpicFightDamageSource efDamageSource && efDamageSource.getStunType() != StunType.KNOCKDOWN) {
                        efDamageSource.setStunType(StunType.NONE);
                    }
                    this.playSound(EpicFightSounds.NEUTRALIZE_MOBS, 3.0F, 0.0F, 0.1F);
                    this.playAnimationSynchronized(fail, 0.1F);
                    this.setStamina(this.getMaxStamina());
                    return new AttackResult(AttackResult.ResultType.SUCCESS, amount/2);
                }
            }
        }
        if(damageSource instanceof EpicFightDamageSource efDamageSource) {
            if (this.staminaLoseMultiply > 0 && this.getStunShield() <= 0) {
                this.setStamina(this.getStamina() - efDamageSource.getImpact() * this.staminaLoseMultiply);
                if (this.getStamina() < efDamageSource.getImpact()) {
                    efDamageSource.setStunType(StunType.NONE);
                    this.setAttackSpeed(1F);
                    this.resetActionTick();
                    this.resetMotion();
                    this.playAnimationSynchronized(Animations.BIPED_COMMON_NEUTRALIZED, 0);
                    this.setStamina(this.getMaxStamina());
                }
            }

            DynamicAnimation animation = this.getAnimator().getPlayerFor(null).getAnimation();
            if(animation == Animations.BIPED_COMMON_NEUTRALIZED || animation == Animations.GREATSWORD_GUARD_BREAK) {
                efDamageSource.setStunType(StunType.NONE);
                this.setAttackSpeed(1F);
                this.resetActionTick();
                this.resetMotion();
                }
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
        this.setBlockTick(0);
        this.setBlocking(false);
        this.getAnimator().playDeathAnimation();
        this.currentLivingMotion = LivingMotions.DEATH;
    }
    @Override
    public float getModifiedBaseDamage(float baseDamage) {
        if(this.damageSourceModifier != null) {
            baseDamage *= damageSourceModifier.damage();
        }
        return baseDamage;
    }

    public record CustomAnimationMotion(StaticAnimation animation, float convertTime, float speed, float stamina) { }
    public record CounterMotion(StaticAnimation counter, float cost, float chance, float speed) {}
    public record DamageSourceModifier(float damage, float impact, float armor_negation){ }
}
