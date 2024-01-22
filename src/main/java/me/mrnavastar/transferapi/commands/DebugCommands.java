package me.mrnavastar.transferapi.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.transfer.CookieRegistry;
import net.fabricmc.fabric.impl.transfer.CookieSigner;
import net.fabricmc.fabric.api.transfer.ServerCookieStore;
import net.fabricmc.fabric.api.transfer.ServerTransferable;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class DebugCommands {

    public static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("transfer").requires(ServerCommandSource::isExecutedByPlayer)
                        .then(CommandManager.argument("address", StringArgumentType.string()).then(CommandManager.argument("port", IntegerArgumentType.integer()).executes(context -> transfer(context, StringArgumentType.getString(context, "address"), IntegerArgumentType.getInteger(context, "port")))
        )));

        dispatcher.register(
                CommandManager.literal("setCookie").requires(ServerCommandSource::isExecutedByPlayer)
                        .then(CommandManager.argument("data", StringArgumentType.greedyString()).executes(context -> setCookieData(context, StringArgumentType.getString(context, "data"))))
        );

        dispatcher.register(
                CommandManager.literal("getCookie").requires(ServerCommandSource::isExecutedByPlayer)
                        .executes(DebugCommands::getCookieData)
        );

        CookieRegistry.register(new Identifier("multiplex:test")).setSecret("poggers dawg").setCustomMac(CookieSigner.DEFAULT_MAC).finish();
    }

    private static int transfer(CommandContext<ServerCommandSource> context, String address, int port) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ((ServerTransferable) player).transferToServer(address, port);
        return 0;
    }

    private static int setCookieData(CommandContext<ServerCommandSource> context, String data) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ((ServerCookieStore) player).setCookie(new Identifier("multiplex:test"), data.getBytes());
        return 0;
    }

    private static int getCookieData(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        player.sendMessage(Text.of(new String(((ServerCookieStore) player).getCookie(new Identifier("multiplex:test")))));
        return 0;
    }
}
