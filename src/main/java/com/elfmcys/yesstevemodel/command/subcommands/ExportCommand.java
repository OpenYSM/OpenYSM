package com.elfmcys.yesstevemodel.command.subcommands;

import com.elfmcys.yesstevemodel.model.ServerModelManager;
import com.elfmcys.yesstevemodel.event.CommandRegistry;
import com.elfmcys.yesstevemodel.util.YSMMessageFormatter;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class ExportCommand {

    private static final String EXPORT_NAME = "export";

    private static final String MODEL_ID_NAME = "model_id";

    private static final String EXTRA_NAME = "extra";

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        LiteralArgumentBuilder<CommandSourceStack> export = Commands.literal(EXPORT_NAME).requires(commandSourceStack -> YSMMessageFormatter.hasCommandPermission(commandSourceStack, 2));
        RequiredArgumentBuilder<CommandSourceStack, String> modelId = Commands.argument(MODEL_ID_NAME, StringArgumentType.string()).suggests(CommandRegistry.MODEL_IDS);
        RequiredArgumentBuilder<CommandSourceStack, String> extra = Commands.argument(EXTRA_NAME, StringArgumentType.greedyString());
        export.then(modelId.executes(ExportCommand::executeExport));
        export.then(modelId.then(extra.executes(ExportCommand::executeExportWithExtra)));
        return export;
    }

    private static int executeExport(CommandContext<CommandSourceStack> context) {
        handleExport(context.getSource(), StringArgumentType.getString(context, MODEL_ID_NAME), null);
        return Command.SINGLE_SUCCESS;
    }

    private static int executeExportWithExtra(CommandContext<CommandSourceStack> context) {
        handleExport(context.getSource(), StringArgumentType.getString(context, MODEL_ID_NAME), StringArgumentType.getString(context, EXTRA_NAME));
        return Command.SINGLE_SUCCESS;
    }

    private static void handleExport(CommandSourceStack sourceStack, String str, @Nullable String str2) {
        ServerModelManager.nativeExportModel(str, str2, exportResult -> {
            if (exportResult.getMessage() != null) {
                YSMMessageFormatter.sendServerMessage(sourceStack, YSMMessageFormatter.withPrefix(exportResult.getMessage()), false);
            }
            if (exportResult.isSuccess()) {
                YSMMessageFormatter.sendServerMessage(sourceStack, Component.translatable("commands.yes_steve_model.export.success", exportResult.getFilePath()), false);
            }
        });
    }
}