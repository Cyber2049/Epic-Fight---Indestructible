package com.nameless.indestructible.compat.kubejs;

import com.nameless.indestructible.data.JsMobPatchProviderEvent;
import com.nameless.indestructible.main.Indestructible;

public class JsEventInstance {
    public static void load(){
        Indestructible.LOGGER.info("kubejs loaded");
        JsMobPatchProviderEvent event = new JsMobPatchProviderEvent();
        PatchJSPlugin.REGISTRY.post(event);
    }
}
