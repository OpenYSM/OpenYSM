package com.elfmcys.yesstevemodel.command;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.command.subcommands.*;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class RootCommand {

    private static final String ROOT_NAME = "ysm";

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(ROOT_NAME);
        root.then(ModelCommand.register());
        root.then(AuthCommand.register());
        root.then(ExportCommand.register());
        root.then(PlayAnimationCommand.register());
        root.then(MoLangCommand.register());
        root.then(PingCommand.register());
        dispatcher.register(root);
    }

    public static void registerFallbackCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(ROOT_NAME);
        root.then(Commands.argument("any", StringArgumentType.greedyString()).executes(commandContext -> {
            if (commandContext.getSource().isPlayer()) {
                commandContext.getSource().sendSystemMessage(YesSteveModel.getUnavailableComponent());
                return 1;
            }
            commandContext.getSource().sendSystemMessage(Component.literal(YesSteveModel.getErrorMessage()));
            return 1;
        }));
        dispatcher.register(root);
    }
}