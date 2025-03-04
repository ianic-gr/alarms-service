package gr.ianic.rules;

import gr.ianic.kafkaStreams.KafkaStreamsFactory;
import gr.ianic.kafkaStreams.serdes.CustomSerdes;
import gr.ianic.model.WaterMeter;
import gr.ianic.model.rules.Rule;
import gr.ianic.repositories.daos.RulesDao;
import gr.ianic.services.KafkaProducerService;
import gr.ianic.services.WaterMeterService;
import jakarta.annotation.PreDestroy;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;

import java.util.List;
import java.util.Set;

/**
 * A session class for managing stream-based rule evaluation using Drools.
 * This class initializes and manages a Drools session for processing measurements in real-time.
 */
public class StreamSession extends Session {


    private final String tenant; // The tenant identifier for this session
    private final String sessionId;
    private final KieBaseConfiguration config; // Configuration for the Drools KieBase
    protected KafkaProducerService kafkaProducerService;
    protected KafkaStreamsFactory kafkaStreamsFactory;
    protected WaterMeterService waterMeterService;
    protected RulesDao rulesDao;
    private Set<String> entryPoints; // The entryPoints identifier for this session
    private KieSession kieSession; // Drools session for rule evaluation
    private KieBase kieBase; // Drools KieBase containing the rules
    private KieHelper kieHelper;
    private List<Rule> rules; // The rule associated with this session


    /**
     * Parameterized constructor for creating a session with a specific tenant and entryPoints.
     *
     * @param tenant      The tenant identifier for the session.
     * @param entryPoints The entryPoints identifier for the session.
     */
    protected StreamSession(String tenant, Set<String> entryPoints, List<Rule> rules) {
        this.tenant = tenant;
        this.entryPoints = entryPoints;
        this.sessionId = tenant;
        this.rules = rules;

        // Drools services for creating configurations and sessions
        KieServices kieServices = KieServices.Factory.get();
        config = kieServices.newKieBaseConfiguration();

        this.init();
    }

    /**
     * Reloads the rules for this session.
     * This method fetches the latest rules from the database and reinitializes the Drools session.
     */
    protected void reloadRules(Set<String> entryPoints, List<Rule> rules) {
        System.out.println("Reloading rules for " + sessionId);

        this.rules = rules;
        this.entryPoints = entryPoints;

        for (Rule rule : rules) {
            System.out.println("Loading rule: " + rule);
            kieHelper.addContent(rule.getDrl(), ResourceType.DRL);
        }

        // Dispose the old session
        stopRulesEngine();

        // Build KieBase with configuration
        kieBase = kieHelper.build(config);

        // Create a new session
        kieSession = kieBase.newKieSession();

        startRulesEngine();
    }

    /**
     * Initializes the session by loading rules, configuring Drools, and starting the rule engine.
     */
    @Override
    protected void init() {
        kieHelper = new KieHelper();
        for (Rule rule : rules) {
            System.out.println("Loading rule: " + rule);
            kieHelper.addContent(rule.getDrl(), ResourceType.DRL);
        }

        config.setOption(EventProcessingOption.STREAM); // Configure Drools for event processing
        kieBase = kieHelper.build(config); // Build the KieBase
        kieSession = kieBase.newKieSession(); // Create a new session
        kieSession.setGlobal("kafkaProducer", kafkaProducerService);

    }


    private void consumeEventFacts() {
        StreamsBuilder builder = kafkaStreamsFactory.getNewBuilder();

        entryPoints.forEach((entryPoint) ->
                builder.stream(entryPoint + "-" + tenant, Consumed.with(Serdes.String(), CustomSerdes.AmrSerde()))
                        .foreach((k, m) -> {
                            if (kieSession.getEntryPoint(entryPoint) == null)
                                System.out.println("There is no entrypoint with name: " + entryPoint);
                            else
                                kieSession.getEntryPoint(entryPoint).insert(m);
                        }));


        kafkaStreamsFactory.startStream(sessionId, builder);
    }

    private void loadEntitiesFacts() {
        // Insert initial data (water meters) into the session
        List<WaterMeter> meters = getMeters(tenant);
        for (WaterMeter meter : meters) {
            kieSession.getEntryPoint("metersEntry").insert(meter);
        }
    }


    /**
     * Starts the Drools rule engine in a separate thread.
     */
    @Override
    protected void startRulesEngine() {
        //kieSession.setGlobal("kafkaProducer", kafkaProducerService);
        loadEntitiesFacts();

        // Add an event listener to capture rule firings
       /*kieSession.addEventListener(new DefaultAgendaEventListener() {
            @Override
            public void afterMatchFired(AfterMatchFiredEvent event) {
                // Check if an Alarm fact was inserted
                kieSession.getObjects(o -> o instanceof Alarm).forEach(obj -> {
                    Alarm message = (Alarm) obj;
                    kafkaProducerService.sendMessage(message.getTopic(), message.getKey(), message.getMessage());
                    kieSession.delete(kieSession.getFactHandle(message)); // Remove fact after sending
                });
            }
        });*/

        new Thread(kieSession::fireUntilHalt).start();
        consumeEventFacts();
        System.out.println("Drools rule engine started...");
    }

    /**
     * Stops the Drools rule engine and disposes of the session.
     */
    @Override
    @PreDestroy
    protected void stopRulesEngine() {
        if (kieSession != null) {
            kieSession.halt();
            kieSession.dispose();
            kafkaStreamsFactory.stopStream(sessionId);
            System.out.println("Drools rule engine stopped.");
        }
    }


    /**
     * Fetches the water meters for the given tenant.
     *
     * @param tenant The tenant identifier.
     * @return A list of water meters associated with the tenant.
     */
    public List<WaterMeter> getMeters(String tenant) {
        return waterMeterService.getWaterMetersByTenant(tenant).await().indefinitely();
    }
}