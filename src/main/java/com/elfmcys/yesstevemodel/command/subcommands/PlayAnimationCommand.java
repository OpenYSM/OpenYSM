package com.elfmcys.yesstevemodel.command.subcommands;

import com.elfmcys.yesstevemodel.event.CommandRegistry;
import com.elfmcys.yesstevemodel.capability.ModelInfoCapabilityProvider;
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

import java.util.Collection;

public class PlayAnimationCommand {

    private static final String PLAY_NAME = "play";

    private static final String TARGETS_NAME = "targets";

    private static final String ANIMATION_NAME = "animation";

    private static final String STOP = "stop";

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        LiteralArgumentBuilder<CommandSourceStack> play = Commands.literal(PLAY_NAME).requires(commandSourceStack -> YSMMessageFormatter.hasCommandPermission(commandSourceStack, 2));
        play.then(Commands.argument(TARGETS_NAME, EntityArgument.players()).then(Commands.argument(ANIMATION_NAME, StringArgumentType.string()).suggests(CommandRegistry.ANIMATION_NAMES).executes(PlayAnimationCommand::playAnimation)));
        return play;
    }

    private static int playAnimation(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, TARGETS_NAME);
        String animation = StringArgumentType.getString(context, ANIMATION_NAME);
        players.forEach(player -> player.getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(cap -> {
            if (STOP.equals(animation)) {
                cap.stopAnimation(player);
            } else {
                cap.playAnimation(player, animation);
            }
        }));
        return Command.SINGLE_SUCCESS;
    }
}