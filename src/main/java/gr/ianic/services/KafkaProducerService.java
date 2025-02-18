package gr.ianic.services;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

@ApplicationScoped
public class KafkaProducerService {

    private Producer<String, String> producer;

    public KafkaProducerService() {

    }

    @PostConstruct
    public void init() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "server1.ianic.gr:9094");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        this.producer = new KafkaProducer<>(props);
    }

    public void sendMessage(String topic, String key, String message) {
        producer.send(new ProducerRecord<>(topic, key, message));
    }
}
