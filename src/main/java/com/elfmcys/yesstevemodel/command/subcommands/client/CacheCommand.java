package com.elfmcys.yesstevemodel.command.subcommands.client;

import com.elfmcys.yesstevemodel.client.ClientModelManager;
import com.elfmcys.yesstevemodel.util.YSMMessageFormatter;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class CacheCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("cache").then(Commands.literal("dump").executes(CacheCommand::dumpCache));
    }

    private static int dumpCache(CommandContext<CommandSourceStack> context) {
        CommandSourceStack sourceStack = context.getSource();

        sourceStack.sendSystemMessage(YSMMessageFormatter.withPrefix(Component.literal("开始解析并导出客户端缓存模型...")));

        ClientModelManager.exportAllCachedModels(null, exportResult -> {
            if (exportResult.getMessage() != null) {
                sourceStack.sendSystemMessage(YSMMessageFormatter.withPrefix(exportResult.getMessage()));
            }
            if (exportResult.isSuccess()) {
                sourceStack.sendSystemMessage(Component.translatable("commands.yes_steve_model.export.success", exportResult.getFilePath()));
            }
        });

        return Command.SINGLE_SUCCESS;
    }
}