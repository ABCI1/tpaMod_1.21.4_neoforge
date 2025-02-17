package com.tpa.tpaMod.commands;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

public class TpaRequests {
    private static final Map<ServerPlayer, ServerPlayer> requests = new HashMap<>();

    // 添加传送请求
    public static void addRequest(ServerPlayer sender, ServerPlayer target) {
        requests.put(target, sender);
    }

    // 获取传送请求
    public static ServerPlayer getRequest(ServerPlayer target) {
        return requests.get(target);
    }

    // 移除传送请求
    public static void removeRequest(ServerPlayer target) {
        requests.remove(target);
    }
}