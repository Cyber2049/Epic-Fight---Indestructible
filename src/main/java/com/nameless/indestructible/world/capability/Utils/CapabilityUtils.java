package com.nameless.indestructible.world.capability.Utils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;

import java.util.Map;

public class CapabilityUtils {
    public static Map<Attribute, AttributeInstance> putEpicFightAttributes(Map<Attribute, AttributeInstance> originalMap) {
        Map<Attribute, AttributeInstance> newMap = Maps.newHashMap();
        AttributeSupplier supplier = AttributeSupplier.builder()
                .add(Attributes.ATTACK_DAMAGE)
                .add(EpicFightAttributes.WEIGHT.get())
                .add(EpicFightAttributes.IMPACT.get())
                .add(EpicFightAttributes.ARMOR_NEGATION.get())
                .add(EpicFightAttributes.MAX_STRIKES.get())
                .add(EpicFightAttributes.STUN_ARMOR.get())
                .add(EpicFightAttributes.EXECUTION_RESISTANCE.get())
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
}
