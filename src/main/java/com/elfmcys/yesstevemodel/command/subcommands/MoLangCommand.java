package com.elfmcys.yesstevemodel.command.subcommands;

import com.elfmcys.yesstevemodel.network.NetworkHandler;
import com.elfmcys.yesstevemodel.network.message.S2CExecuteMolangPacket;
import com.elfmcys.yesstevemodel.util.YSMMessageFormatter;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.Collection;

public class MoLangCommand {

    private static final String MOLANG_NAME = "molang";

    private static final String EXECUTE_NAME = "execute";

    private static final String EXP_NAME = "exp";

    private static final String TARGETS_NAME = "targets";

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        LiteralArgumentBuilder<CommandSourceStack> molang = Commands.literal(MOLANG_NAME).requires(commandSourceStack -> YSMMessageFormatter.hasCommandPermission(commandSourceStack, 2));
        molang.then(Commands.literal(EXECUTE_NAME).then(Commands.argument(TARGETS_NAME, EntityArgument.players()).then(Commands.argument(EXP_NAME, StringArgumentType.greedyString()).executes(MoLangCommand::executeMoLang))));
        return molang;
    }

    private static int executeMoLang(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return sendMoLangToPlayers(StringArgumentType.getString(context, EXP_NAME), EntityArgument.getPlayers(context, TARGETS_NAME));
    }

    private static int sendMoLangToPlayers(String str, Collection<ServerPlayer> collection) {
        NetworkHandler.sendToAll(new S2CExecuteMolangPacket(collection.stream().mapToInt(Entity::getId).toArray(), str));
        return Command.SINGLE_SUCCESS;
    }
}