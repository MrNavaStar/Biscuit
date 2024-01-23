package me.mrnavastar.cookiejar;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import me.mrnavastar.cookiejar.api.Cookie;
import me.mrnavastar.cookiejar.api.CookieJar;
import me.mrnavastar.cookiejar.api.ServerCookieJar;
import me.mrnavastar.cookiejar.util.BufUtils;
import net.fabricmc.fabric.api.networking.v1.ServerTransferable;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class DebugCommands {

    @NoArgsConstructor
    public static class TestCookie implements Cookie {

        private String data;

        public TestCookie(String data) {
            this.data = data;
        }

        @Override
        public void encode(ByteBuf buf) throws Exception {
            BufUtils.writeString(buf, data);
        }

        @Override
        public void decode(ByteBuf buf) throws Exception {
            data = BufUtils.readString(buf);
        }
    }

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

        CookieJar.register(new Identifier("cookiejar:test"), TestCookie.class).setSecret("very epic").finish();
    }

    private static int transfer(CommandContext<ServerCommandSource> context, String address, int port) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ((ServerTransferable) player.networkHandler).transferToServer(address, port);
        return 0;
    }

    private static int setCookieData(CommandContext<ServerCommandSource> context, String data) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ((ServerCookieJar) player).setCookie(new TestCookie(data));
        return 0;
    }

    private static int getCookieData(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) return 0;

        player.getCookie(TestCookie.class).whenComplete((data, throwable) -> {
           player.sendMessage(Text.of(data.data));
        });

        return 0;
    }
}
