package dev.emortal.minestom.edge;

import dev.emortal.api.service.playertracker.PlayerTrackerService;
import dev.emortal.api.utils.GrpcStubCollection;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.ping.Status;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

final class ServerPingListener {
    private static final String[] MOTDS = new String[] {
            "coolest server to ever exist",
            "better than hypixel",
            "you should join",
            "stop scrolling, click here!",
            "Lunar client users: Beware!",
            "using 3 server softwares!",
            "gradient lover",
            "emortal is watching",
            "emortal says 2 + 2 = 5",
            "Chuck Norris joined and said it was pretty good",
            "Chuck Norris doesn't join, the server joins him",
            "private lobbies when?",
            "I heard SunriseMC was releasing soon...",
            "This server is certified aladeen!",
    };

    private static final Component PING_MOTD = Component.text()
            .append(Component.text("▓▒░              ", NamedTextColor.LIGHT_PURPLE))
            .append(Component.text("⚡   ", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD))
            .append(MiniMessage.miniMessage().deserialize("<gradient:gold:light_purple><bold>EmortalMC</bold></gradient>"))
            .append(Component.text("   ⚡", NamedTextColor.GOLD, TextDecoration.BOLD))
            .append(Component.text("              ░▒▓", NamedTextColor.GOLD))
            .build();

    private static final int MAX_PLAYERS = 3000;

    private static final byte[] FAVICON;

    static {
        InputStream inputStream = ServerListPingEvent.class.getResourceAsStream("/server-icon.png");
        byte[] bytes;
        try {
            bytes = inputStream.readAllBytes();
        } catch (Exception e) {
            bytes = null;
        }

        if (bytes == null) {
            FAVICON = null;
        } else {
            FAVICON = bytes;
        }
    }

    private final AtomicInteger globalPlayerCount = new AtomicInteger(0);

    public ServerPingListener() {
        this.startPlayerCountUpdates();
    }

    private void startPlayerCountUpdates() {
        PlayerTrackerService playerTracker = GrpcStubCollection.getPlayerTrackerService().orElse(null);
        if (playerTracker == null) return;

        MinecraftServer.getSchedulerManager().buildTask(() -> {
                    this.globalPlayerCount.set((int) playerTracker.getGlobalPlayerCount());
                }).delay(1, TimeUnit.CLIENT_TICK)
                .repeat(40, TimeUnit.CLIENT_TICK)
                .schedule();

        MinecraftServer.getGlobalEventHandler().addListener(ServerListPingEvent.class, this::onServerPing);
    }

    public void onServerPing(@NotNull ServerListPingEvent event) {
        event.setStatus(
                new Status(
                        createMessage(),
                        FAVICON,
                        Status.VersionInfo.DEFAULT,
                        new Status.PlayerInfo(this.globalPlayerCount.get(), MAX_PLAYERS),
                        false
                )
        );
    }

    private @NotNull Component createMessage() {
        String randomMessage = this.selectRandomMessage();
        return Component.text()
                .append(PING_MOTD)
                .appendNewline()
                .append(Component.text(randomMessage, NamedTextColor.YELLOW))
                .build();
    }

    private @NotNull String selectRandomMessage() {
        return MOTDS[ThreadLocalRandom.current().nextInt(MOTDS.length)];
    }
}
