package dev.emortal.minestom.edge;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import dev.emortal.api.message.matchmaker.MatchCreatedMessage;
import dev.emortal.api.model.matchmaker.Assignment;
import dev.emortal.api.model.matchmaker.Match;
import dev.emortal.api.model.matchmaker.Ticket;
import dev.emortal.api.service.matchmaker.MatchmakerService;
import dev.emortal.api.utils.GrpcStubCollection;
import io.grpc.StatusRuntimeException;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.common.CookieStorePacket;
import net.minestom.server.network.packet.server.common.TransferPacket;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public final class MinestomEdgeServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(MinestomEdgeServer.class);

    private static final String ADDRESS = "0.0.0.0";
    private static final int PORT = 25565;
    private static final NamespaceID COOKIE_NAME = NamespaceID.from("emortalmc", "proxy_route_token");
    private static final String KAFKA_HOST = System.getenv("KAFKA_HOST");
    private static final String KAFKA_PORT = System.getenv("KAFKA_PORT");

    private final MinecraftServer server;
    private final Instance instance;

    private final MessagingHandler messaging;
    private final MatchmakerService matchmaker;

    // pendingPlayers - a proxy has been requested and the configuration event thread is locked
    private final Cache<UUID, CountDownLatch> pendingPlayers = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .evictionListener(this::onEvict)
            .build();
    // pendingTransfers - a Proxy has been found, the configuration event thread is unlocked and data is retrieved from here
    private final Map<UUID, Assignment> pendingTransfers = new ConcurrentHashMap<>();

    private final AtomicLong globalPlayerCount = new AtomicLong(0);

    public MinestomEdgeServer() {
        this.server = MinecraftServer.init();
        this.instance = MinecraftServer.getInstanceManager().createInstanceContainer(IChunkLoader.noop());

        this.matchmaker = GrpcStubCollection.getMatchmakerService().orElse(null);
        if (matchmaker == null) {
            throw new RuntimeException("Matchmaker service unavailable. Shutting down...");
        }

        this.messaging = new MessagingHandler();

        MinecraftServer.getGlobalEventHandler().addListener(AsyncPlayerConfigurationEvent.class, event -> {
            event.setSpawningInstance(this.instance);

            CountDownLatch latch = new CountDownLatch(1);
            Player player = event.getPlayer();
            boolean reqSuccess = this.requestProxyMatch(player, latch);
            if (!reqSuccess) return;
            try {
                boolean latchSuccess = latch.await(10, TimeUnit.SECONDS);
                if (!latchSuccess) {
                    LOGGER.error("Player '{}' timed out waiting for proxy match", player.getUsername());
                    player.kick(ChatMessages.DISCONNECT_MATCHMAKER_ERR);
                    return;
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (!player.getPlayerConnection().isOnline()) return;

            Assignment foundProxy = this.pendingTransfers.remove(player.getUuid());
            if (foundProxy == null) {
                LOGGER.error("Failed to find proxy for player '{}'", player.getUsername());
                player.kick(ChatMessages.DISCONNECT_MATCHMAKER_ERR);
                return;
            }

            player.sendPacket(new CookieStorePacket(COOKIE_NAME, JWTUtils.generateJWT(player.getUsername(), foundProxy.getServerId()).getBytes(StandardCharsets.UTF_8)));

            LOGGER.info("Sending player '{}' to proxy '{}'", player.getUsername(), foundProxy.getServerId());
            player.sendPacket(new TransferPacket(foundProxy.getServerAddress(), foundProxy.getServerPort()));
        });

        new ServerPingListener();

        this.messaging.addListener(MatchCreatedMessage.class, this::handleMatchCreated);

        AgonesHandler agonesHandler = new AgonesHandler();
        MinecraftServer.getSchedulerManager().buildShutdownTask(agonesHandler::shutdown);

        this.server.start(ADDRESS, PORT);
    }

    @Blocking
    private boolean requestProxyMatch(@NotNull Player player, @NotNull CountDownLatch latch) {
        try {
            this.matchmaker.loginQueue(player.getUuid(), true);
            this.pendingPlayers.put(player.getUuid(), latch);

            return true;
        } catch (StatusRuntimeException ex) {
            LOGGER.error("Failed to queue player '{}' for initial lobby", player.getUuid(), ex);
            player.kick(ChatMessages.DISCONNECT_MATCHMAKER_ERR);
            return false;
        }
    }

    private void onEvict(@Nullable UUID playerId, @Nullable CountDownLatch latch, @NotNull RemovalCause cause) {
        if (cause != RemovalCause.EXPIRED) return;
        if (playerId == null || latch == null) return;

        LOGGER.warn("Evicting player by ID '{}' from pending players", playerId);
        latch.countDown();
    }

    private void handleMatchCreated(@NotNull MatchCreatedMessage message) {
        Match match = message.getMatch();
        Assignment assignment = match.getAssignment();
        if (!match.getGameModeId().equals("proxy")) return;

        for (Ticket ticket : match.getTicketsList()) {
            if (ticket.getAutoTeleport()) continue;
            if (ticket.getPlayerIdsList().size() != 1) continue;

            UUID playerId = UUID.fromString(ticket.getPlayerIds(0));

            this.pendingTransfers.put(playerId, assignment);

            CountDownLatch latch = this.pendingPlayers.getIfPresent(playerId);
            if (latch == null) continue;

            this.pendingPlayers.invalidate(playerId);
            latch.countDown();
        }
    }
}