package gr.ianic.kafka.serdes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.ianic.model.measurements.AmrMeasurement;

public class AmrSerializer implements org.apache.kafka.common.serialization.Serializer<AmrMeasurement> {
    private final static ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public byte[] serialize(String topic, AmrMeasurement amrMeasurement) {
        try {
            return mapper.writeValueAsBytes(amrMeasurement);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
