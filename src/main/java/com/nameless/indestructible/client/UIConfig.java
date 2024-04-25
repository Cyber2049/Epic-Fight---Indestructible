package com.nameless.indestructible.client;

import net.minecraftforge.common.ForgeConfigSpec;

public class UIConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.ConfigValue<Boolean> REPLACE_UI;
    public static final ForgeConfigSpec SPEC;
    static {
        BUILDER.push("replace entityindicator");
        BUILDER.comment("replace original entityindicator");
        REPLACE_UI = BUILDER.define("replace_ui", true);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

}
