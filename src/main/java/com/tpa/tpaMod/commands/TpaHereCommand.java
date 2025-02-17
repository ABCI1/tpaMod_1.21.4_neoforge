package com.tpa.tpaMod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TpaHereCommand {
    private static final int PERMISSION_LEVEL = 0;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tpahere")
                .requires(source -> source.hasPermission(PERMISSION_LEVEL))
                .then(Commands.argument("target", StringArgumentType.string())
                        .suggests(TpaHereCommand::suggestTargets) // 添加自动补全逻辑
                        .executes(context -> {
                            ServerPlayer sender = context.getSource().getPlayerOrException();
                            String targetName = StringArgumentType.getString(context, "target");

                            if (sender.getGameProfile().getName().equalsIgnoreCase(targetName)) {
                                sender.sendSystemMessage(Component.literal("你不能把自己传送到这里"));
                                return 0;
                            }

                            ServerPlayer targetPlayer = context.getSource().getServer().getPlayerList().getPlayerByName(targetName);
                            if (targetPlayer != null) {
                                // 存储传送请求
                                TpaRequests.addRequest(sender, targetPlayer);

                                // 发送提示信息
                                sender.sendSystemMessage(Component.literal("请求 " + targetName + " 传送到当前位置"));

                                Component acceptMessage = Component.literal("【同意】 ")
                                        .setStyle(Style.EMPTY
                                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept"))
                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("接受传送")))
                                                .withColor(0x00FF00));

                                Component denyMessage = Component.literal(" 【拒绝】")
                                        .setStyle(Style.EMPTY
                                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny"))
                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("拒绝传送")))
                                                .withColor(0xFF0000));

                                targetPlayer.sendSystemMessage(Component.literal(sender.getName().getString() + "请求将你传送到对方所在位置").append(acceptMessage).append(denyMessage));
                                return 1;
                            } else {
                                sender.sendSystemMessage(Component.literal(targetName + " 不在线"));
                                return 0;
                            }
                        })));
    }

    // 提供自动补全建议
    private static CompletableFuture<Suggestions> suggestTargets(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        ServerLevel level = context.getSource().getLevel();
        List<String> targets = new ArrayList<>();

        // 添加在线玩家名称
        level.getServer().getPlayerList().getPlayers().forEach(player -> targets.add(player.getGameProfile().getName()));

        // 提供自动补全建议
        return SharedSuggestionProvider.suggest(targets, builder);
    }
}