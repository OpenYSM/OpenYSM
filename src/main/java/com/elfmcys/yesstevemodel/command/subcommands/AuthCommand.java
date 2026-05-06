package com.elfmcys.yesstevemodel.command.subcommands;

import com.elfmcys.yesstevemodel.model.ServerModelManager;
import com.elfmcys.yesstevemodel.event.CommandRegistry;
import com.elfmcys.yesstevemodel.capability.AuthModelsCapabilityProvider;
import com.elfmcys.yesstevemodel.capability.ModelInfoCapabilityProvider;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import com.elfmcys.yesstevemodel.network.message.S2CSyncAuthModelsPacket;
import com.elfmcys.yesstevemodel.util.YSMMessageFormatter;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

public class AuthCommand {

    private static final String AUTH_NAME = "auth";
    private static final String ADD_NAME = "add";
    private static final String REMOVE_NAME = "remove";

    private static final String ALL_NAME = "all";

    private static final String CLEAR_NAME = "clear";

    private static final String TARGETS_NAME = "targets";

    private static final String MODEL_ID_NAME = "model_id";

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        LiteralArgumentBuilder<CommandSourceStack> AUTH = Commands.literal(AUTH_NAME).requires(commandSourceStack -> YSMMessageFormatter.hasCommandPermission(commandSourceStack, 2));
        RequiredArgumentBuilder<CommandSourceStack, EntitySelector> targets = Commands.argument(TARGETS_NAME, EntityArgument.players());
        RequiredArgumentBuilder<CommandSourceStack, String> modelId = Commands.argument(MODEL_ID_NAME, StringArgumentType.string()).suggests(CommandRegistry.MODEL_IDS);
        AUTH.then(targets.then(Commands.literal(ADD_NAME).then(modelId.executes(AuthCommand::addAuthModel))));
        AUTH.then(targets.then(Commands.literal(REMOVE_NAME).then(modelId.executes(AuthCommand::removeAuthModel))));
        AUTH.then(targets.then(Commands.literal(ALL_NAME).executes(AuthCommand::addAllAuthModel)));
        AUTH.then(targets.then(Commands.literal(CLEAR_NAME).executes(AuthCommand::executeClear)));
        return AUTH;
    }

    private static int addAuthModel(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, TARGETS_NAME);
        String string = StringArgumentType.getString(context, MODEL_ID_NAME);
        if (!ServerModelManager.getServerModelInfo().containsKey(string)) {
            context.getSource().sendSuccess(() -> Component.translatable("commands.yes_steve_model.export.not_exist", string), true);
            return Command.SINGLE_SUCCESS;
        }
        targets.forEach(player -> {
            player.getCapability(AuthModelsCapabilityProvider.AUTH_MODELS_CAP).ifPresent(ownModelCap -> {
                ownModelCap.addModel(string);
                NetworkHandler.sendToClientPlayer(new S2CSyncAuthModelsPacket(ownModelCap.getAuthModels()), player);
                context.getSource().sendSuccess(() -> Component.translatable("commands.yes_steve_model.auth_model.add.info", string, player.getScoreboardName()), true);
            });
        });
        return Command.SINGLE_SUCCESS;
    }

    private static int addAllAuthModel(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        EntityArgument.getPlayers(context, TARGETS_NAME).forEach(player -> player.getCapability(AuthModelsCapabilityProvider.AUTH_MODELS_CAP).ifPresent(ownModelCap -> {
            Set<String> setKeySet = ServerModelManager.getServerModelInfo().keySet();
            Objects.requireNonNull(ownModelCap);
            setKeySet.forEach(ownModelCap::addModel);
            NetworkHandler.sendToClientPlayer(new S2CSyncAuthModelsPacket(ownModelCap.getAuthModels()), player);
            context.getSource().sendSuccess(() -> Component.translatable("commands.yes_steve_model.auth_model.all.info", player.getScoreboardName()), true);
        }));
        return Command.SINGLE_SUCCESS;
    }

    private static int removeAuthModel(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, TARGETS_NAME);
        String modelName = StringArgumentType.getString(context, MODEL_ID_NAME);
        targets.forEach(player -> player.getCapability(AuthModelsCapabilityProvider.AUTH_MODELS_CAP).ifPresent(ownModelsCap -> {
            ownModelsCap.removeModel(modelName);
            player.getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(modelIdCap -> {
                if (ServerModelManager.getAuthModels().contains(modelIdCap.getModelId()) && !ownModelsCap.containsModel(modelIdCap.getModelId())) {
                    modelIdCap.resetToDefault();
                }
            });
            NetworkHandler.sendToClientPlayer(new S2CSyncAuthModelsPacket(ownModelsCap.getAuthModels()), player);
            context.getSource().sendSuccess(() -> Component.translatable("commands.yes_steve_model.auth_model.remove.info", modelName, player.getScoreboardName()), true);
        }));
        return Command.SINGLE_SUCCESS;
    }

    private static int executeClear(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        EntityArgument.getPlayers(context, TARGETS_NAME).forEach(player -> player.getCapability(AuthModelsCapabilityProvider.AUTH_MODELS_CAP).ifPresent(ownModelCap -> {
            ownModelCap.clear();
            player.getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(modelIdCap -> {
                if (ServerModelManager.getAuthModels().contains(modelIdCap.getModelId())) {
                    modelIdCap.resetToDefault();
                }
            });
            NetworkHandler.sendToClientPlayer(new S2CSyncAuthModelsPacket(ownModelCap.getAuthModels()), player);
            context.getSource().sendSuccess(() -> Component.translatable("commands.yes_steve_model.auth_model.clear.info", player.getScoreboardName()), true);
        }));
        return Command.SINGLE_SUCCESS;
    }
}