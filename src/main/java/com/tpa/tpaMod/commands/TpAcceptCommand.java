package com.tpa.tpaMod.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;

import java.util.Set;

public class TpAcceptCommand {

    private static final int PERMISSION_LEVEL = 0; // 设置权限等级

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tpaccept")
                .requires(source -> source.hasPermission(PERMISSION_LEVEL))
                .executes(context -> {
                    ServerPlayer target = context.getSource().getPlayerOrException();
                    ServerPlayer sender = TpaRequests.getRequest(target);

                    if (sender == null) {
                        target.sendSystemMessage(Component.literal("没有传送请求"));
                        return 0;
                    }

                    // 获取目标玩家的维度和位置
                    ServerLevel targetLevel = target.serverLevel();
                    Vec3 targetPos = target.position();

                    // 传送玩家
                    sender.teleportTo(
                            targetLevel, // 目标维度
                            targetPos.x, targetPos.y, targetPos.z, // 目标位置
                            Set.of(), // 不保留任何状态
                            sender.getYRot(), sender.getXRot(), // 保持玩家的旋转角度
                            true // 设置摄像机视角
                    );

                    // 添加粒子效果
                    targetLevel.sendParticles(ParticleTypes.PORTAL, targetPos.x, targetPos.y, targetPos.z, 100, 0.5, 0.5, 0.5, 1);

                    // 移除请求
                    TpaRequests.removeRequest(target);

                    target.sendSystemMessage(Component.literal("接受传送"));
                    sender.sendSystemMessage(Component.literal("你已被传送到 " + target.getName().getString()));
                    return 1;
                }));
    }
}

