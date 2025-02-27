package gr.ianic.kafkaStreams.deserializers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

/**
 * A Kafka deserializer for converting JSON byte arrays into Java objects.
 * This class uses Jackson's {@link ObjectMapper} to perform the deserialization.
 *
 * @param <T> The type of the object to deserialize.
 */
public class JsonPojoDeserializer<T> implements Deserializer<T> {

    private final ObjectMapper objectMapper = new ObjectMapper(); // Jackson ObjectMapper for JSON deserialization

    private Class<T> tClass; // The class of the object to deserialize

    /**
     * Default constructor required by Kafka.
     */
    public JsonPojoDeserializer() {
    }

    /**
     * Configures the deserializer with the specified properties.
     *
     * @param props The configuration properties.
     * @param isKey Whether the deserializer is used for a key or value.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void configure(Map<String, ?> props, boolean isKey) {
        // Extract the class type from the configuration properties
        tClass = (Class<T>) props.get("JsonPOJOClass");
    }

    /**
     * Deserializes a JSON byte array into an object of type {@code T}.
     *
     * @param topic The topic associated with the data.
     * @param bytes The byte array to deserialize.
     * @return The deserialized object, or {@code null} if the input bytes are {@code null}.
     * @throws SerializationException If deserialization fails.
     */
    @Override
    public T deserialize(String topic, byte[] bytes) {
        // Return null if the input bytes are null
        if (bytes == null) {
            return null;
        }

        T data;
        try {
            // Deserialize the byte array into an object of type T
            data = objectMapper.readValue(bytes, tClass);
        } catch (Exception e) {
            // Wrap any exceptions in a SerializationException
            throw new SerializationException("Error deserializing JSON to " + tClass.getName(), e);
        }

        return data;
    }

    /**
     * Closes the deserializer. This implementation does nothing.
     */
    @Override
    public void close() {
        // No resources to close
    }
}