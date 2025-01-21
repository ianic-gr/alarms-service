package gr.ianic.kafka;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;


@ApplicationScoped
public class KafkaProducerService {

    @Channel("alarm-topic")
    Emitter<String> alarmEmitter;

    public void sendMessage(String message) {
        alarmEmitter.send(message);
    }
}