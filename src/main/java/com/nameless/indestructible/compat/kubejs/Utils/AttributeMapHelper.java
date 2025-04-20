package com.nameless.indestructible.compat.kubejs.Utils;

import com.google.common.collect.Maps;
import com.nameless.indestructible.main.Indestructible;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

public class AttributeMapHelper {
    private final Map<Attribute, Double> attributeMap = Maps.newHashMap();
    public static AttributeMapHelper getInstance() {
        return new AttributeMapHelper();
    }
    public AttributeMapHelper addAttribute(Object object, Double value){
        if(object instanceof Attribute attribute){
            this.attributeMap.put(attribute, value);
        } else if (object instanceof String string) {
            ResourceLocation rl = new ResourceLocation(string);
            Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(rl);
            if(attribute != null) this.attributeMap.put(attribute, value);
            else Indestructible.LOGGER.info(string + " doesn't exist");
        } else Indestructible.LOGGER.info(object + " can't be recognized");
        return this;
    }
    public Map<Attribute, Double> createMap(){
        return this.attributeMap;
    }
}
