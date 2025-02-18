package gr.ianic.kafkaStreams;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A factory class for managing Kafka Streams instances.
 * This class provides methods to create, start, and stop Kafka Streams for different sessions.
 * It uses a concurrent map to store and manage active Kafka Streams sessions.
 */
@ApplicationScoped
public class KafkaStreamsFactory {

    // A thread-safe map to store Kafka Streams instances, keyed by session ID.
    private final Map<String, KafkaStreams> streamsSessions = new ConcurrentHashMap<>();

    @Inject
    private KafkaStreamsFactory() {
        // Private constructor to enforce singleton pattern
    }

    /**
     * Creates and returns a new {@link StreamsBuilder} instance.
     * The {@link StreamsBuilder} is used to define the topology of the Kafka Streams application.
     *
     * @return A new {@link StreamsBuilder} instance.
     */
    public StreamsBuilder getBuilder() {
        return new StreamsBuilder();
    }

    /**
     * Starts a Kafka Stream for the given session ID using the provided {@link StreamsBuilder}.
     * The Kafka Stream is configured with default properties and added to the active sessions map.
     *
     * @param sessionId The unique identifier for the session.
     * @param builder   The {@link StreamsBuilder} containing the topology for the Kafka Stream.
     */
    public void startStream(String sessionId, StreamsBuilder builder) {
        try {
            Properties props = getProperties(sessionId);
            KafkaStreams kafkaStreams = new KafkaStreams(builder.build(), props);
            streamsSessions.put(sessionId, kafkaStreams);
            kafkaStreams.start();
            System.out.println("Started Kafka Stream for session: " + sessionId);
        } catch (Exception e) {
            System.err.println("Failed to start Kafka Stream for session: " + sessionId);
            e.printStackTrace();
        }
    }

    private static Properties getProperties(String sessionId) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "server1.ianic.gr:9094"); // Kafka broker address
        props.put("application.id", sessionId + "-stream-rules"); // Unique application ID for the stream
        props.put("default.key.serde", "org.apache.kafka.common.serialization.Serdes$StringSerde"); // Default key serializer/deserializer
        props.put("default.value.serde", "org.apache.kafka.common.serialization.Serdes$StringSerde"); // Default value serializer/deserializer
        return props;
    }

    /**
     * Stops the Kafka Stream associated with the given session ID.
     * If a stream exists for the session, it is closed and removed from the active sessions map.
     *
     * @param sessionId The unique identifier for the session.
     */
    public void stopStream(String sessionId) {
        // Retrieve and remove the KafkaStreams instance from the sessions map
        KafkaStreams streams = streamsSessions.remove(sessionId);

        // If the stream exists, close it
        if (streams != null) {
            streams.close();
            System.out.println("Stopped Kafka Stream for session: " + sessionId);
        }
    }

    @PreDestroy
    public void cleanup() {
        streamsSessions.forEach((sessionId, streams) -> {
            streams.close();
            System.out.println("Closed Kafka Stream for session: " + sessionId);
        });
        streamsSessions.clear();
    }
}