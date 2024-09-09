package dev.emortal.minestom.edge;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.ping.ResponseData;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

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

    private static final String FAVICON;

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
            FAVICON = "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
        }
    }

    public static void onServerPing(@NotNull ServerListPingEvent event) {
        ResponseData responseData = event.getResponseData();
        responseData.setDescription(createMessage());
        responseData.setFavicon(FAVICON);
    }

    private static @NotNull Component createMessage() {
        String randomMessage = selectRandomMessage();
        return Component.text()
                .append(PING_MOTD)
                .appendNewline()
                .append(Component.text(randomMessage, NamedTextColor.YELLOW))
                .build();
    }

    private static @NotNull String selectRandomMessage() {
        return MOTDS[ThreadLocalRandom.current().nextInt(MOTDS.length)];
    }
}
