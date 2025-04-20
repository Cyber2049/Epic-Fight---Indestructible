package com.nameless.indestructible.data.conditions;

import com.nameless.indestructible.world.capability.Utils.IAdvancedCapability;
import io.netty.util.internal.StringUtil;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.client.gui.datapack.widgets.ComboBox;
import yesman.epicfight.client.gui.datapack.widgets.ResizableEditBox;
import yesman.epicfight.data.conditions.Condition.EntityPatchCondition;
import yesman.epicfight.data.conditions.entity.HealthPoint;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.List;
import java.util.Locale;

public class SelfStamina extends EntityPatchCondition {
	private float value;
	private HealthPoint.Comparator comparator;

	public SelfStamina() {}

	public SelfStamina(float stamina, HealthPoint.Comparator comparator) {
		this.value = stamina;
		this.comparator = comparator;
	}
	
	@Override
	public SelfStamina read(CompoundTag tag) {
		if (!tag.contains("comparator")) {
			throw new IllegalArgumentException("stamina condition error: comparator not specified!");
		}
		
		if (!tag.contains("stamina")) {
			throw new IllegalArgumentException("stamina condition error: health not specified!");
		}
		
		String sComparator = tag.getString("comparator").toUpperCase(Locale.ROOT);
		
		try {
			this.comparator = HealthPoint.Comparator.valueOf(sComparator);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("stamina condition error: invalid comparator " + sComparator);
		}
		
		this.value = tag.getFloat("stamina");
		
		return this;
	}
	
	@Override
	public CompoundTag serializePredicate() {
		CompoundTag tag = new CompoundTag();
		tag.putString("comparator", this.comparator.toString().toLowerCase(Locale.ROOT));
		tag.putFloat("stamina", this.value);
		
		return tag;
	}
	
	@Override
	public boolean predicate(LivingEntityPatch<?> self) {
		if(!(self instanceof IAdvancedCapability iac)) return true;
		float stamina = iac.getStamina();
		float maxstamina = iac.getMaxStamina();
        return switch (this.comparator) {
            case LESS_ABSOLUTE -> this.value > stamina;
            case GREATER_ABSOLUTE -> this.value < stamina;
            case LESS_RATIO -> this.value > stamina / maxstamina;
            case GREATER_RATIO -> this.value < stamina / maxstamina;
        };

    }
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public List<ParameterEditor> getAcceptingParameters(Screen screen) {
		ResizableEditBox editbox = new ResizableEditBox(screen.getMinecraft().font, 0, 0, 0, 0, Component.literal("Don't use this"), null, null);

		AbstractWidget comboBox = new ComboBox<>(screen, screen.getMinecraft().font, 0, 0, 0, 0, null, null, 4, Component.literal("in here"), List.of(HealthPoint.Comparator.values()), ParseUtil::snakeToSpacedCamel, null);

		editbox.setFilter((context) -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Float::parseFloat));

		return List.of(
				ParameterEditor.of((value) -> FloatTag.valueOf(Float.parseFloat(value.toString())), (tag) -> ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(tag, Tag::getAsString)), editbox),
				ParameterEditor.of((value) -> StringTag.valueOf(value.toString().toLowerCase(Locale.ROOT)), (tag) -> ParseUtil.enumValueOfOrNull(HealthPoint.Comparator.class, ParseUtil.nullOrToString(tag, Tag::getAsString)), comboBox)
		);
	}
}
