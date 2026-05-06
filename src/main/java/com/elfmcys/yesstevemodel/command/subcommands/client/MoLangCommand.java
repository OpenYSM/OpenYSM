package com.elfmcys.yesstevemodel.command.subcommands.client;

import com.elfmcys.yesstevemodel.capability.PlayerCapabilityProvider;
import com.elfmcys.yesstevemodel.client.entity.GeoEntity;
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

public class MoLangCommand {

    private static final String MOLANG_NAME = "molang";

    private static final String WATCH_NAME = "watch";

    private static final String ADD_NAME = "add";

    private static final String PRE_NAME = "pre";

    private static final String POST_NAME = "post";

    private static final String CLEAR_NAME = "clear";

    private static final String REMOVE_NAME = "remove";

    private static final String EXP_NAME_NAME = "exp_name";

    private static final String EXP_NAME = "exp";

    private static final String EXECUTE_NAME = "execute";

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        LiteralArgumentBuilder<CommandSourceStack> molang = Commands.literal(MOLANG_NAME);
        LiteralArgumentBuilder<CommandSourceStack> watch = Commands.literal(WATCH_NAME);
        LiteralArgumentBuilder<CommandSourceStack> add = Commands.literal(ADD_NAME);
        LiteralArgumentBuilder<CommandSourceStack> pre = Commands.literal(PRE_NAME);
        LiteralArgumentBuilder<CommandSourceStack> post = Commands.literal(POST_NAME);
        LiteralArgumentBuilder<CommandSourceStack> clear = Commands.literal(CLEAR_NAME);
        LiteralArgumentBuilder<CommandSourceStack> remove = Commands.literal(REMOVE_NAME);
        LiteralArgumentBuilder<CommandSourceStack> execute = Commands.literal(EXECUTE_NAME);
        Supplier<RequiredArgumentBuilder<CommandSourceStack, String>> expName = () -> Commands.argument(EXP_NAME_NAME, StringArgumentType.string());
        Supplier<RequiredArgumentBuilder<CommandSourceStack, String>> exp = () -> Commands.argument(EXP_NAME, StringArgumentType.greedyString()).suggests(RootClientCommand.VARS_SUGGESTION_PROVIDER);
        watch.then(add.then(pre.then((expName.get()).then((exp.get()).executes(commandContext -> addWatch(commandContext, MolangWatchRegistry.EvaluationPhase.PRE_ANIMATION))))).then(post.then((expName.get()).then((exp.get()).executes(commandContext2 -> addWatch(commandContext2, MolangWatchRegistry.EvaluationPhase.POST_ANIMATION)))))).then(remove.then((expName.get()).executes(MoLangCommand::removeWatch))).then(clear.executes(MoLangCommand::clearWatch));
        molang.then(watch).then(execute.then((exp.get()).executes(MoLangCommand::executeExperssion)));
        return molang;
    }

    public static int addWatch(CommandContext<CommandSourceStack> context, MolangWatchRegistry.EvaluationPhase watchRegistry) {
        if (!isClientSide()) {
            return Command.SINGLE_SUCCESS;
        }
        String string = StringArgumentType.getString(context, EXP_NAME_NAME);
        try {
            IValue value = GeckoLibCache.parseSimpleExpression(StringArgumentType.getString(context, EXP_NAME));
            Minecraft.getInstance().execute(() -> {
                Minecraft.getInstance().player.getCapability(PlayerCapabilityProvider.PLAYER_CAP).ifPresent(cap -> {
                    AnimationDebugOverlay.getMolangWatch().addWatch(watchRegistry, string, value);
                });
            });
            return Command.SINGLE_SUCCESS;
        } catch (ParseException e) {
            context.getSource().sendFailure(Component.translatable("message.yes_steve_model.model.debug_animation.parser_error", e.getMessage()));
            return Command.SINGLE_SUCCESS;
        }
    }

    private static int removeWatch(CommandContext<CommandSourceStack> context) {
        if (!isClientSide()) {
            return Command.SINGLE_SUCCESS;
        }
        String string = StringArgumentType.getString(context, EXP_NAME_NAME);
        Minecraft.getInstance().execute(() -> {
            Minecraft.getInstance().player.getCapability(PlayerCapabilityProvider.PLAYER_CAP).ifPresent(cap -> {
                AnimationDebugOverlay.getMolangWatch().removeWatch(string);
            });
        });
        return Command.SINGLE_SUCCESS;
    }

    private static int clearWatch(CommandContext<CommandSourceStack> context) {
        if (!isClientSide()) {
            return Command.SINGLE_SUCCESS;
        }
        Minecraft.getInstance().execute(() -> {
            Minecraft.getInstance().player.getCapability(PlayerCapabilityProvider.PLAYER_CAP).ifPresent(cap -> {
                AnimationDebugOverlay.getMolangWatch().clearAll();
            });
        });
        return Command.SINGLE_SUCCESS;
    }

    private static int executeExperssion(CommandContext<CommandSourceStack> context) {
        if (!isClientSide()) {
            return Command.SINGLE_SUCCESS;
        }
        try {
            IValue value = GeckoLibCache.parseSimpleExpression(StringArgumentType.getString(context, EXP_NAME));
            GeoEntity<?> geoEntity = AnimationDebugOverlay.getActiveModel();
            if (geoEntity == null) {
                geoEntity = Minecraft.getInstance().player.getCapability(PlayerCapabilityProvider.PLAYER_CAP).orElse(null);
            }
            if (geoEntity != null) {
                geoEntity.executeExpression(value, true, false, str -> Minecraft.getInstance().player.sendSystemMessage(Component.translatable("message.yes_steve_model.model.debug_animation.result", str)));
            }
            return Command.SINGLE_SUCCESS;
        } catch (ParseException e) {
            context.getSource().sendFailure(Component.translatable("message.yes_steve_model.model.debug_animation.parser_error", e.getMessage()));
            return Command.SINGLE_SUCCESS;
        }
    }

    private static boolean isClientSide() {
        return Minecraft.getInstance() != null && Minecraft.getInstance().player != null;
    }
}