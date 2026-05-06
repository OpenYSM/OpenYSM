package com.elfmcys.yesstevemodel.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ExtraPlayerRenderConfig {

    public static ForgeConfigSpec.BooleanValue DISABLE_PLAYER_RENDER;

    public static ForgeConfigSpec.IntValue PLAYER_POS_X;

    public static ForgeConfigSpec.IntValue PLAYER_POS_Y;

    public static ForgeConfigSpec.DoubleValue PLAYER_SCALE;

    public static ForgeConfigSpec.DoubleValue PLAYER_YAW_OFFSET;

    public static void define(ForgeConfigSpec.Builder builder) {
        builder.push("extra_player_render");
        builder.comment("Whether to display player");
        DISABLE_PLAYER_RENDER = builder.define("DisablePlayerRender", false);
        builder.comment("Player position x in screen");
        PLAYER_POS_X = builder.defineInRange("PlayerPosX", 10, 0, Integer.MAX_VALUE);
        builder.comment("Player position y in screen");
        PLAYER_POS_Y = builder.defineInRange("PlayerPosY", 10, 0, Integer.MAX_VALUE);
        builder.comment("Player scale in screen");
        PLAYER_SCALE = builder.defineInRange("PlayerScale", 40.0d, 8.0d, 360.0d);
        builder.comment("Player yaw offset in screen");
        PLAYER_YAW_OFFSET = builder.defineInRange("PlayerYawOffset", 5.0d, Double.MIN_VALUE, Double.MAX_VALUE);
        builder.pop();
    }
}