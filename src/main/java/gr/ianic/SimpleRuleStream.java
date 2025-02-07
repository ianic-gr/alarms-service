package gr.ianic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.ianic.kafka.KafkaProducerService;
import gr.ianic.model.measurements.AmrMeasurement;
import gr.ianic.model.rules.Rule;
import gr.ianic.repositories.RulesDao;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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

    @Inject
    RulesDao rulesDao;

    private KieSession kieSession;

    @Inject
    KafkaProducerService kafkaProducerService;

    private KieBaseConfiguration config;
    private KieBase kieBase;


    private final static ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


    public void reloadRules(String tenant) {
        System.out.println("Reloading rules for " + tenant);
        Rule rule = rulesDao.getByTenantAndType(tenant, "stream");

        System.out.println("Loading rule: " + rule);
        KieHelper kieHelper = new KieHelper();
        kieHelper.addContent(rule.getRule(), ResourceType.DRL);

        // Dispose the old session
        if (kieSession != null) {
            kieSession.halt();
            kieSession.dispose();
        }

        // Build KieBase with configuration
        kieBase = kieHelper.build(config);

        // Create session
        kieSession = kieBase.newKieSession();

        startRuleEngine();
    }

    @PostConstruct
    public void init() {
        Rule rule = rulesDao.getByTenantAndType("testTenant", "stream");

        System.out.println(rule.getRule());

        KieServices kieServices = KieServices.Factory.get();

        config = kieServices.newKieBaseConfiguration();
        config.setOption(EventProcessingOption.STREAM);

        // Create and configure KieBase


        KieHelper kieHelper = new KieHelper();
        /*
        kieHelper.addResource(kieServices.getResources()
                .newClassPathResource("rules/alarm-rules.drl"), ResourceType.DRL);
        */

        kieHelper.addContent(rule.getRule(), ResourceType.DRL);

        // Build KieBase with configuration
        kieBase = kieHelper.build(config);

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
        AmrMeasurement m;
        try {
            m = mapper.readValue(message, AmrMeasurement.class);
            kieSession.getEntryPoint("AlarmStream").insert(m);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
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
