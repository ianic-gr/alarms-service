package gr.ianic.kafka.serdes;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.ianic.model.measurements.AmrMeasurement;
import org.apache.kafka.common.serialization.Deserializer;
import org.jboss.logmanager.Level;

import java.io.IOException;
import java.util.logging.Logger;

public class AmrDeserializer implements Deserializer<AmrMeasurement> {

    private final static ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final Logger LOGGER = Logger.getLogger(AmrDeserializer.class.getName());

    @Override
    public AmrMeasurement deserialize(String topic, byte[] data) {
        return parseMessage(data);
    }

    private AmrMeasurement parseMessage(byte[] message) {
        try {
            return mapper.readValue(message, AmrMeasurement.class);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to parse message", e);
            return new AmrMeasurement();
        }
    }
}

