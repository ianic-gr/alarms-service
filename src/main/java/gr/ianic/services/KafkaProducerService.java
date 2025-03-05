package gr.ianic.services;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * A service for producing messages to Kafka topics.
 * This class is responsible for initializing a Kafka producer and sending messages to specified topics.
 */
@ApplicationScoped
public class KafkaProducerService {

    private Producer<String, String> producer; // Kafka producer instance

    /**
     * Default constructor.
     */
    public KafkaProducerService() {
    }

    /**
     * Initializes the Kafka producer with the necessary configuration.
     * This method is automatically called after the bean is constructed.
     */
    @PostConstruct
    public void init() {
        // Kafka producer configuration properties
        Properties props = new Properties();
        props.put("bootstrap.servers", "server1.ianic.gr:9094"); // Kafka broker address
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer"); // Key serializer
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer"); // Value serializer

        // Initialize the Kafka producer
        this.producer = new KafkaProducer<>(props);
    }

    /**
     * Sends a message to a specified Kafka topic.
     *
     * @param topic   The Kafka topic to which the message will be sent.
     * @param key     The key for the message.
     * @param message The message to be sent.
     */
    public void sendMessage(String topic, String key, String message) {
        // Create a ProducerRecord with the topic, key, and message
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, message);

        // Send the message to the Kafka topic
        producer.send(record);
    }
}