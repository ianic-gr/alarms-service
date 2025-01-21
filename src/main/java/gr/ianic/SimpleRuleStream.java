package gr.ianic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.ianic.model.AmrMeasurement;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;

@ApplicationScoped
public class SimpleRuleStream {
    private KieSession kieSession;
    private final static ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @PostConstruct
    public void init() {
        KieServices kieServices = KieServices.Factory.get();

        // Create and configure KieBase
        KieBaseConfiguration config = kieServices.newKieBaseConfiguration();
        config.setOption(EventProcessingOption.STREAM);

        KieHelper kieHelper = new KieHelper();
        kieHelper.addResource(kieServices.getResources()
                .newClassPathResource("rules/alarm-rules.drl"), ResourceType.DRL);

        // Build KieBase with configuration
        KieBase kieBase = kieHelper.build(config);

        // Create session
        kieSession = kieBase.newKieSession();

        startRuleEngine();
    }

    private void startRuleEngine() {
        new Thread(kieSession::fireUntilHalt).start();
        System.out.println("Drools rule engine started...");
    }

    @Incoming("measurements")
    public void consumeAlarmMessage(String message) {
        AmrMeasurement event = parseMessage(message);
        kieSession.getEntryPoint("AlarmStream").insert(event);
    }

    private AmrMeasurement parseMessage(String message) {
        try {
            return mapper.readValue(message, AmrMeasurement.class);
        } catch (JsonProcessingException e) {
            return new AmrMeasurement();
        }
    }

    @PreDestroy
    public void cleanup() {
        if (kieSession != null) {
            kieSession.halt();
            kieSession.dispose();
            System.out.println("Drools rule engine stopped.");
        }
    }
}
