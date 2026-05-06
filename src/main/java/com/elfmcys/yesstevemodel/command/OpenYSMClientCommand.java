package com.elfmcys.yesstevemodel.command;

import com.elfmcys.yesstevemodel.command.subcommands.client.CacheCommand;
import com.elfmcys.yesstevemodel.util.YSMMessageFormatter;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class OpenYSMClientCommand {
    public static void registerClientCommands(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("openysm").requires(commandSourceStack -> YSMMessageFormatter.isCurrentClientPlayer(commandSourceStack.getEntity()));
        root.then(CacheCommand.register());
        commandDispatcher.register(root);
    }
}