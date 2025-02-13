package gr.ianic.kafka.serdes;


import gr.ianic.kafka.deserializers.JsonPojoDeserializer;
import gr.ianic.kafka.serializers.JsonPojoSerializer;
import gr.ianic.model.measurements.AmrMeasurement;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

import java.util.HashMap;
import java.util.Map;

public class CustomSerdes {

    private CustomSerdes() {
    }


    /**
     * Returns a Serde instance for serializing and deserializing {@link AmrMeasurement} objects using JSON.
     * <p>
     * The serializer and deserializer are configured to use {@link JsonPojoSerializer} and {@link JsonPojoDeserializer},
     * <p>
     * respectively.
     *
     * @return a Serde instance for serializing and deserializing {@link AmrMeasurement} objects using JSON.
     */
    public static Serde<AmrMeasurement> AmrSerde() {
        // Create a map to hold the serde properties.
        Map<String, Object> AmrserdeProps = new HashMap<>();
        // Add the class name of the POJO to the serde properties.
        AmrserdeProps.put("JsonPOJOClass", AmrMeasurement.class);
        // Create a JSON serializer for the AmrMeasurement class.
        Serializer<AmrMeasurement> AmrMeasurementSerializer = new JsonPojoSerializer<>();
        // Create a JSON deserializer for the AmrMeasurement class.
        Deserializer<AmrMeasurement> AmrMeasurementDeserializer = new JsonPojoDeserializer<>();
        // Configure the serializer with the serde properties.
        AmrMeasurementSerializer.configure(AmrserdeProps, false);
        // Configure the deserializer with the serde properties.
        AmrMeasurementDeserializer.configure(AmrserdeProps, false);

        // Create a Serde instance that uses the JSON serializer and deserializer for the AmrMeasurement class.
        return Serdes.serdeFrom(AmrMeasurementSerializer, AmrMeasurementDeserializer);
    }


}
