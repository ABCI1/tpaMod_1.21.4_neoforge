package com.tpa.tpaMod.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class TpDenyCommand {

    private static final int PERMISSION_LEVEL = 0; // 设置权限等级

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tpdeny")
                .requires(source -> source.hasPermission(PERMISSION_LEVEL))
                .executes(context -> {
                    ServerPlayer target = context.getSource().getPlayerOrException();
                    ServerPlayer sender = TpaRequests.getRequest(target);

                    if (sender == null) {
                        target.sendSystemMessage(Component.literal("没有传送请求"));
                        return 0;
                    }
                    // 拒绝请求
                    sender.sendSystemMessage(Component.literal(target.getName().getString() + " 拒绝传送"));
                    TpaRequests.removeRequest(target);

                    return 1;
                }));
    }
}
