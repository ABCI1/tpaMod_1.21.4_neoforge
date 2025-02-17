package com.tpa.tpaMod.commands;

import com.mojang.brigadier.Command;
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

public class TpaCommand {
    private static final int PERMISSION_LEVEL = 0; // 设置权限等级

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tpa")
                .requires(source -> source.hasPermission(PERMISSION_LEVEL))
                .then(Commands.argument("target", StringArgumentType.string())
                        .suggests(TpaCommand::suggestTargets) // 添加自动补全逻辑
                        .executes(context -> {
                            ServerPlayer sender = context.getSource().getPlayerOrException();
                            String targetName = StringArgumentType.getString(context, "target");

                            // 检查是否传送自己
                            if (sender.getGameProfile().getName().equalsIgnoreCase(targetName)) {
                                sender.sendSystemMessage(Component.literal("你不能把自己传送到这里"));
                                return 0; // 返回失败
                            }

                            // 查找目标玩家或命名实体
                            ServerPlayer targetPlayer = context.getSource().getServer().getPlayerList().getPlayerByName(targetName);
                            if (targetPlayer != null) {
                                // 存储传送请求
                                TpaRequests.addRequest(sender, targetPlayer);

                                // 如果目标是玩家，发送提示信息
                                sender.sendSystemMessage(Component.literal("请求传送到 " + targetName));

                                // 创建可点击的链接
                                Component acceptMessage = Component.literal("【同意】 ")
                                        .setStyle(Style.EMPTY
                                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept"))
                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("同意传送")))
                                                .withColor(0x00FF00)); // 绿色

                                Component denyMessage = Component.literal(" 【拒绝】")
                                        .setStyle(Style.EMPTY
                                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny"))
                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("拒绝传送")))
                                                .withColor(0xFF0000)); // 红色

                                targetPlayer.sendSystemMessage(Component.literal(sender.getName().getString() + " 请求传送到当前位置").append(acceptMessage).append(denyMessage));
                                return Command.SINGLE_SUCCESS;
                            } else {
                                sender.sendSystemMessage(Component.literal("目标不存在"));
                            }
                            return 0;

                        })));
    }

    // 提供自动补全建议
    private static CompletableFuture<Suggestions> suggestTargets(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        ServerLevel level = context.getSource().getLevel();
        List<String> targets = new ArrayList<>();

        // 添加在线玩家名称（绿色）
        level.getServer().getPlayerList().getPlayers().forEach(player -> {
            Component playerComponent = Component.literal(player.getGameProfile().getName())
                    .setStyle(Style.EMPTY.withColor(0x00FF00)); // 绿色
            targets.add(playerComponent.getString());
        });


        // 提供自动补全建议
        return SharedSuggestionProvider.suggest(targets, builder);
    }
}