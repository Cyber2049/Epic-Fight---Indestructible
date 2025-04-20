package com.nameless.indestructible.compat.kubejs;

import com.nameless.indestructible.data.JSCustomHumanoidMobPatchProviderBuilder;
import com.nameless.indestructible.world.capability.AdvancedCustomHumanoidMobPatch;
import com.nameless.indestructible.world.capability.Utils.IAdvancedCapability;
import com.nameless.indestructible.world.capability.Utils.IAnimationEventCapability;
import com.nameless.indestructible.world.capability.Utils.IBossEventCapability;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import yesman.epicfight.world.capabilities.entitypatch.Faction;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;

public class JsCustomHumanoidMobPatch<T extends PathfinderMob> extends AdvancedCustomHumanoidMobPatch<T> implements IAdvancedCapability, IBossEventCapability, IAnimationEventCapability {
    public JsCustomHumanoidMobPatch(Faction faction, JSCustomHumanoidMobPatchProviderBuilder.JSCustomHumanoidMobPatchProvider provider) {
        super(faction, provider);
    }

    @Override
    public void initAttributes() {
        this.getOriginal().getAttribute(EpicFightAttributes.WEIGHT.get()).setBaseValue(40);
        this.getOriginal().getAttribute(EpicFightAttributes.MAX_STRIKES.get()).setBaseValue(1);
        this.getOriginal().getAttribute(EpicFightAttributes.ARMOR_NEGATION.get()).setBaseValue(0);
        this.getOriginal().getAttribute(EpicFightAttributes.IMPACT.get()).setBaseValue(0.5);
        this.getOriginal().getAttribute(EpicFightAttributes.OFFHAND_IMPACT.get()).setBaseValue(0.5F);
        this.getOriginal().getAttribute(EpicFightAttributes.OFFHAND_ARMOR_NEGATION.get()).setBaseValue(0F);
        this.getOriginal().getAttribute(EpicFightAttributes.OFFHAND_ATTACK_SPEED.get()).setBaseValue(1.2F);
        this.getOriginal().getAttribute(EpicFightAttributes.OFFHAND_MAX_STRIKES.get()).setBaseValue(1);
        this.getOriginal().getAttribute(EpicFightAttributes.MAX_STAMINA.get()).setBaseValue(15);
        this.getOriginal().getAttribute(EpicFightAttributes.STAMINA_REGEN.get()).setBaseValue(1);

        this.capabilityState.getProvider().getAttributeValues().forEach((attribute, value) -> {
            AttributeInstance attributeInstance = this.getOriginal().getAttribute(attribute);
            if(attributeInstance != null) attributeInstance.setBaseValue(value);
        });
    }

}
