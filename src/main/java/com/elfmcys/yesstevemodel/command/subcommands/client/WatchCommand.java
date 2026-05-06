package com.elfmcys.yesstevemodel.command.subcommands.client;

import com.elfmcys.yesstevemodel.capability.PlayerCapabilityProvider;
import com.elfmcys.yesstevemodel.client.animation.molang.MolangWatchRegistry;
import com.elfmcys.yesstevemodel.client.renderer.AnimationDebugOverlay;
import com.elfmcys.yesstevemodel.command.RootClientCommand;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue;
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache;
import com.elfmcys.yesstevemodel.molang.parser.ParseException;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public class WatchCommand {

    private static final String WATCH_NAME = "watch";

    private static final String VAR_NAME = "var";

    private static final String STATE_NAME = "state";

    private static final String CLEAR_NAME = "clear";

    private static final String EXP_NAME = "exp";

    private static final String CONTROLLER_NAME = "controller";

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        LiteralArgumentBuilder<CommandSourceStack> watch = Commands.literal(WATCH_NAME);
        LiteralArgumentBuilder<CommandSourceStack> var = Commands.literal(VAR_NAME);
        LiteralArgumentBuilder<CommandSourceStack> state = Commands.literal(STATE_NAME);
        LiteralArgumentBuilder<CommandSourceStack> clear = Commands.literal(CLEAR_NAME);
        Supplier<RequiredArgumentBuilder<CommandSourceStack, String>> exp = () -> Commands.argument(EXP_NAME, StringArgumentType.greedyString()).suggests(RootClientCommand.VARS_SUGGESTION_PROVIDER);
        Supplier<RequiredArgumentBuilder<CommandSourceStack, String>> controller = () -> Commands.argument(CONTROLLER_NAME, StringArgumentType.greedyString()).suggests(RootClientCommand.CONTROLLERS_SUGGESTION_PROVIDER);
        watch.then(var.then((exp.get()).executes(WatchCommand::watchVar)));
        watch.then(state.then((controller.get()).executes(WatchCommand::watchState)));
        watch.then(clear.executes(WatchCommand::watchClear));
        return watch;
    }

    private static int watchVar(CommandContext<CommandSourceStack> context) {
        if (!isClientSide()) {
            return Command.SINGLE_SUCCESS;
        }
        Minecraft minecraft = Minecraft.getInstance();
        String string = StringArgumentType.getString(context, EXP_NAME);
        try {
            IValue value = GeckoLibCache.parseSimpleExpression(string);
            minecraft.execute(() -> {
                minecraft.player.getCapability(PlayerCapabilityProvider.PLAYER_CAP).ifPresent(cap -> {
                    AnimationDebugOverlay.getMolangWatch().addWatch(MolangWatchRegistry.EvaluationPhase.POST_ANIMATION, string, value);
                    if (!AnimationDebugOverlay.isDebugActive()) {
                        AnimationDebugOverlay.tryUpdateFromLocalPlayer();
                    }
                });
            });
            return Command.SINGLE_SUCCESS;
        } catch (ParseException e) {
            context.getSource().sendFailure(Component.translatable("message.yes_steve_model.model.debug_animation.parser_error", e.getMessage()));
            return Command.SINGLE_SUCCESS;
        }
    }

    private static int watchClear(CommandContext<CommandSourceStack> context) {
        if (!isClientSide()) {
            return Command.SINGLE_SUCCESS;
        }
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            minecraft.player.getCapability(PlayerCapabilityProvider.PLAYER_CAP).ifPresent(cap -> AnimationDebugOverlay.getMolangWatch().clearAll());
        });
        AnimationDebugOverlay.clearDebugLines();
        return Command.SINGLE_SUCCESS;
    }

    private static int watchState(CommandContext<CommandSourceStack> context) {
        if (!isClientSide()) {
            return Command.SINGLE_SUCCESS;
        }
        AnimationDebugOverlay.addDebugLine(StringArgumentType.getString(context, CONTROLLER_NAME));
        if (!AnimationDebugOverlay.isDebugActive()) {
            AnimationDebugOverlay.tryUpdateFromLocalPlayer();
            return Command.SINGLE_SUCCESS;
        }
        return Command.SINGLE_SUCCESS;
    }

    private static boolean isClientSide() {
        return Minecraft.getInstance() != null && Minecraft.getInstance().player != null;
    }
}