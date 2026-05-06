package com.elfmcys.yesstevemodel.command.subcommands.client;

import com.elfmcys.yesstevemodel.client.renderer.AnimationDebugOverlay;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;

public class DebugCommand {

    private static final String DEBUG_NAME = "debug";

    private static final String ARG_NAME = "target";

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal(DEBUG_NAME).then(Commands.argument(ARG_NAME, EntityArgument.entity()).executes(DebugCommand::debugEntity));
    }

    private static int debugEntity(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (AnimationDebugOverlay.tryUpdateFromEntity(Minecraft.getInstance().level.getEntity(EntityArgument.getEntity(context, ARG_NAME).getId()))) {
            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }
}