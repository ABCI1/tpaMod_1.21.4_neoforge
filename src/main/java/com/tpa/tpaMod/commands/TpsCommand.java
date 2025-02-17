package com.tpa.tpaMod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class TpsCommand {
    private static final int PERMISSION_LEVEL = 0; // 设置权限等级

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tps")
                .requires(source -> source.hasPermission(PERMISSION_LEVEL))
                .then(Commands.argument("target", StringArgumentType.string())
                        .suggests(TpsCommand::suggestTargets) // 添加自动补全逻辑
                        .executes(context -> {
                            ServerPlayer sender = context.getSource().getPlayerOrException();
                            String targetName = StringArgumentType.getString(context, "target");


                            // 如果目标是命名实体
                            Entity targetEntity = findNamedEntity(context.getSource().getServer().overworld(), targetName);
                            if (targetEntity != null) {

                                // 获取目标实体的位置和维度
                                ServerLevel targetLevel = (ServerLevel) targetEntity.level();
                                Vec3 targetPos = targetEntity.position();

                                // 传送玩家
                                boolean teleportSuccess = sender.teleportTo(
                                        targetLevel,
                                        targetPos.x, targetPos.y, targetPos.z,
                                        Set.of(),
                                        sender.getYRot(), sender.getXRot(),
                                        true
                                );

                                if (teleportSuccess) {
                                    // 添加粒子效果
                                    targetLevel.sendParticles(
                                            ParticleTypes.PORTAL,
                                            targetPos.x, targetPos.y, targetPos.z,
                                            100,
                                            0.5, 0.5, 0.5,
                                            1
                                    );

                                    sender.sendSystemMessage(Component.literal("已传送到实体 " + targetName));
                                } else {
                                    sender.sendSystemMessage(Component.literal("传送失败"));
                                }
                            } else {
                                sender.sendSystemMessage(Component.literal("目标不存在"));
                            }
                            return 0;

                        })));
    }

    // 查找命名实体（遍历所有维度）
    private static Entity findNamedEntity(ServerLevel level, String name) {
        for (ServerLevel serverLevel : level.getServer().getAllLevels()) {
            for (Entity entity : serverLevel.getAllEntities()) {
                if (entity.hasCustomName() && Objects.requireNonNull(entity.getCustomName()).getString().equalsIgnoreCase(name)) {
                    return entity;
                }
            }
        }
        return null;
    }

    // 提供自动补全建议
    private static CompletableFuture<Suggestions> suggestTargets(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        ServerLevel level = context.getSource().getLevel();
        List<String> targets = new ArrayList<>();

        // 添加被命名的实体名称（紫色）
        for (ServerLevel serverLevel : level.getServer().getAllLevels()) {
            serverLevel.getAllEntities().forEach(entity -> {
                if (entity.hasCustomName()) {
                    Component entityComponent = Component.literal(Objects.requireNonNull(entity.getCustomName()).getString())
                            .setStyle(Style.EMPTY.withColor(0xAA00FF)); // 紫色
                    targets.add(entityComponent.getString());
                }
            });
        }

        // 提供自动补全建议
        return SharedSuggestionProvider.suggest(targets, builder);
    }
}