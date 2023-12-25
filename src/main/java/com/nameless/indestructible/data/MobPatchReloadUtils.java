package com.nameless.indestructible.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.nameless.indestructible.api.animation.types.AnimationEvent;
import com.nameless.indestructible.gameasset.GuardAnimations;
import com.nameless.indestructible.utils.BehaviorInterface;
import com.nameless.indestructible.utils.ExtraPredicate;
import com.nameless.indestructible.world.capability.AdvancedCustomHumanoidMobPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MobPatchReloadUtils {
    public static Map<Attribute, Double> deserializeAdvancedAttributes(CompoundTag tag) {
        Map<Attribute, Double> attributes = Maps.newHashMap();
        attributes.put(EpicFightAttributes.WEIGHT.get(), tag.contains("weight",6) ? tag.getDouble("weight") : 40);
        attributes.put(EpicFightAttributes.IMPACT.get(), tag.contains("impact", 6) ? tag.getDouble("impact") : 0.5);
        attributes.put(EpicFightAttributes.ARMOR_NEGATION.get(), tag.contains("armor_negation", 6) ? tag.getDouble("armor_negation") : 0.0);
        attributes.put(EpicFightAttributes.MAX_STRIKES.get(), (double)(tag.contains("max_strikes", 3) ? tag.getInt("max_strikes") : 1));
        if (tag.contains("attack_damage", 6)) {
            attributes.put(Attributes.ATTACK_DAMAGE, tag.getDouble("attack_damage"));
        }

        return attributes;
    }

    public static Map<WeaponCategory, Map<Style, CombatBehaviors.Builder<HumanoidMobPatch<?>>>> deserializeAdvancedCombatBehaviors(ListTag tag) {
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

    public static Map<WeaponCategory, Map<Style,Pair<StaticAnimation,Pair<Float, Boolean>>>> deserializeGuardMotions(ListTag tag){
        Map<WeaponCategory, Map<Style,Pair<StaticAnimation,Pair<Float, Boolean>>>> map = Maps.newHashMap();

        for (int i = 0; i < tag.size(); i++) {
            CompoundTag list = tag.getCompound(i);
            Style style = Style.ENUM_MANAGER.get(list.getString("style"));
            StaticAnimation guard = list.contains("guard") ? EpicFightMod.getInstance().animationManager.findAnimationByPath(list.getString("guard")) : GuardAnimations.MOB_LONGSWORD_GUARD;
            float stamina_cost_multiply = list.contains("stamina_cost_multiply") ? (float)list.getDouble("stamina_cost_multiply") : 1F;
            boolean canBlockProjectile = list.contains("can_block_projectile") && list.getBoolean("can_block_projectile");

            Tag weponTypeTag = list.get("weapon_categories");

            if (weponTypeTag instanceof StringTag) {
                WeaponCategory weaponCategory = WeaponCategory.ENUM_MANAGER.get(weponTypeTag.getAsString());
                if (!map.containsKey(weaponCategory)) {
                    map.put(weaponCategory, Maps.newHashMap());
                }
                map.get(weaponCategory).put(style, Pair.of(guard,Pair.of(stamina_cost_multiply,canBlockProjectile)));

            } else if (weponTypeTag instanceof ListTag weponTypesTag) {

                for (int j = 0; j < weponTypesTag.size(); j++) {
                    WeaponCategory weaponCategory = WeaponCategory.ENUM_MANAGER.get(weponTypesTag.getString(j));
                    if (!map.containsKey(weaponCategory)) {
                        map.put(weaponCategory, Maps.newHashMap());
                    }
                    map.get(weaponCategory).put(style, Pair.of(guard,Pair.of(stamina_cost_multiply,canBlockProjectile)));
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

                if(behavior.contains("animation")) {
                    StaticAnimation animation = EpicFightMod.getInstance().animationManager.findAnimationByPath(behavior.getString("animation"));
                    float speed = behavior.contains("play_speed") ? (float) behavior.getDouble("play_speed") : 1F;
                    float stamina = behavior.contains("stamina") ? (float) behavior.getDouble("stamina") : 0F;
                    float convertTime = behavior.contains("convert_time") ? (float)behavior.getDouble("convert_time") : 0F;
                    AdvancedCustomHumanoidMobPatch.CustomAnimationMotion motion = new AdvancedCustomHumanoidMobPatch.CustomAnimationMotion(animation,convertTime,speed,stamina);
                    List<AnimationEvent.TimeStampedEvent> timeCommandList = behavior.contains("command_list") ? deserializeTimeCommandList(behavior.getList("command_list", 10)) : null;
                    List<AnimationEvent.HitEvent> hitCommandList = behavior.contains("hit_command_list") ? deserializeHitCommandList(behavior.getList("hit_command_list", 10)) : null;
                    int phase = behavior.contains("set_phase") ? behavior.getInt("set_phase") : -1;
                    AdvancedCustomHumanoidMobPatch.DamageSourceModifier modifier = behavior.contains("damage_modifier") ? deserializeDamageModifier(behavior.getCompound("damage_modifier")) : null;
                    ((BehaviorInterface<?>) behaviorBuilder).customAttackAnimation(motion, modifier, timeCommandList, hitCommandList, phase);
                } else if (behavior.contains("guard")){
                    int guardTime = behavior.getInt("guard");
                    StaticAnimation counter = behavior.contains("counter") ? EpicFightMod.getInstance().animationManager.findAnimationByPath(behavior.getString("counter")) : GuardAnimations.MOB_COUNTER_ATTACK;
                    float cost = behavior.contains("counter_cost") ? (float) behavior.getDouble("counter_cost") : 3.0F;
                    float chance = behavior.contains("counter_chance") ? (float)behavior.getDouble("counter_chance") : 0.3F;
                    float speed = behavior.contains("counter_speed") ? (float)behavior.getDouble("counter_speed") : 1F;
                    ((BehaviorInterface<?>) behaviorBuilder).setGuardMotion(guardTime,counter,cost,chance,speed);
                } else if (behavior.contains("strafing")){
                    int strafingTime = behavior.getInt("strafing");
                    int inactionTime = behavior.contains("inaction_time") ?  behavior.getInt("inaction_time") : behavior.getInt("strafing");
                    float forward = behavior.contains("z_axis") ? (float) behavior.getDouble("z_axis") : 0F;
                    float clockwise = behavior.contains("x_axis") ? (float) behavior.getDouble("x_axis") : 0F;
                    ((BehaviorInterface<?>) behaviorBuilder).setStrafing(strafingTime, inactionTime, forward, clockwise);
                }

                for (int k = 0; k < conditionList.size(); k++) {
                    CompoundTag condition = conditionList.getCompound(k);
                    CombatBehaviors.BehaviorPredicate<T> predicate = deserializeAdvancedBehaviorPredicate(condition.getString("predicate"), condition);
                    behaviorBuilder.predicate(predicate);
                }

                behaviorSeriesBuilder.nextBehavior(behaviorBuilder);
            }

            builder.newBehaviorSeries(behaviorSeriesBuilder);
        }

        return builder;
    }

    private static List<AnimationEvent.TimeStampedEvent> deserializeTimeCommandList(ListTag args){
        List<AnimationEvent.TimeStampedEvent> list = Lists.newArrayList();
        for(int k = 0; k < args.size(); k++){
            CompoundTag command = args.getCompound(k);
            AnimationEvent.TimeStampedEvent event = AnimationEvent.TimeStampedEvent.CreateTimeCommandEvent(command.getFloat("time"), command.getString("command"), command.getBoolean("execute_at_target"));
            list.add(event);
        }
        return list;
    }
    private static List<AnimationEvent.HitEvent> deserializeHitCommandList(ListTag args){
        List<AnimationEvent.HitEvent> list = Lists.newArrayList();
        for(int k = 0; k < args.size(); k++){
            CompoundTag command = args.getCompound(k);
            AnimationEvent.HitEvent event = AnimationEvent.HitEvent.CreateHitCommandEvent(command.getString("command"), command.getBoolean("execute_at_target"));
            list.add(event);
        }
        return list;
    }

    private static AdvancedCustomHumanoidMobPatch.DamageSourceModifier deserializeDamageModifier(CompoundTag args){
        float damage = args.contains("damage") ? args.getFloat("damage") : 1F;
        float impact = args.contains("impact") ? args.getFloat("impact") : 1F;
        float armor_negation = args.contains("armor_negation") ? args.getFloat("armor_negation") : 1F;
        return new AdvancedCustomHumanoidMobPatch.DamageSourceModifier(damage, impact, armor_negation);
    }


    private static <T extends MobPatch<?>> CombatBehaviors.BehaviorPredicate<T> deserializeAdvancedBehaviorPredicate(String type, CompoundTag args) {
        CombatBehaviors.BehaviorPredicate<T> predicate = null;
        List<String[]> loggerNote = Lists.newArrayList();

        switch (type) {
            case "random_chance":
                if (!args.contains("chance", 6)) {
                    loggerNote.add(new String[] {"random_chance", "chance", "double", "0.0"});
                }

                predicate = new CombatBehaviors.RandomChance<>((float) args.getDouble("chance"));
                break;
            case "within_eye_height":
                predicate = new CombatBehaviors.TargetWithinEyeHeight<>();
                break;
            case "within_distance":
                if (!args.contains("min", 6)) {
                    loggerNote.add(new String[] {"within_distance", "min", "double", "0.0"});
                }

                if (!args.contains("max", 6)) {
                    loggerNote.add(new String[] {"within_distance", "max", "double", "0.0"});
                }

                predicate = new CombatBehaviors.TargetWithinDistance<>(args.getDouble("min"), args.getDouble("max"));
                break;
            case "within_angle":
                if (!args.contains("min", 6)) {
                    loggerNote.add(new String[] {"within_angle", "within_distance", "min", "double", "0.0F"});
                }

                if (!args.contains("max", 6)) {
                    loggerNote.add(new String[] {"within_angle", "max", "double", "0.0F"});
                }

                predicate = new CombatBehaviors.TargetWithinAngle<>(args.getDouble("min"), args.getDouble("max"));
                break;
            case "within_angle_horizontal":
                if (!args.contains("min", 6)) {
                    loggerNote.add(new String[] {"within_angle_horizontal", "min", "double", "0.0F"});
                }

                if (!args.contains("max", 6)) {
                    loggerNote.add(new String[] {"within_angle_horizontal", "max", "double", "0.0F"});
                }

                predicate = new CombatBehaviors.TargetWithinAngle.Horizontal<>(args.getDouble("min"), args.getDouble("max"));
                break;
            case "health":
                if (!args.contains("health", 6)) {
                    loggerNote.add(new String[] {"health", "health", "double", "0.0F"});
                }

                if (!args.contains("comparator", 8)) {
                    loggerNote.add(new String[] {"health", "comparator", "string", ""});
                }

                predicate = new CombatBehaviors.Health<>((float) args.getDouble("health"), CombatBehaviors.Health.Comparator.valueOf(args.getString("comparator").toUpperCase(Locale.ROOT)));
                break;

            case "guard_break":
                if(!args.contains("invert")){
                    loggerNote.add(new String[] {"guard_break", "invert", "boolean", ""});
                }
                predicate = new ExtraPredicate.TargetIsGuardBreak<>(args.getBoolean("invert"));
                break;

            case "knock_down":
                if(!args.contains("invert")){
                    loggerNote.add(new String[] {"knock_down", "invert", "boolean", ""});
                }
                predicate = new ExtraPredicate.TargetIsKnockDown<>(args.getBoolean("invert"));
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
                predicate = new ExtraPredicate.SelfStamina<>((float) args.getDouble("stamina"), CombatBehaviors.Health.Comparator.valueOf(args.getString("comparator").toUpperCase(Locale.ROOT)));
                break;

            case "using_item":
                if(!args.contains("edible")){
                    loggerNote.add(new String[] {"using_item", "edible", "boolean", ""});
                }
                predicate = new ExtraPredicate.TargetIsUsingItem<>(args.getBoolean("edible"));
                break;

            case "phase":
                if(!args.contains("min",3)){
                    loggerNote.add(new String[] {"phase","min","int",""});
                }
                if(!args.contains("max",3)){
                    loggerNote.add(new String[] {"phase","max","int",""});
                }
                predicate = new ExtraPredicate.Phase<>(args.getInt("min"),args.getInt("max"));
                break;
        }

        for (String[] formatArgs : loggerNote) {
            EpicFightMod.LOGGER.info(String.format("[Custom Entity Error] can't find a proper argument for %s. [name: %s, type: %s, default: %s]", (Object[])formatArgs));
        }

        if (predicate == null) {
            throw new IllegalArgumentException("[Custom Entity Error] No predicate type: " + type);
        }

        return predicate;
    }
}
