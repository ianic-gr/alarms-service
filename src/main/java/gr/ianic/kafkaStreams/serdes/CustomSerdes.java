package gr.ianic.kafkaStreams.serdes;

import gr.ianic.kafkaStreams.deserializers.JsonPojoDeserializer;
import gr.ianic.kafkaStreams.serializers.JsonPojoSerializer;
import gr.ianic.model.measurements.AmrMeasurement;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides custom Serde (Serializer/Deserializer) implementations for Kafka Streams.
 * This class includes Serdes for specific types, such as {@link AmrMeasurement}.
 */
public class CustomSerdes {

    /**
     * Private constructor to prevent instantiation.
     * This class is a utility class and should not be instantiated.
     */
    private CustomSerdes() {
    }

    /**
     * Returns a Serde instance for serializing and deserializing {@link AmrMeasurement} objects using JSON.
     * <p>
     * The serializer and deserializer are configured to use {@link JsonPojoSerializer} and {@link JsonPojoDeserializer},
     * respectively.
     *
     * @return A Serde instance for serializing and deserializing {@link AmrMeasurement} objects using JSON.
     */
    public static Serde<AmrMeasurement> AmrSerde() {
        // Create a map to hold the serde properties
        Map<String, Object> AmrserdeProps = new HashMap<>();
        // Add the class name of the POJO to the serde properties
        AmrserdeProps.put("JsonPOJOClass", AmrMeasurement.class);

        // Create a JSON serializer for the AmrMeasurement class
        Serializer<AmrMeasurement> AmrMeasurementSerializer = new JsonPojoSerializer<>();
        // Create a JSON deserializer for the AmrMeasurement class
        Deserializer<AmrMeasurement> AmrMeasurementDeserializer = new JsonPojoDeserializer<>();

        // Configure the serializer with the serde properties
        AmrMeasurementSerializer.configure(AmrserdeProps, false);
        // Configure the deserializer with the serde properties
        AmrMeasurementDeserializer.configure(AmrserdeProps, false);

        // Create a Serde instance that uses the JSON serializer and deserializer for the AmrMeasurement class
        return Serdes.serdeFrom(AmrMeasurementSerializer, AmrMeasurementDeserializer);
    }
}