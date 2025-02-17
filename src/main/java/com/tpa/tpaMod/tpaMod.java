package com.tpa.tpaMod;

import com.tpa.tpaMod.commands.TpAcceptCommand;
import com.tpa.tpaMod.commands.TpDenyCommand;
import com.tpa.tpaMod.commands.TpaCommand;
import com.tpa.tpaMod.commands.TpaHereCommand;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;


@Mod(tpaMod.MODID)
public class tpaMod {
    public static final String MODID = "tpamod";

    public tpaMod(IEventBus modEventBus) {
        // 注册 Mod 事件
        modEventBus.addListener(this::commonSetup);

        // 注册服务器事件
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        NeoForge.EVENT_BUS.addListener(this::onServerStopping);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // 初始化代码
        System.out.println("TpaMod common setup complete!");
    }

    private void onServerStarting(ServerStartingEvent event) {
        // 服务器启动时执行的代码
        System.out.println("Server starting!");
    }

    private void onServerStopping(ServerStoppingEvent event) {
        // 服务器关闭时执行的代码
        System.out.println("Server stopping!");
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        // 注册命令
        TpaCommand.register(event.getDispatcher());
        TpaHereCommand.register(event.getDispatcher());
        TpAcceptCommand.register(event.getDispatcher());
        TpDenyCommand.register(event.getDispatcher());
    }
}