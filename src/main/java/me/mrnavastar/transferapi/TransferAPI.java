package me.mrnavastar.transferapi;

import lombok.Getter;
import me.mrnavastar.transferapi.commands.DebugCommands;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.List;

public class TransferAPI implements ModInitializer {

    @Getter
    private static boolean transferOnly = false;
    private static final HashSet<Identifier> registeredCookieIds = new HashSet<>();

    @Override
    public void onInitialize() {
        //if (FabricLoader.getInstance().isDevelopmentEnvironment())
            ServerLifecycleEvents.SERVER_STARTED.register(server -> DebugCommands.init(server.getCommandManager().getDispatcher()));
    }

    public static void registerCookie(Identifier identifier) {
        registeredCookieIds.add(identifier);
    }

    public static List<Identifier> getRegisteredCookies() {
        return registeredCookieIds.stream().toList();
    }
}