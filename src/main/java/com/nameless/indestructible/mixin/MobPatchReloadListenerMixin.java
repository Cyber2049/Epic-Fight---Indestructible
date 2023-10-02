package com.nameless.indestructible.mixin;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.nameless.indestructible.gameasset.GuardAnimations;
import com.nameless.indestructible.utils.BehaviorUtils;
import com.nameless.indestructible.utils.ExtraPredicate;
import com.nameless.indestructible.utils.ProviderUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.data.reloader.MobPatchReloadListener;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static yesman.epicfight.api.data.reloader.MobPatchReloadListener.deserializeBehaviorPredicate;

@Mixin(MobPatchReloadListener.class)
public class MobPatchReloadListenerMixin {

    @Inject(method = "deserializeMobPatchProvider(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/nbt/CompoundTag;Z)Lyesman/epicfight/api/data/reloader/MobPatchReloadListener$AbstractMobPatchProvider;", at = @At("RETURN"), cancellable = true)
    private static void onDeserializeMobPatchProvider(EntityType<?> entityType, CompoundTag tag, boolean clientSide, CallbackInfoReturnable<MobPatchReloadListener.AbstractMobPatchProvider> cir) {
        MobPatchReloadListener.AbstractMobPatchProvider provider = cir.getReturnValue();
        boolean humanoid = tag.getBoolean("isHumanoid") && tag.getBoolean("isHumanoid");
        boolean disabled = tag.contains("disabled") && tag.getBoolean("disabled");
        if(!disabled && !tag.contains("preset") && humanoid){
            MobPatchReloadListener.CustomMobPatchProvider hprovider = (MobPatchReloadListener.CustomMobPatchProvider) provider;
            ((ProviderUtils) hprovider).setGuardMotions(deserializeGuardMotions(tag.getList("custom_guard_motion",10)));
            if(!clientSide) {
                ((ProviderUtils) hprovider).setBlockStamina(tag.getCompound("attributes").contains("block_stamina") ? (float) tag.getCompound("attributes").getDouble("block_stamina") : 15F);
                ((ProviderUtils) hprovider).setStaminaCostMultiply(tag.getCompound("attributes").contains("stamina_cost_multiply") ? (float) tag.getCompound("attributes").getDouble("stamina_cost_multiply") : 1F);
                ((ProviderUtils) hprovider).setCanBlockProjectile(tag.getCompound("attributes").contains("can_block_projectile") && tag.getCompound("attributes").getBoolean("can_block_projectile"));
                ((ProviderUtils) hprovider).setCounterChance(tag.getCompound("attributes").contains("counter_chance") ? (float) tag.getCompound("attributes").getDouble("counter_chance") : 0.25F);
                ((ProviderUtils) hprovider).setCounterCost(tag.getCompound("attributes").contains("counter_cost") ? (float) tag.getCompound("attributes").getDouble("counter_cost") : 1.5F);
            }
            cir.setReturnValue(hprovider);
        }
    }

    /*
    @Unique
    private static Map<WeaponCategory, CustomGuardAnimation> deserializeCustomGuardAnimations(CompoundNBT defaultLivingmotions) {
        Map<WeaponCategory, CustomGuardAnimation> guardAnimations = Maps.newHashMap();

        for (String key : defaultLivingmotions.getAllKeys()) {
            String animation = defaultLivingmotions.getString(key);
            guardAnimations.put(WeaponCategory.ENUM_MANAGER.get(key),(CustomGuardAnimation)EpicFightMod.getInstance().animationManager.findAnimationByPath(animation));
        }
        return guardAnimations;
    }

     */

    @Unique
    private static Map<WeaponCategory, Map<Style,Pair<StaticAnimation,Pair<StaticAnimation, Float>>>> deserializeGuardMotions(ListTag tag){
        Map<WeaponCategory, Map<Style,Pair<StaticAnimation,Pair<StaticAnimation, Float>>>> map = Maps.newHashMap();

        for (int i = 0; i < tag.size(); i++) {
            CompoundTag list = tag.getCompound(i);
            Style style = Style.ENUM_MANAGER.get(list.getString("style"));
            ImmutableSet.Builder<Pair<StaticAnimation, Float>> pair = ImmutableSet.builder();
            StaticAnimation guard = list.contains("guard") ? EpicFightMod.getInstance().animationManager.findAnimationByPath(list.getString("guard")) : GuardAnimations.MOB_LONGSWORD_GUARD;
            StaticAnimation counter = list.contains("counter") ? EpicFightMod.getInstance().animationManager.findAnimationByPath(list.getString("counter")) : GuardAnimations.MOB_COUNTER_ATTACK;
            Float speed = list.contains("speed") ? (float)list.getDouble("speed") : 1F;

            Tag weponTypeTag = list.get("weapon_categories");

            if (weponTypeTag instanceof StringTag) {
                WeaponCategory weaponCategory = WeaponCategory.ENUM_MANAGER.get(weponTypeTag.getAsString());
                if (!map.containsKey(weaponCategory)) {
                    map.put(weaponCategory, Maps.newHashMap());
                }
                map.get(weaponCategory).put(style, Pair.of(guard,Pair.of(counter,speed)));

            } else if (weponTypeTag instanceof ListTag weponTypesTag) {

                for (int j = 0; j < weponTypesTag.size(); j++) {
                    WeaponCategory weaponCategory = WeaponCategory.ENUM_MANAGER.get(weponTypesTag.getString(j));
                    if (!map.containsKey(weaponCategory)) {
                        map.put(weaponCategory, Maps.newHashMap());
                    }
                    map.get(weaponCategory).put(style, Pair.of(guard,Pair.of(counter,speed)));
                }
            }
        }
        return map;
    }

    /**
     * @author namelesslk
     * @reason  edit speed
     */
    @Overwrite
    public static <T extends MobPatch<?>> CombatBehaviors.Builder<T> deserializeCombatBehaviorsBuilder(ListTag tag) {
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
                if(behavior.contains("animation")) {
                    StaticAnimation animation = EpicFightMod.getInstance().animationManager.findAnimationByPath(behavior.getString("animation"));
                    float speed = behavior.contains("play_speed") ? (float) behavior.getDouble("play_speed") : 1F;
                    float stamina = behavior.contains("stamina") ? (float) behavior.getDouble("stamina") : 0F;
                    float convertTime = behavior.contains("convert_time") ? (float)behavior.getDouble("convert_time") : 0F;
                    ((BehaviorUtils<?>) behaviorBuilder).customAttackAnimation(animation,convertTime,speed,stamina);
                } else if (behavior.contains("guard")){
                    ((BehaviorUtils<?>) behaviorBuilder).setGuardTime(behavior.getInt("guard"));
                }
                behaviorBuilder.predicate(new ExtraPredicate.IsNotBlocking<>());

                for (int k = 0; k < conditionList.size(); k++) {
                    CompoundTag condition = conditionList.getCompound(k);
                    CombatBehaviors.BehaviorPredicate<T> predicate = deserializeBehaviorPredicate(condition.getString("predicate"), condition);
                    behaviorBuilder.predicate(predicate);
                }

                behaviorSeriesBuilder.nextBehavior(behaviorBuilder);
            }

            builder.newBehaviorSeries(behaviorSeriesBuilder);
        }

        return builder;
    }

    @Inject(method = "deserializeBehaviorPredicate(Ljava/lang/String;Lnet/minecraft/nbt/CompoundTag;)Lyesman/epicfight/world/entity/ai/goal/CombatBehaviors$BehaviorPredicate;", at = @At("HEAD"), cancellable = true)
    private static <T extends MobPatch<?>> void onDeserializeBehaviorPredicate(String type, CompoundTag args, CallbackInfoReturnable<CombatBehaviors.BehaviorPredicate<T>> cir) {
        CombatBehaviors.BehaviorPredicate<T> predicate = null;
        List<String[]> loggerNote = Lists.newArrayList();

        switch (type) {
            case "guard_break":
                if(!args.contains("guard_break")){
                    loggerNote.add(new String[] {"guard_break", "invert", "boolean", ""});
                }
                predicate = new ExtraPredicate.TargetIsGuardBreak<T>(args.getBoolean("invert"));
                break;

            case "knock_down":
                if(!args.contains("knock_down")){
                    loggerNote.add(new String[] {"knock_down", "invert", "boolean", ""});
                }
                predicate = new ExtraPredicate.TargetIsKnockDown<T>(args.getBoolean("invert"));
                break;

            case "attack_level":
                if(!args.contains("min",3)){
                    loggerNote.add(new String[] {"level","min","int",""});
                }
                if(!args.contains("max",3)){
                    loggerNote.add(new String[] {"level","max","int",""});
                }
                predicate = new ExtraPredicate.TargetWithinState<>(args.getInt("min"),args.getInt("max"));
                break;

            case "stamina":
                if (!args.contains("stamina", 6)) {
                    loggerNote.add(new String[] {"stamina", "stamina", "double", "0.0F"});
                }

                if (!args.contains("comparator", 8)) {
                    loggerNote.add(new String[] {"stamina", "comparator", "string", ""});
                }
                predicate = new ExtraPredicate.SelfStamina<T>((float) args.getDouble("stamina"),CombatBehaviors.Health.Comparator.valueOf(args.getString("comparator").toUpperCase(Locale.ROOT)));
                break;

            case "using_item":
                if(!args.contains("edible")){
                    loggerNote.add(new String[] {"using_item", "edible", "boolean", ""});
                }
                predicate = new ExtraPredicate.TargetIsUsingItem<T>(args.getBoolean("edible"));
                break;
        }

        for (String[] formatArgs : loggerNote) {
            EpicFightMod.LOGGER.info(String.format("[Custom Entity Error] can't find a proper argument for %s. [name: %s, type: %s, default: %s]", (Object[])formatArgs));
        }

        if (predicate != null) {
            cir.setReturnValue(predicate);
            cir.cancel();
        }
    }
}
