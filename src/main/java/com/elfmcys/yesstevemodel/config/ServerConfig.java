package com.elfmcys.yesstevemodel.config;

import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class ServerConfig {

    public static ForgeConfigSpec.IntValue THREAD_COUNT;

    public static ForgeConfigSpec.IntValue BANDWIDTH_LIMIT;

    public static ForgeConfigSpec.IntValue PLAYER_SYNC_TIMEOUT;

    public static ForgeConfigSpec.BooleanValue LOW_BANDWIDTH_USAGE;

    public static ForgeConfigSpec.BooleanValue CAN_SWITCH_MODEL;

    public static ForgeConfigSpec.ConfigValue<String> DEFAULT_MODEL_ID;

    public static ForgeConfigSpec.ConfigValue<String> DEFAULT_MODEL_TEXTURE;

    public static ForgeConfigSpec.IntValue ACCEPT_SOUND_FX;

    public static ForgeConfigSpec.ConfigValue<List<String>> CLIENT_NOT_DISPLAY_MODELS;

    public static ForgeConfigSpec buildSpec() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        defineOptions(builder);
        return builder.build();
    }

    private static void defineOptions(ForgeConfigSpec.Builder builder) {
        builder.comment("The default model ID when a player first enters the game");
        DEFAULT_MODEL_ID = builder.define("DefaultModelId", "default");
        builder.comment("The default model texture when a player first enters the game");
        DEFAULT_MODEL_TEXTURE = builder.define("DefaultModelTexture", "default");
        builder.comment("Whether or not players are allowed to switch models");
        CAN_SWITCH_MODEL = builder.define("CanSwitchModel", true);
        builder.comment("Models that are not displayed on the client model selection screen");
        builder.comment("Example: [\"default\", \"misc_3_default_boy\", \"misc_1_alex\", \"misc_2_steve\", \"wine_fox_1_taisho_maid\", \"wine_fox_7_jk\"]");
        CLIENT_NOT_DISPLAY_MODELS = builder.define("ClientNotDisplayModels", Lists.newArrayList());
        builder.push("server_scheduler");
        builder.comment("Concurrent level for processing models. Value 0 means AUTO.");
        THREAD_COUNT = builder.defineInRange("ThreadCount", 0, 0, Math.max(2, Runtime.getRuntime().availableProcessors() - 1));
        builder.comment("Bandwidth limitation during distributing models to players.(In Mbps)");
        BANDWIDTH_LIMIT = builder.defineInRange("BandwidthLimit", 5, 1, 999);
        builder.comment("Timeout for players to respond to synchronization. Value not greater than 10 means AUTO.(In seconds)");
        PLAYER_SYNC_TIMEOUT = builder.defineInRange("PlayerSyncTimeout", 0, 0, 120);
        builder.comment("Suppress network synchronization of partial features to reduce bandwidth usage");
        builder.comment("Only effective when there are tons of players");
        LOW_BANDWIDTH_USAGE = builder.define("LowBandwidthUsage", false);
        builder.comment("Skip sound effect processing to reduce server bandwidth and client memory usage");
        builder.comment("0: Accept all sounds (Default)");
        builder.comment("1: Accept short sounds only (Shorter than 4s and smaller than 40KB)");
        builder.comment("2: Reject all sounds (Not recommended)");
        builder.comment("Note: Takes effect after model reloading. Increasing this option does not cause model resynchronization, whereas decreasing it does.");
        ACCEPT_SOUND_FX = builder.defineInRange("AcceptSoundFX", 0, 0, 2);
        builder.pop();
    }
}