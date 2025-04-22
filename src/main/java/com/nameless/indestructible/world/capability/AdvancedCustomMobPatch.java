package com.nameless.indestructible.world.capability;

import com.nameless.indestructible.api.animation.types.CustomGuardAnimation;
import com.nameless.indestructible.client.ClientBossInfo;
import com.nameless.indestructible.data.AdvancedMobpatchReloader;
import com.nameless.indestructible.gameasset.GuardAnimations;
import com.nameless.indestructible.server.AdvancedBossInfo;
import com.nameless.indestructible.world.ai.CombatBehaviors.*;
import com.nameless.indestructible.world.ai.goal.AdvancedChasingGoal;
import com.nameless.indestructible.world.ai.goal.AdvancedCombatGoal;
import com.nameless.indestructible.world.ai.goal.GuardGoal;
import com.nameless.indestructible.world.ai.task.AdvancedChasingBehavior;
import com.nameless.indestructible.world.ai.task.AdvancedCombatBehavior;
import com.nameless.indestructible.world.ai.task.GuardBehavior;
import com.nameless.indestructible.world.capability.Utils.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPChangeLivingMotion;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.world.capabilities.entitypatch.Faction;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageSources;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
import yesman.epicfight.world.entity.ai.behavior.BackUpIfTooCloseStopInaction;
import yesman.epicfight.world.entity.ai.brain.BrainRecomposer;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

import static com.nameless.indestructible.world.capability.Utils.CapabilityUtils.putEpicFightAttributes;

public class AdvancedCustomMobPatch <T extends PathfinderMob> extends MobPatch<T> implements IAdvancedCapability, IBossEventCapability, IAnimationEventCapability {
    protected final CapabilityState<MobPatch<?>, ?> capabilityState;
    private AdvancedCustomPatchEventManger eventManger;
    public  boolean hasBossBar;
    public AdvancedBossInfo bossInfo;
    @OnlyIn(Dist.CLIENT)
    public ClientBossInfo clientBossInfo;
    public AdvancedCustomMobPatch(Faction faction, AdvancedMobpatchReloader.AdvancedCustomMobPatchProvider provider) {
        super(faction);
        this.capabilityState = new CapabilityState<>(provider, this);
    }
    @Override
    public void onConstructed(T entityIn) {
        super.onConstructed(entityIn);
        this.capabilityState.entityConstructed();
    }
    @Override
    public void onJoinWorld(T entityIn, EntityJoinLevelEvent event) {
        this.initialized = true;
        this.original.getAttributes().supplier = new AttributeSupplier(putEpicFightAttributes(this.original.getAttributes().supplier.instances));
        this.initAttributes();
        if (!entityIn.level().isClientSide() && !this.original.isNoAi()) {
            this.initAI();
        }
        this.capabilityState.entityJoinWorld();
    }
    @Override
    public void initAttributes() {
        this.capabilityState.initAttributes();
    }
    @Override
    public void serverTick(LivingEvent.LivingTickEvent event) {
        //连续硬质衰减
        if(this.capabilityState.hasStunReduction) {
            super.serverTick(event);
        }
        this.cancelKnockback = false;

        this.capabilityState.serverStateTick();
    }
    @Override
    protected void clientTick(LivingEvent.LivingTickEvent event) {
        this.capabilityState.clientStateTick();
    }
    @Override
    protected void selectGoalToRemove(Set<Goal> toRemove) {
        //换武器时重置所有史诗战斗ai
        this.capabilityState.selectGoalToRemove(toRemove);
    }
    @SuppressWarnings("unchecked")
    protected void initAI() {
        super.initAI();
        boolean isUsingBrain = !this.getOriginal().getBrain().availableBehaviorsByPriority.isEmpty();
        AdvancedMobpatchReloader.AdvancedCustomMobPatchProvider provider = this.capabilityState.getProvider();
        CombatBehaviors<MobPatch<?>> behaviors = this.capabilityState.getProvider().getCombatBehaviorsBuilder().build(this);

        if (isUsingBrain) {
                Brain<T> brain = (Brain<T>) this.original.getBrain();
                BrainsUtils.removeBehaviors(brain, Activity.CORE, AdvancedCombatBehavior.class);
                BrainsUtils.removeBehaviors(brain, Activity.CORE, AdvancedChasingBehavior.class);
                BrainsUtils.removeBehaviors(brain, Activity.CORE, GuardBehavior.class);
                if (behaviors != null) {
                    BrainsUtils.removeBehaviors((Brain<T>)this.original.getBrain(), Activity.FIGHT, MeleeAttack.class);
                    BrainsUtils.addBehaviors((Brain<T>)this.original.getBrain(), Activity.CORE, new AdvancedCombatBehavior<>(this, behaviors));
                }

                BrainRecomposer.replaceBehavior(brain, Activity.FIGHT, 11, BehaviorBuilder.triggerIf((entity) -> entity.isHolding((is) -> is.getItem() instanceof CrossbowItem), BackUpIfTooCloseStopInaction.create(5, 0.75F)), OneShot.class);
                BrainsUtils.replaceBehaviors((Brain<T>)this.original.getBrain(), Activity.CORE, MoveToTargetSink.class, new AdvancedChasingBehavior<>(this, provider.getChasingSpeed(), provider.getAttackRadius()));
                BrainsUtils.addBehaviors((Brain<T>)this.original.getBrain(), Activity.CORE, new GuardBehavior<>(this, provider.getGuardRadius()));
        } else {
                if (behaviors != null) {
                    this.original.goalSelector.addGoal(0, new AdvancedCombatGoal<>(this, behaviors));
                    this.original.goalSelector.addGoal(0, new GuardGoal<>(this, provider.getGuardRadius()));
                    this.original.goalSelector.addGoal(1, new AdvancedChasingGoal<>(this, this.getOriginal(), provider.getChasingSpeed(), true, provider.getAttackRadius()));
                }
        }
    }

    @Override
    public void initAnimator(Animator clientAnimator) {
        this.capabilityState.initStateAnimator(clientAnimator);
        if(this.capabilityState.getProvider().getGuardMotion() != null) {
            clientAnimator.addLivingAnimation(LivingMotions.BLOCK, this.capabilityState.getProvider().getGuardMotion().guard_animation);
        }
    }
    @Override
    public void updateMotion(boolean considerInaction) {
        super.commonAggressiveMobUpdateMotion(considerInaction);
        if(this.isBlocking()) currentCompositeMotion = LivingMotions.BLOCK;
    }
    @Override
    public AttackResult attack(EpicFightDamageSource damageSource, Entity target, InteractionHand hand) {
        AttackResult result = super.attack(damageSource, target, hand);
        this.capabilityState.attackedEvent(target,result);
        return result;
    }

    @Override
    public OpenMatrix4f getModelMatrix(float partialTicks) {
        float scale = this.capabilityState.getProvider().getScale();
        return super.getModelMatrix(partialTicks).scale(scale, scale, scale);
    }
    @Override
    public void setStunReductionOnHit(StunType stunType) {
        if(this.capabilityState.hasStunReduction){
            super.setStunReductionOnHit(stunType);
        }
    }
    @Override
    public float getStunReduction() {
        if(this.capabilityState.hasStunReduction){
            return super.getStunReduction();
        }
        return 0;
    }

    @Override
    public void modifyGuardMotion(){
        this.getAnimator().resetLivingAnimations();
        this.capabilityState.initStateAnimator(this.animator);
        if(this.capabilityState.getProvider().getGuardMotion() != null) {
            this.setCurrentGuardMotion(this.getGuardMotion());
            this.animator.addLivingAnimation(LivingMotions.BLOCK, this.capabilityState.currentGuardMotion.guard_animation);
        }
        SPChangeLivingMotion msg = new SPChangeLivingMotion(this.original.getId());
        msg.putEntries(this.getAnimator().getLivingAnimations().entrySet());
        EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(msg, this.original);
    }

    private GuardMotion getGuardMotion(){
        GuardMotion motion = this.capabilityState.getProvider().getGuardMotion();
        GuardMotion specific = this.capabilityState.specificGuardMotion;
        if(specific != null){
            Boolean[] b = this.capabilityState.specificGuardMotion.changeTag;
            if(b[0]){
                motion.guard_animation = specific.guard_animation;
            }
            if(b[1]){
                motion.can_block_projectile = specific.can_block_projectile;
            }
            if(b[2]){
                motion.cost = specific.cost;
            }
            if(b[3]){
                motion.parry_cost = specific.parry_cost;
            }
            if(b[4]){
                motion.parry_animation = specific.parry_animation;
            }
        }
        return motion;
    }
    @Override
    public AttackResult tryHurt(DamageSource damageSource, float amount) {
        return this.capabilityState.TryHurt(damageSource,amount);
    }

    @Override
    public EpicFightDamageSource getDamageSource(StaticAnimation animation, InteractionHand hand) {
        EpicFightDamageSources damageSources = EpicFightDamageSources.of(this.original.level());
        EpicFightDamageSource damagesource = damageSources.mobAttack(this.original).setAnimation(animation);
        damagesource.setImpact(this.getImpact(hand));
        damagesource.setArmorNegation(this.getArmorNegation(hand));
        damagesource.setHurtItem(this.original.getItemInHand(hand));
        this.capabilityState.modifierDamageSource(damagesource,hand);
        return damagesource;
    }
    @Override
    public Collider getColliderMatching(InteractionHand hand) {
        Collider collider = this.getAdvancedHoldingItemCapability(hand).getWeaponCollider();
        collider = this.capabilityState.modifierCollider(collider);
        return collider;
    }
    @Override
    public float getModifiedBaseDamage(float baseDamage) {
        return this.capabilityState.modifierDamage(baseDamage);
    }
    @Override
    public void onDeath(LivingDeathEvent event) {
        this.capabilityState.onDeath();
        this.getAnimator().playDeathAnimation();
        this.currentLivingMotion = LivingMotions.DEATH;
    }
    @Override
    public boolean applyStun(StunType stunType, float time){
        stunType = this.capabilityState.processStun(stunType);
        boolean isStunned = super.applyStun(stunType, time);
        this.capabilityState.setNeutralized(stunType);
        return isStunned;
    }

    @Override
    public void onFall(LivingFallEvent event) {
        if (!this.getOriginal().level().isClientSide() && this.isAirborneState()) {
            StaticAnimation fallAnimation = this.getAnimator().getLivingAnimation(LivingMotions.LANDING_RECOVERY, this.getHitAnimation(StunType.FALL));

            if (fallAnimation != null) {
                this.playAnimationSynchronized(fallAnimation, 0);
            }
        }
        if(!this.getOriginal().level().isClientSide() && this.getOriginal().onGround() && event.getDamageMultiplier() > 0.0F && !this.getEntityState().inaction() && !this.isAirborneState() && event.getDistance() > 5.0F && this.getHitAnimation(StunType.FALL) != null){
            this.capabilityState.fallEvent();
        }

        this.setAirborneState(false);
    }
    @Override
    public void knockBackEntity(Vec3 sourceLocation, float power) {
        if(this.capabilityState.neutralized) {
            return;
        }
        super.knockBackEntity(sourceLocation,power);
    }
    @Override
    public StaticAnimation getHitAnimation(StunType stunType) {
        return this.capabilityState.getProvider().getStunAnimations().get(stunType);
    }
    @Override
    public void onStartTracking(ServerPlayer trackingPlayer) {
        super.onStartTracking(trackingPlayer);
        this.capabilityState.tracked(trackingPlayer);
    }

    public void onStopTracking(Player trackingPlayer) {
        this.capabilityState.stopTracked(trackingPlayer);
    }

    @Override
    public void processSpawnData(ByteBuf buf) {
        this.capabilityState.processData(buf);
    }

    @Override
    public void onAttackBlocked(DamageSource damageSource, LivingEntityPatch<?> livingEntityPatch) {
        this.capabilityState.onAttackBlocked(livingEntityPatch);
    }
    public SoundEvent getWeaponHitSound(InteractionHand hand) {
        CapabilityItem itemCap = this.getAdvancedHoldingItemCapability(hand);
        return itemCap.isEmpty() ? this.capabilityState.getProvider().getHitSound() : itemCap.getHitSound();
    }

    public SoundEvent getSwingSound(InteractionHand hand) {
        CapabilityItem itemCap = this.getAdvancedHoldingItemCapability(hand);
        return itemCap.isEmpty() ? this.capabilityState.getProvider().getSwingSound() : itemCap.getSmashingSound();
    }

    public HitParticleType getWeaponHitParticle(InteractionHand hand) {
        CapabilityItem itemCap = this.getAdvancedHoldingItemCapability(hand);
        return itemCap.isEmpty() ? this.capabilityState.getProvider().getHitParticle() : itemCap.getHitParticle();
    }
    /**
     * IBossEventCapability
     **/
    @Override
    public boolean hasBossBar(){
        return this.hasBossBar;
    }
    @Override
    public void setupServerBossInfo(AdvancedBossInfo bossInfo){
        this.bossInfo = bossInfo;
    }
    @Override
    public AdvancedBossInfo getServerBossInfo(){
        return this.bossInfo;
    }
    public void setupClientBossInfo(ClientBossInfo bossInfo){
        this.clientBossInfo = bossInfo;
    }
    @Override
    public ClientBossInfo getClientBossInfo(){
        return this.clientBossInfo;
    }

    /**
     * IAnimationEventCapability
     **/
    @Override
    public void setupEventManger(){
        this.eventManger = new AdvancedCustomPatchEventManger();
    }

    @Override
    public AdvancedCustomPatchEventManger getEventManager(){
        return this.eventManger;
    }

    /**
     * IAdvancedCapability
     **/
    //防御状态
    @Override
    public void setBlocking(boolean blocking) {
        this.getOriginal().getEntityData().set(IS_BLOCKING, blocking);
        //格挡计数归0
        if(!blocking) this.capabilityState.parryCounter = 0;
    }

    @Override
    public boolean isBlocking() {
        return this.getOriginal().getEntityData().get(IS_BLOCKING);
    }

    //耐力
    @Override
    public float getMaxStamina() {
        AttributeInstance maxStamina = this.original.getAttribute(EpicFightAttributes.MAX_STAMINA.get());
        return (float)(maxStamina == null ? 0 : maxStamina.getValue());
    }

    @Override
    public float getStamina() {
        return this.getMaxStamina() == 0 ? 0 : this.original.getEntityData().get(STAMINA);
    }

    @Override
    public void setStamina(float value) {
        float f1 = Math.max(Math.min(value, this.getMaxStamina()), 0);
        this.original.getEntityData().set(STAMINA, f1);
    }

    //攻速
    @Override
    public float getAttackSpeed() {
        return this.original.getEntityData().get(ATTACK_SPEED);
    }

    @Override
    public void setAttackSpeed(float value) {
        this.original.getEntityData().set(ATTACK_SPEED, Math.abs(value));
    }

    @Override
    public void resetActionTick() {
        this.capabilityState.tickSinceLastAction = 0;
    }

    @Override
    public void setParried(boolean isParried) {
        this.capabilityState.isParried = isParried;
    }

    @Override
    public float getGuardCostMultiply(){
        return this.capabilityState.currentGuardMotion != null ? this.capabilityState.currentGuardMotion.cost : 0;
    }

    @Override
    public CustomGuardAnimation getGuardAnimation(){
        if(this.capabilityState.currentGuardMotion != null && this.capabilityState.currentGuardMotion.guard_animation instanceof CustomGuardAnimation guardAnimation){
            return guardAnimation;
        }
        return (CustomGuardAnimation) GuardAnimations.MOB_LONGSWORD_GUARD;
    }
    @Override
    public void specificGuardMotion(@Nullable GuardMotion guard_motion) {
        this.capabilityState.specificGuardMotion = guard_motion;
    }
    @Override
    public void setCurrentGuardMotion(GuardMotion guardMotion){
        this.capabilityState.currentGuardMotion = guardMotion;
    }
    @Override
    public boolean canBlockProjectile(){
        return this.capabilityState.currentGuardMotion != null && this.capabilityState.currentGuardMotion.can_block_projectile;
    }
    @Override
    public float getParryCostMultiply(){return this.capabilityState.currentGuardMotion != null ?  this.capabilityState.currentGuardMotion.parry_cost : 0;}
    @Override
    public StaticAnimation getParryAnimation(int times){
        if(this.capabilityState.currentGuardMotion != null) {
            GuardMotion guardMotion = this.capabilityState.currentGuardMotion;
            List<StaticAnimation> parry_animation = guardMotion.parry_animation != null && !guardMotion.parry_animation.isEmpty() ? this.capabilityState.currentGuardMotion.parry_animation :  List.of(Animations.LONGSWORD_GUARD_ACTIVE_HIT1, Animations.LONGSWORD_GUARD_ACTIVE_HIT2);
            return parry_animation.get(times % parry_animation.size());
        }
        return Animations.DUMMY_ANIMATION;
    }
    @Override
    public boolean isBlockableSource(DamageSource damageSource) {
        return !damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !damageSource.is(DamageTypeTags.BYPASSES_ARMOR) && (!damageSource.is(DamageTypeTags.IS_PROJECTILE) || this.canBlockProjectile()) && !damageSource.is(DamageTypeTags.IS_EXPLOSION) && !damageSource.is(DamageTypes.MAGIC) && !damageSource.is(DamageTypeTags.IS_FIRE);
    }

    public int getBlockTick(){return this.capabilityState.block_tick;}
    public void setBlockTick(int tick){this.capabilityState.block_tick = tick;}
    public void setCounterMotion(CounterMotion counter_motion){
        this.capabilityState.counterMotion = counter_motion;
    }
    public void setMaxParryTimes(int times){
        this.capabilityState.maxParryTimes = times;
    }
    public boolean isParrying(){
        return this.capabilityState.maxParryTimes > 0;
    }
    public void setStunImmunityTime(int tick){
        this.capabilityState.stun_immunity_time = tick;
    }

    @Override
    public int getPhase() {
        return this.capabilityState.phase;
    }

    @Override
    public void setPhase(int phase) {
        this.capabilityState.phase = phase;
    }

    //受击相关
    @Override
    public void setHurtResistLevel(int level) {
        this.capabilityState.hurtResistLevel = level;
    }
    @Override
    public void setInterrupted(boolean interrupted){
        this.capabilityState.interrupted = interrupted;
    }
    @Override
    public boolean interrupted(){
        return this.capabilityState.interrupted;
    }

    //游荡相关
    public int getStrafingTime(){
        return this.capabilityState.strafingTime;
    }
    public void setStrafingTime(int tick){
        this.capabilityState.strafingTime = tick;
    }
    public int getInactionTime(){
        return this.capabilityState.inactionTime;
    }
    public void setInactionTime(int tick){
        this.capabilityState.inactionTime = tick;
    }
    public float getStrafingForward(){
        return this.capabilityState.strafingForward;
    }
    public float getStrafingClockwise(){
        return this.capabilityState.strafingClockwise;
    }
    public void setStrafingDirection(float forward, float clockwise){
        this.capabilityState.strafingForward = forward;
        this.capabilityState.strafingClockwise = clockwise;
    }

    @Override
    public void actAnimationMotion(AnimationMotionSet motionSet) {
        this.capabilityState.animationMotion(motionSet);
    }

    @Override
    public void actGuardMotion(GuardMotionSet motionSet) {
        this.capabilityState.guardMotion(motionSet);
    }

    @Override
    public void actStrafing(WanderMotionSet wanderMotionSet) {
        this.capabilityState.strafingMotion(wanderMotionSet);

    }

    @Override
    public void setDamageSourceModifier(@Nullable DamageSourceModifier damageSourceModifier) {
        this.capabilityState.damageSourceModifier = damageSourceModifier;
    }
}
