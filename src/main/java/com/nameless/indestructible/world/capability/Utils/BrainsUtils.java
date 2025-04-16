package com.nameless.indestructible.world.capability.Utils;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Map;
import java.util.Set;

public class BrainsUtils {
    public static <E extends LivingEntity> void removeBehaviors(Brain<E> brain, Activity activity, Class<?> target) {
        for (Map<Activity, Set<BehaviorControl<? super E>>> map : brain.availableBehaviorsByPriority.values()) {
            Set<BehaviorControl<? super E>> set = map.get(activity);

            if (set != null) {
                set.removeIf(target::isInstance);
            }
        }
    }

    public static <E extends LivingEntity> void replaceBehaviors(Brain<E> brain, Activity activity, Class target, Behavior<? super E> newBehavior) {
        for (Map<Activity, Set<BehaviorControl<? super E>>> map : brain.availableBehaviorsByPriority.values()) {
            Set<BehaviorControl<? super E>> set = map.get(activity);

            if (set != null) {
                set.removeIf(target::isInstance);
                set.add(newBehavior);
            }
        }
    }



    public static <E extends LivingEntity> void addBehaviors(Brain<E> brain, Activity activity, Behavior<? super E> newBehavior) {
        for (Map<Activity, Set<BehaviorControl<? super E>>> map : brain.availableBehaviorsByPriority.values()) {
            Set<BehaviorControl<? super E>> set = map.get(activity);

            if (set != null) {
                set.add(newBehavior);
            }
        }
    }
}
