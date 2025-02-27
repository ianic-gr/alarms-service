package gr.ianic.kafkaStreams.serializers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

/**
 * A Kafka serializer for converting Java objects into JSON byte arrays.
 * This class uses Jackson's {@link ObjectMapper} to perform the serialization.
 *
 * @param <T> The type of the object to serialize.
 */
public class JsonPojoSerializer<T> implements Serializer<T> {

    private final ObjectMapper objectMapper = new ObjectMapper(); // Jackson ObjectMapper for JSON serialization

    /**
     * Default constructor required by Kafka.
     */
    public JsonPojoSerializer() {
    }

    /**
     * Configures the serializer. This implementation does nothing.
     *
     * @param props The configuration properties.
     * @param isKey Whether the serializer is used for a key or value.
     */
    @Override
    public void configure(Map<String, ?> props, boolean isKey) {
        // No configuration needed
    }

    /**
     * Serializes a Java object of type {@code T} into a JSON byte array.
     *
     * @param topic The topic associated with the data.
     * @param data  The object to serialize.
     * @return The serialized byte array, or {@code null} if the input object is {@code null}.
     * @throws SerializationException If serialization fails.
     */
    @Override
    public byte[] serialize(String topic, T data) {
        // Return null if the input object is null
        if (data == null) {
            return null;
        }

        try {
            // Serialize the object into a JSON byte array
            return objectMapper.writeValueAsBytes(data);
        } catch (Exception e) {
            // Wrap any exceptions in a SerializationException
            throw new SerializationException("Error serializing JSON message", e);
        }
    }

    /**
     * Closes the serializer. This implementation does nothing.
     */
    @Override
    public void close() {
        // No resources to close
    }
}