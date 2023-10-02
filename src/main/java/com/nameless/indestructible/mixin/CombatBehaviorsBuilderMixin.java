package com.nameless.indestructible.mixin;

import com.nameless.indestructible.utils.BehaviorUtils;
import com.nameless.indestructible.utils.CustomHumanoidMobPatchUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.network.server.SPPlayAnimation;
import yesman.epicfight.world.capabilities.entitypatch.CustomHumanoidMobPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

import java.util.function.Consumer;

@Mixin(CombatBehaviors.Behavior.Builder.class)
public class CombatBehaviorsBuilderMixin <T extends MobPatch<?>> implements BehaviorUtils<T> {
    @Shadow
    private Consumer<T> behavior;
    @Shadow
    private LivingEntityPatch.AnimationPacketProvider packetProvider = SPPlayAnimation::new;
    @Unique @SuppressWarnings("unchecked")
    public CombatBehaviors.Behavior.Builder<T> customAttackAnimation(StaticAnimation motion, float convertTime, float speed, float stamina) {
        this.behavior = (mobpatch) -> {
            if(mobpatch instanceof CustomHumanoidMobPatch){
               ((CustomHumanoidMobPatchUtils)mobpatch).setAttackSpeed(speed);
               if(stamina != 0F) ((CustomHumanoidMobPatchUtils)mobpatch).setStamina(((CustomHumanoidMobPatchUtils)mobpatch).getStamina() - stamina);
            }
            mobpatch.playAnimationSynchronized(motion, convertTime, this.packetProvider);
        };
        return (CombatBehaviors.Behavior.Builder<T>)((Object)this);
    }

    @Unique @SuppressWarnings("unchecked")
    public CombatBehaviors.Behavior.Builder<T> setGuardTime(int time) {
        this.behavior = (mobpatch) -> {
            if(mobpatch instanceof CustomHumanoidMobPatch){
                CustomHumanoidMobPatchUtils utils = (CustomHumanoidMobPatchUtils)mobpatch;
                utils.setBlockTick(time);
            }
        };
        return (CombatBehaviors.Behavior.Builder<T>)(Object)this;
    }

}
