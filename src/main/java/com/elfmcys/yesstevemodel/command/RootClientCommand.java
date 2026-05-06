package com.elfmcys.yesstevemodel.command;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.animation.molang.struct.RoamingStruct;
import com.elfmcys.yesstevemodel.capability.PlayerCapabilityProvider;
import com.elfmcys.yesstevemodel.client.entity.GeoEntity;
import com.elfmcys.yesstevemodel.command.subcommands.client.CacheCommand;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController;
import com.elfmcys.yesstevemodel.client.renderer.AnimationDebugOverlay;
import com.elfmcys.yesstevemodel.command.subcommands.client.WatchCommand;
import com.elfmcys.yesstevemodel.command.subcommands.client.DebugCommand;
import com.elfmcys.yesstevemodel.command.subcommands.client.MoLangCommand;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.ContextBinding;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache;
import com.elfmcys.yesstevemodel.client.entity.RoamingPropertyHolder;
import com.elfmcys.yesstevemodel.molang.runtime.Struct;
import com.elfmcys.yesstevemodel.util.YSMMessageFormatter;
import com.google.common.collect.Sets;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

public class RootClientCommand {

    private static final String ROOT_NAME = "ysmclient";

    public static final SuggestionProvider<CommandSourceStack> VARS_SUGGESTION_PROVIDER = SuggestionProviders.register(ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "vars"), (context, builder) -> {
        if (context.getSource() instanceof SharedSuggestionProvider && FMLEnvironment.dist == Dist.CLIENT) {
            return getActiveGeoModel().map(geo -> {
                HashSet<String> set = Sets.newHashSet();
                geo.getEvaluationContext().forEachPropertyName(str -> set.add(String.format("v.%s", str)));
                if (geo instanceof RoamingPropertyHolder) {
                    Struct struct = ((RoamingPropertyHolder) geo).getPropertyContainer();
                    if (struct instanceof RoamingStruct roamingStruct) {
                        roamingStruct.forEachVar(str2 -> {
                            if (roamingStruct.getProperty(StringPool.getName(str2)) != null) {
                                set.add(String.format("v.roaming.%s", str2));
                            }
                        });
                    }
                }
                GeckoLibCache.getGlobalBindings().forEach((namespace, obj) -> {
                    if (obj instanceof ContextBinding) {
                        ((ContextBinding) obj).getKeys().forEach(key -> {
                            set.add(String.format("%s.%s", namespace, key));
                        });
                    }
                });
                for (String s : geo.getModelAssembly().getExpressionCache().getFunctions().keySet()) {
                    set.add(String.format("fn.%s", s));
                }
                return SharedSuggestionProvider.suggest(set, builder);
            }).orElseGet(Suggestions::empty);
        }
        return Suggestions.empty();
    });

    public static final SuggestionProvider<CommandSourceStack> CONTROLLERS_SUGGESTION_PROVIDER = SuggestionProviders.register(ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "controllers"), (commandContext, suggestionsBuilder) -> {
        if (commandContext.getSource() instanceof SharedSuggestionProvider && FMLEnvironment.dist == Dist.CLIENT) {
            return getActiveGeoModel().map(geo -> SharedSuggestionProvider.suggest(geo.getAnimationData().getAnimationControllers().stream().map(IAnimationController::getName).collect(Collectors.toSet()), suggestionsBuilder)).orElseGet(Suggestions::empty);
        }
        return Suggestions.empty();
    });

    public static void registerClientCommands(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(ROOT_NAME).requires(commandSourceStack -> YSMMessageFormatter.isCurrentClientPlayer(commandSourceStack.getEntity()));
        root.then(MoLangCommand.register());
        root.then(WatchCommand.register());
        root.then(DebugCommand.register());
        commandDispatcher.register(root);
    }

    private static Optional<GeoEntity<?>> getActiveGeoModel() {
        LocalPlayer localPlayer;
        GeoEntity<?> geoEntity = AnimationDebugOverlay.getActiveModel();
        if (geoEntity == null && (localPlayer = Minecraft.getInstance().player) != null) {
            geoEntity = localPlayer.getCapability(PlayerCapabilityProvider.PLAYER_CAP).orElse(null);
        }
        return Optional.ofNullable(geoEntity);
    }
}