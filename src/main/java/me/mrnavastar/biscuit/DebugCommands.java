package me.mrnavastar.biscuit;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import lombok.NoArgsConstructor;
import me.mrnavastar.biscuit.api.Biscuit;
import me.mrnavastar.biscuit.api.CookieJar;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class DebugCommands {

    @NoArgsConstructor
    public static class TestCookie {

        private String data;

        public TestCookie(String data) {
            this.data = data;
        }
    }

    public static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("setCookie").requires(ServerCommandSource::isExecutedByPlayer)
                        .then(CommandManager.argument("data", StringArgumentType.greedyString()).executes(context -> setCookieData(context, StringArgumentType.getString(context, "data"))))
        );

        dispatcher.register(
                CommandManager.literal("getCookie").requires(ServerCommandSource::isExecutedByPlayer)
                        .executes(DebugCommands::getCookieData)
        );

        Biscuit.register(TestCookie.class).finish();
    }

    private static int setCookieData(CommandContext<ServerCommandSource> context, String data) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ((CookieJar) player).setCookie(new TestCookie(data));
        return 0;
    }

    private static int getCookieData(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) return 0;

        player.getCookie(TestCookie.class).whenComplete((data, throwable) -> {

            System.out.println(data.data);

           player.sendMessage(Text.of(data.data));
        });

        return 0;
    }
}
