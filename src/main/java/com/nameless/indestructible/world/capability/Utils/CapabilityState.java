package com.nameless.indestructible.world.capability.Utils;

import com.mojang.datafixers.util.Pair;
import com.nameless.indestructible.api.animation.types.CommandEvent;
import com.nameless.indestructible.api.animation.types.CustomGuardAnimation;
import com.nameless.indestructible.client.ClientBossInfo;
import com.nameless.indestructible.client.gui.BossBarGUi;
import com.nameless.indestructible.data.AdvancedMobpatchReloader;
import com.nameless.indestructible.server.AdvancedBossInfo;
import com.nameless.indestructible.world.ai.goal.AdvancedCombatGoal;
import com.nameless.indestructible.world.ai.goal.GuardGoal;
import com.nameless.indestructible.world.capability.Utils.BehaviorsUtils.CounterMotion;
import com.nameless.indestructible.world.capability.Utils.BehaviorsUtils.DamageSourceModifier;
import com.nameless.indestructible.world.capability.Utils.BehaviorsUtils.GuardMotion;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPSpawnData;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageType;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.effect.EpicFightMobEffects;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
import yesman.epicfight.world.entity.ai.goal.AnimatedAttackGoal;
import yesman.epicfight.world.entity.ai.goal.TargetChasingGoal;

import java.util.Set;
import java.util.UUID;

import static com.nameless.indestructible.main.Indestructible.BOSS_BAR;
import static com.nameless.indestructible.world.capability.Utils.IAdvancedCapability.*;

public class CapabilityState<T extends MobPatch<?>> {
    private final AdvancedMobpatchReloader.AdvancedCustomMobPatchProvider provider;
    private final T mobPatch;
    public GuardMotion currentGuardMotion;
    public GuardMotion specificGuardMotion;
    //stamina
    private final int regenStaminaStandbyTime;
    //stun shield
    public final boolean hasStunReduction;
    private final float maxStunShield;
    private final int reganShieldStandbyTime;
    private final float reganShieldMultiply;
    //block
    private final float staminaLoseMultiply;
    public int block_tick;
    public boolean cancel_block;
    public int maxParryTimes;
    public int tickSinceLastAction;
    public int tickSinceBreakShield;
    public CounterMotion counterMotion;
    public int parryCounter = 0;
    private int parryTimes = 0;
    public int stun_immunity_time;
    public DamageSourceModifier damageSourceModifier = null;
    public int phase;
    public int hurtResistLevel = 2;
    public boolean neutralized;
    //wandering
    public float strafingForward;
    public float strafingClockwise;
    public int strafingTime;
    public int inactionTime;
    private Entity lastAttacker;
    private float lastGetImpact;
    public boolean interrupted;
    public boolean isParried = false;
    public CapabilityState(AdvancedMobpatchReloader.AdvancedCustomMobPatchProvider provider, T mobPatch){
        this.provider = provider;
        this.mobPatch = mobPatch;
        this.regenStaminaStandbyTime = provider.getRegenStaminaStandbyTime();
        this.hasStunReduction = provider.hasStunReduction();
        this.maxStunShield = provider.getMaxStunShield();
        this.reganShieldStandbyTime = provider.getReganShieldStandbyTime();
        this.reganShieldMultiply = provider.getReganShieldMultiply();
        this.staminaLoseMultiply = provider.getStaminaLoseMultiply();
    }
    public T getPatch(){
        return mobPatch;
    }


    public AdvancedMobpatchReloader.AdvancedCustomMobPatchProvider getProvider(){
        return this.provider;
    }

    public void entityConstructed(){
        //添加耐力，攻速，防御实体数据
        if(mobPatch instanceof IAdvancedCapability){
            mobPatch.getOriginal().getEntityData().define(STAMINA, 0.0F);
            mobPatch.getOriginal().getEntityData().define(ATTACK_SPEED, 1.0F);
            mobPatch.getOriginal().getEntityData().define(IS_BLOCKING, false);
        }

        //添加boss血条事件
        if(mobPatch instanceof IBossEventCapability ibc && ibc.hasBossBar()){
            ibc.setupServerBossInfo(new AdvancedBossInfo(mobPatch.getOriginal()));
        }

        //添加动画事件容器
        if(mobPatch instanceof IAnimationEventCapability iec){
            iec.setupEventManger();
        }
    }
    public void entityJoinWorld(){
        //初始化非行动状态时间计数，初始化破韧性时间计数
        this.tickSinceLastAction = 0;
        this.tickSinceBreakShield =0;
        this.block_tick = 30;
        //初始化耐力，攻速
        if(mobPatch instanceof IAdvancedCapability iac) {
            iac.setStamina(iac.getMaxStamina());
            iac.setAttackSpeed(1F);
        }
        //初始化阶段计数，设置最大韧性
        this.phase = 0;
        float maxStunShield = this.maxStunShield;
        if(maxStunShield > 0) {
            mobPatch.setMaxStunShield(maxStunShield);
            mobPatch.setStunShield(maxStunShield);
        }
        //初始化被动类动画事件，初始化
        if(!mobPatch.isLogicalClient() ){
           if(mobPatch instanceof IAnimationEventCapability iec) {
               iec.getEventManager().initPassiveEvent(provider.getStunEvent());
               iec.getEventManager().initAnimationEvent();
           }
            if(this.damageSourceModifier != null) this.damageSourceModifier = null;
        }
    }

    public void serverStateTick(){
        //每4tick更新一次boss血条事件
        if(mobPatch instanceof IBossEventCapability ibc){
            if (ibc.hasBossBar() && mobPatch.getOriginal().tickCount % 4 == 0) ibc.getServerBossInfo().update();
        }

        if(!(mobPatch instanceof IAdvancedCapability iac)) return;
        //非行动状态时间计数
        if (!mobPatch.getEntityState().inaction()) {
            this.tickSinceLastAction++;
        }
        //耐力回复
        float stamina = iac.getStamina();
        float maxStamina = iac.getMaxStamina();
        float staminaRegen = (float)mobPatch.getOriginal().getAttributeValue(EpicFightAttributes.STAMINA_REGEN.get());

        if (stamina < maxStamina) {
            if(this.tickSinceLastAction > this.regenStaminaStandbyTime && !iac.isBlocking()) {
                float staminaFactor = 1.0F + (float) Math.pow((stamina / (maxStamina - stamina * 0.5F)), 2);
                iac.setStamina(stamina + maxStamina * (0.01F * staminaRegen) * staminaFactor);
            } else {
                iac.setStamina(stamina + 0.0015F * staminaRegen * maxStamina);
            }
        }

        if (maxStamina < stamina) {
            iac.setStamina(maxStamina);
        }

        //韧性回复
        if(this.maxStunShield > 0){
            float stunShield = mobPatch.getStunShield();
            float maxStunShield = this.maxStunShield;
            if(stunShield > 0){
                if(stunShield < maxStunShield && !mobPatch.getEntityState().hurt() && !mobPatch.getEntityState().knockDown()){
                    mobPatch.setStunShield(stunShield + 0.0015F * this.reganShieldMultiply * maxStunShield);
                }
                if(this.tickSinceBreakShield > 0) this.tickSinceBreakShield = 0;
            }

            if(stunShield == 0){
                this.tickSinceBreakShield++;
                if(tickSinceBreakShield > this.reganShieldStandbyTime){
                    mobPatch.setStunShield(this.maxStunShield);
                }
            }

            if(stunShield > maxStunShield){
                mobPatch.setStunShield(this.maxStunShield);
            }
        }

        if(neutralized && mobPatch.getEntityState().hurtLevel() < 2){
            neutralized = false;
        }

        if(mobPatch.getEntityState().hurt() && mobPatch.getEntityState().hurtLevel() >= this.hurtResistLevel){
            interrupted = true;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void clientStateTick(){
        //客户端boss血条时间更新
        if(mobPatch instanceof IBossEventCapability ibc) {
            ClientBossInfo clientBossInfo = ibc.getClientBossInfo();
            if (ibc.hasBossBar() && clientBossInfo != null) {
                clientBossInfo.setHealthRatio(mobPatch.getOriginal().getHealth() / mobPatch.getOriginal().getMaxHealth());
                if(mobPatch instanceof IAdvancedCapability iac){
                clientBossInfo.setStaminaRatio(iac.getStamina() / iac.getMaxStamina());
                }
            }
        }
    }

    public void selectGoalToRemove(Set<Goal> toRemove) {
        //换武器时重置所有史诗战斗ai
        if(mobPatch instanceof IAdvancedCapability) {
            for (WrappedGoal wrappedGoal : mobPatch.getOriginal().goalSelector.getAvailableGoals()) {
                Goal goal = wrappedGoal.getGoal();

                if (goal instanceof MeleeAttackGoal || goal instanceof AnimatedAttackGoal || goal instanceof RangedAttackGoal || goal instanceof TargetChasingGoal || goal instanceof GuardGoal || goal instanceof AdvancedCombatGoal) {
                    toRemove.add(goal);
                }
            }
        }
    }
    
    public void initAttributes(){
        //初始化attribute
        mobPatch.getOriginal().getAttribute(EpicFightAttributes.WEIGHT.get()).setBaseValue(this.provider.getAttributeValues().get(EpicFightAttributes.WEIGHT.get()));
        mobPatch.getOriginal().getAttribute(EpicFightAttributes.MAX_STRIKES.get()).setBaseValue(this.provider.getAttributeValues().get(EpicFightAttributes.MAX_STRIKES.get()));
        mobPatch.getOriginal().getAttribute(EpicFightAttributes.ARMOR_NEGATION.get()).setBaseValue(this.provider.getAttributeValues().get(EpicFightAttributes.ARMOR_NEGATION.get()));
        mobPatch.getOriginal().getAttribute(EpicFightAttributes.IMPACT.get()).setBaseValue(this.provider.getAttributeValues().get(EpicFightAttributes.IMPACT.get()));
        mobPatch.getOriginal().getAttribute(EpicFightAttributes.OFFHAND_IMPACT.get()).setBaseValue(0.5F);
        mobPatch.getOriginal().getAttribute(EpicFightAttributes.OFFHAND_ARMOR_NEGATION.get()).setBaseValue(0F);
        mobPatch.getOriginal().getAttribute(EpicFightAttributes.OFFHAND_ATTACK_SPEED.get()).setBaseValue(1.2F);
        mobPatch.getOriginal().getAttribute(EpicFightAttributes.OFFHAND_MAX_STRIKES.get()).setBaseValue(1);

        if(mobPatch instanceof IAdvancedCapability){
            mobPatch.getOriginal().getAttribute(EpicFightAttributes.MAX_STAMINA.get()).setBaseValue(this.provider.getAttributeValues().get(EpicFightAttributes.MAX_STAMINA.get()));
            mobPatch.getOriginal().getAttribute(EpicFightAttributes.STAMINA_REGEN.get()).setBaseValue(this.provider.getAttributeValues().get(EpicFightAttributes.STAMINA_REGEN.get()));
        }

        if (this.provider.getAttributeValues().containsKey(Attributes.ATTACK_DAMAGE)) {
            mobPatch.getOriginal().getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(this.provider.getAttributeValues().get(Attributes.ATTACK_DAMAGE));
        }
    }

    public void initStateAnimator(Animator clientAnimator){
        //行为动画绑定
        for (Pair<LivingMotion, StaticAnimation> pair : this.provider.getDefaultAnimations()) {
            clientAnimator.addLivingAnimation(pair.getFirst(), pair.getSecond());
        }
        //clientAnimator.setCurrentMotionsAsDefault();
    }

    public void attackedEvent(Entity target, AttackResult result){
        //执行攻击动画时间
        if(mobPatch instanceof IAnimationEventCapability iec){
            AdvancedCustomPatchEventManger eventManger = iec.getEventManager();
            if(result.resultType.dealtDamage() && eventManger.hasHitEvent()){
                for(CommandEvent.BiEvent event: eventManger.getHitEventList()) {
                    event.testAndExecute(mobPatch, target);
                    if(!mobPatch.getOriginal().isAlive() || !eventManger.hasHitEvent()){break;}
                }
            }
        }
    }

    public AttackResult TryHurt(DamageSource damageSource, float amount){
        //获取伤害冲击量以及伤害的实体来源
        AttackResult result = AttackResult.of(mobPatch.getEntityState().attackResult(damageSource), amount);
        if(result.resultType.dealtDamage()){
            result = damageSource.getDirectEntity() != mobPatch.getOriginal() ? this.tryProcess(damageSource, amount) : result;
            if(result.resultType.dealtDamage()){
                this.lastAttacker = damageSource.getDirectEntity();
                if(damageSource instanceof EpicFightDamageSource efDamageSource) {
                    this.lastGetImpact = efDamageSource.getImpact();
                } else {
                    this.lastGetImpact = amount/3;
                }
            }
        }
        return result;
    }

    private AttackResult tryProcess(DamageSource damageSource, float amount){
        if(!(mobPatch instanceof IAdvancedCapability iac)) return new AttackResult(AttackResult.ResultType.SUCCESS, amount);
        //TRY BLOCK
        if (iac.isBlocking()) {
            CustomGuardAnimation animation = iac.getGuardAnimation();
            StaticAnimation success = animation.successAnimation != null ? animation.successAnimation.get() : Animations.SWORD_GUARD_HIT;
            boolean isFront = false;
            Vec3 sourceLocation = damageSource.getSourcePosition();

            if (sourceLocation != null) {
                Vec3 viewVector = mobPatch.getOriginal().getViewVector(1.0F);
                Vec3 toSourceLocation = sourceLocation.subtract(mobPatch.getOriginal().position()).normalize();

                if (toSourceLocation.dot(viewVector) > 0.0D) {
                    isFront = true;
                }
            }
            if (iac.isBlockableSource(damageSource) && isFront) {
                float impact;
                float knockback;
                if (damageSource instanceof EpicFightDamageSource efDamageSource) {
                    impact = efDamageSource.getImpact();
                    if(efDamageSource.is(EpicFightDamageType.GUARD_PUNCTURE)){
                        return new AttackResult(AttackResult.ResultType.SUCCESS, amount);
                    }
                } else {
                    impact = amount / 3;
                }
                knockback = 0.25F + Math.min(impact * 0.1F, 1.0F);
                if (damageSource.getDirectEntity() instanceof LivingEntity targetEntity) {
                    knockback += EnchantmentHelper.getKnockbackBonus(targetEntity) * 0.1F;
                }
                float cost = this.maxParryTimes > 0 && this.parryCounter+1 < this.maxParryTimes ? iac.getParryCostMultiply() : iac.getGuardCostMultiply();
                float stamina = iac.getStamina() - impact * cost;
                iac.setStamina(stamina);
                EpicFightParticles.HIT_BLUNT.get().spawnParticleWithArgument(((ServerLevel) mobPatch.getOriginal().level()), HitParticleType.FRONT_OF_EYES, HitParticleType.ZERO, mobPatch.getOriginal(), damageSource.getDirectEntity());
                //success
                if (stamina >= 0F) {
                    float counter_cost = this.counterMotion.cost;
                    RandomSource random = mobPatch.getOriginal().getRandom();
                    mobPatch.rotateTo(damageSource.getDirectEntity(),30F,true);
                    if (random.nextFloat() < this.counterMotion.chance && stamina >= counter_cost) {
                        if(this.stun_immunity_time > 0){
                            mobPatch.getOriginal().addEffect(new MobEffectInstance(EpicFightMobEffects.STUN_IMMUNITY.get(), this.stun_immunity_time));
                        }
                        iac.setAttackSpeed(this.counterMotion.speed);
                        mobPatch.playAnimationSynchronized(this.counterMotion.counter,0);
                        mobPatch.playSound(EpicFightSounds.CLASH.get(), -0.05F, 0.1F);
                        //this.knockBackEntity(damageSource.getDirectEntity().position(), 0.1F);
                        if(this.cancel_block){iac.setBlocking(false);}
                        iac.setStamina(iac.getStamina() - counter_cost);
                        //counter
                    } else if (this.maxParryTimes > 0){
                        if(this.parryCounter + 1 >= this.maxParryTimes) {
                            iac.setBlocking(false);
                            this.maxParryTimes = 0;
                            if(this.stun_immunity_time > 0){
                                mobPatch.getOriginal().addEffect(new MobEffectInstance(EpicFightMobEffects.STUN_IMMUNITY.get(), this.stun_immunity_time));
                            }
                        }
                        mobPatch.playAnimationSynchronized(iac.getParryAnimation(this.parryTimes), 0F);
                        this.parryCounter += 1;
                        this.parryTimes += 1;
                        mobPatch.playSound(EpicFightSounds.CLASH.get(), -0.05F, 0.1F);
                        mobPatch.knockBackEntity(damageSource.getDirectEntity().position(), 0.4F * knockback);
                    } else {
                        mobPatch.playAnimationSynchronized(success, 0.1F);
                        mobPatch.playSound(animation.isShield ? SoundEvents.SHIELD_BLOCK : EpicFightSounds.CLASH.get(), -0.05F, 0.1F);
                        mobPatch.knockBackEntity(damageSource.getDirectEntity().position(), knockback);
                    }
                    if(damageSource.getDirectEntity() instanceof LivingEntity living && damageSource.getDirectEntity() instanceof LivingEntity) {
                        LivingEntityPatch<?> targetPatch = EpicFightCapabilities.getEntityPatch(living, LivingEntityPatch.class);
                        if(targetPatch != null){
                            targetPatch.onAttackBlocked(damageSource, mobPatch);
                        }
                        if(targetPatch instanceof IAdvancedCapability targetIAC){
                            targetIAC.setParried(this.parryCounter > 0);
                        }
                    }
                    return new AttackResult(AttackResult.ResultType.BLOCKED, amount);
                    //break
                } else {
                    iac.setBlocking(false);
                    mobPatch.applyStun(StunType.NEUTRALIZE,2.0F);
                    mobPatch.playSound(EpicFightSounds.NEUTRALIZE_MOBS.get(), -0.05F, 0.1F);
                    EpicFightParticles.AIR_BURST.get().spawnParticleWithArgument((ServerLevel) mobPatch.getOriginal().level(), mobPatch.getOriginal(), damageSource.getDirectEntity());
                    iac.setStamina(iac.getMaxStamina());
                    return new AttackResult(AttackResult.ResultType.SUCCESS, amount/2);
                }
            }
        }
        return new AttackResult(AttackResult.ResultType.SUCCESS, amount);
    }

    public void modifierDamageSource(EpicFightDamageSource damageSource, InteractionHand hand){
        if(this.damageSourceModifier != null){
            damageSource.setImpact(mobPatch.getImpact(hand) * damageSourceModifier.impact);
            damageSource.setArmorNegation(Math.min(100, mobPatch.getArmorNegation(hand) * damageSourceModifier.armor_negation));
            if(damageSourceModifier.stunType != null){damageSource.setStunType(this.damageSourceModifier.stunType);}
        }
    }

    public Collider modifierCollider(Collider collider){
        if(this.damageSourceModifier != null && this.damageSourceModifier.collider !=null) {
            collider = this.damageSourceModifier.collider;
        }
        return collider;
    }

    public void onDeath(){
        if(mobPatch instanceof IAnimationEventCapability iec) {
            iec.getEventManager().initAnimationEvent();
        }
        if(mobPatch instanceof IAdvancedCapability iac) iac.setBlocking(false);
        if(mobPatch instanceof IBossEventCapability iec) {
            if (iec.hasBossBar() && !mobPatch.getOriginal().isRemoved()) {
                iec.getServerBossInfo().update();
            }
        }
    }

    public float modifierDamage(float damage){
        if(this.damageSourceModifier != null) {
           damage *= damageSourceModifier.damage;
        }
        return damage;
    }

    private void resetWhenStunned(){
        this.tickSinceLastAction = 0;
        if (this.damageSourceModifier != null) this.damageSourceModifier = null;
        if(mobPatch instanceof IAdvancedCapability iac){
            iac.setAttackSpeed(1);
        }
        if(mobPatch instanceof IAnimationEventCapability iec) {
            iec.getEventManager().initAnimationEvent();
        }
    }
    public StunType processStun(StunType stunType){
        if(this.neutralized) {
            stunType = stunType == StunType.KNOCKDOWN ? stunType : StunType.NONE;
        } else if (mobPatch instanceof IAdvancedCapability iac && this.staminaLoseMultiply > 0 && this.lastGetImpact > 0 && mobPatch.getStunShield() <= 0){
            iac.setStamina(iac.getStamina() - this.lastGetImpact * this.staminaLoseMultiply);
            if (iac.getStamina() <  this.lastGetImpact * this.staminaLoseMultiply) {
                stunType = StunType.NEUTRALIZE;
                mobPatch.playSound(EpicFightSounds.NEUTRALIZE_MOBS.get(), -0.05F, 0.1F);
                if(this.lastAttacker != null)EpicFightParticles.AIR_BURST.get().spawnParticleWithArgument((ServerLevel) mobPatch.getOriginal().level(),mobPatch.getOriginal(), lastAttacker);
                iac.setStamina(iac.getMaxStamina());
            }
        }

        if(mobPatch instanceof IAnimationEventCapability iec && iec.getEventManager().hasStunEvent()){
            if(mobPatch.getHitAnimation(stunType) != null){
                for(CommandEvent.StunEvent event: iec.getEventManager().getStunEvents()) {
                    event.testAndExecute(mobPatch, lastAttacker, stunType.ordinal());
                    if(!mobPatch.getOriginal().isAlive() || !iec.getEventManager().hasStunEvent()){break;}
                }
            }
        }

        if(stunType != StunType.NONE) {
           resetWhenStunned();
        }

        return stunType;
    }
    public void setNeutralized(StunType stunType){
        if(stunType == StunType.NEUTRALIZE){
            this.neutralized = true;
        }
    }
    public void fallEvent(){
        resetWhenStunned();
        if(mobPatch instanceof IAnimationEventCapability iec) {
            if (iec.getEventManager().hasStunEvent()) {
                for (CommandEvent.StunEvent stunEvent : iec.getEventManager().getStunEvents()) {
                    stunEvent.testAndExecute(mobPatch, lastAttacker, StunType.FALL.ordinal());
                    if (!mobPatch.getOriginal().isAlive() || !iec.getEventManager().hasStunEvent()) {
                        break;
                    }
                }
            }
        }
    }

    public void tracked(ServerPlayer trackingPlayer){
        if(mobPatch instanceof IBossEventCapability ibc && ibc.hasBossBar()) {
            AdvancedBossInfo bossInfo = ibc.getServerBossInfo();
            bossInfo.addPlayer(trackingPlayer);
            SPSpawnData packet = new SPSpawnData(mobPatch.getOriginal().getId());
            packet.getBuffer().writeLong(bossInfo.getId().getMostSignificantBits());
            packet.getBuffer().writeLong(bossInfo.getId().getLeastSignificantBits());
            EpicFightNetworkManager.sendToPlayer(packet, trackingPlayer);
        }
    }
    public void stopTracked(Player trackingPlayer){
        if(mobPatch instanceof IBossEventCapability ibc && ibc.hasBossBar()) {
            AdvancedBossInfo bossInfo = ibc.getServerBossInfo();
            bossInfo.removePlayer((ServerPlayer) trackingPlayer);
        }
    }
    public void processData(ByteBuf buf){
        if(mobPatch instanceof IBossEventCapability ibc) {
            long mostSignificant = buf.readLong();
            long leastSignificant = buf.readLong();
            UUID uuid = new UUID(mostSignificant, leastSignificant);
            ResourceLocation rl = this.provider.getBossBar() == null ? BOSS_BAR : this.provider.getBossBar();
            Component name = this.provider.getName() == null ? mobPatch.getOriginal().getType().getDescription() : Component.translatable(this.provider.getName());
            ibc.setupClientBossInfo(new ClientBossInfo(name.getString(), mobPatch.getOriginal().getDisplayName().getString(), rl, uuid));
            BossBarGUi.BossBarEntities.put(uuid, ibc.getClientBossInfo());
        }
    }

    public void onAttackBlocked(LivingEntityPatch<?> livingEntityPatch){
        if(mobPatch instanceof IAnimationEventCapability iec && iec.getEventManager().hasBlockEvents()){
            for(CommandEvent.BlockedEvent event: iec.getEventManager().getBlockedEvents()) {
                event.testAndExecute(mobPatch, livingEntityPatch.getOriginal(), this.isParried);
                if(!mobPatch.getOriginal().isAlive() || !iec.getEventManager().hasBlockEvents()){break;}
            }
        }
        this.isParried = false;
    }
}
