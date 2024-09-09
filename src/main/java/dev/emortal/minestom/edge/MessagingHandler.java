package dev.emortal.minestom.edge;

import com.google.protobuf.AbstractMessage;
import dev.emortal.api.utils.kafka.FriendlyKafkaConsumer;
import dev.emortal.api.utils.kafka.KafkaSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public final class MessagingHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessagingHandler.class);

    private static final String KAFKA_HOST = System.getenv("KAFKA_HOST");
    private static final String KAFKA_PORT = System.getenv("KAFKA_PORT");

    private final @Nullable FriendlyKafkaConsumer kafkaConsumer;

    public <T extends AbstractMessage> void addListener(@NotNull Class<T> messageType, @NotNull Consumer<T> listener) {
        if (this.kafkaConsumer != null) this.kafkaConsumer.addListener(messageType, listener);
    }

    public MessagingHandler() {
        KafkaSettings settings = KafkaSettings.builder().bootstrapServers(KAFKA_HOST + ":" + KAFKA_PORT).build();
        this.kafkaConsumer = new FriendlyKafkaConsumer(settings);
    }

    public void onUnload() {
        if (this.kafkaConsumer != null) this.kafkaConsumer.close();
    }
}
