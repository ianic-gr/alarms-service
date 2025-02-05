package gr.ianic.kafka;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;


@ApplicationScoped
public class KafkaProducerService {

    @Channel("alarm-topic")
    @Inject
    Emitter<String> alarmEmitter;

    public void sendMessage(String message) {
        alarmEmitter.send(message);
    }
}