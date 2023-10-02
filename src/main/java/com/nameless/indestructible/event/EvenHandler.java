package com.nameless.indestructible.event;

import com.nameless.indestructible.api.animation.types.CustomGuardAnimation;
import com.nameless.indestructible.main.Indestructible;
import com.nameless.indestructible.utils.CustomHumanoidMobPatchUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.CustomHumanoidMobPatch;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.SourceTags;
import yesman.epicfight.world.effect.EpicFightMobEffects;

import java.util.Random;

@Mod.EventBusSubscriber(modid= Indestructible.MOD_ID)
public class EvenHandler {
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void blockEvent(LivingAttackEvent event) {
        EntityPatch<?> entityPatch = event.getEntityLiving().getCapability(EpicFightCapabilities.CAPABILITY_ENTITY, null).orElse(null);
        if (entityPatch instanceof CustomHumanoidMobPatch<?> chpatch) {
            CustomHumanoidMobPatchUtils utils = (CustomHumanoidMobPatchUtils) chpatch;
            if (utils.getBlockTick() > 0) {
                CustomGuardAnimation animation = utils.getGuardAnimation();
                DamageSource damageSource = event.getSource();
                StaticAnimation success = animation.successAnimation != null ? EpicFightMod.getInstance().animationManager.findAnimationByPath(animation.successAnimation) : Animations.SWORD_GUARD_HIT;
                StaticAnimation fail = animation.failAnimation != null ? EpicFightMod.getInstance().animationManager.findAnimationByPath(animation.failAnimation) : Animations.BIPED_COMMON_NEUTRALIZED;
                boolean isFront = false;
                boolean canBlockSource = damageSource instanceof EntityDamageSource && !damageSource.isExplosion() && !damageSource.isMagic() && !damageSource.isBypassInvul() && (!damageSource.isProjectile() || utils.canBlockProjectile());
                Vec3 sourceLocation = damageSource.getSourcePosition();

                if (sourceLocation != null) {
                    Vec3 viewVector = event.getEntityLiving().getViewVector(1.0F);
                    Vec3 toSourceLocation = sourceLocation.subtract(event.getEntityLiving().position()).normalize();

                    if (toSourceLocation.dot(viewVector) > 0.0D) {
                        isFront = true;
                    }
                }
                if (canBlockSource && isFront) {
                    float impact;
                    float knockback;
                    if (damageSource instanceof EpicFightDamageSource efsource) {
                        if (efsource.hasTag(SourceTags.GUARD_PUNCTURE)){return;}
                        impact = event.getAmount() / 4F * (1F + ((EpicFightDamageSource) damageSource).getImpact() / 2F);
                    } else {
                        impact = event.getAmount() / 3;
                    }
                    knockback = 0.25F + Math.min(impact * 0.1F, 1.0F);
                    if (damageSource.getDirectEntity() instanceof LivingEntity) {
                        knockback += EnchantmentHelper.getKnockbackBonus((LivingEntity) damageSource.getDirectEntity()) * 0.1F;
                    }

                    float stamina = utils.getStamina() - impact * utils.getStaminaCostMultiply();
                    utils.setStamina(stamina);
                    EpicFightParticles.HIT_BLUNT.get().spawnParticleWithArgument(((ServerLevel) event.getEntityLiving().level), HitParticleType.FRONT_OF_EYES, HitParticleType.ZERO, event.getEntityLiving(), damageSource.getDirectEntity());
                    if (stamina >= 0F) {
                        event.setCanceled(true);
                        float counter_cost = utils.getDefaultCounterCost();
                        Random random = chpatch.getOriginal().getRandom();
                        if (random.nextFloat() < utils.getDefaultCounterChance() && stamina >= counter_cost) {
                            chpatch.getOriginal().addEffect(new MobEffectInstance(  EpicFightMobEffects.STUN_IMMUNITY.get(), 25));
                            utils.setAttackSpeed(utils.getCounterMotion().getSecond());
                            chpatch.playAnimationSynchronized(utils.getCounterMotion().getFirst(),0);
                            chpatch.playSound(EpicFightSounds.CLASH, -0.05F, 0.1F);
                            chpatch.knockBackEntity(damageSource.getDirectEntity().position(), 0.1F);
                            utils.setBlockTick(0);
                            utils.setStamina(utils.getStamina() - counter_cost);
                        } else {
                            chpatch.playAnimationSynchronized(success, 0.1F);
                            chpatch.playSound(EpicFightSounds.CLASH, -0.05F, 0.1F);
                            chpatch.knockBackEntity(damageSource.getDirectEntity().position(), knockback);
                        }
                    } else {
                        event.setCanceled(true);
                        utils.setBlockTick(0);
                        chpatch.getOriginal().hurt(event.getSource(),event.getAmount()/2);
                        chpatch.playSound(EpicFightSounds.NEUTRALIZE_MOBS, 3.0F, 0.0F, 0.1F);
                        chpatch.playAnimationSynchronized(fail, 0.1F);
                        utils.setStamina(utils.getMaxStamina());
                    }
                }
            }
        }
    }
}
