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

/**
 * A session class for managing stream-based rule evaluation using Drools.
 * This class initializes and manages a Drools session for processing measurements in real-time.
 */
public class StreamSession extends Session {


    protected KafkaProducerService kafkaProducerService;
    protected KafkaStreamsFactory kafkaStreamsFactory;
    protected WaterMeterService waterMeterService;
    protected RulesDao rulesDao;

    private KieSession kieSession; // Drools session for rule evaluation
    private KieBaseConfiguration config; // Configuration for the Drools KieBase
    private KieBase kieBase; // Drools KieBase containing the rules
    private KieServices kieServices; // Drools services for creating configurations and sessions
    private KieHelper kieHelper;

    private List<Rule> rules; // The rule associated with this session
    private final String tenant; // The tenant identifier for this session
    private final String source; // The source identifier for this session
    private final String sessionId;


    /**
     * Parameterized constructor for creating a session with a specific tenant and source.
     *
     * @param tenant The tenant identifier for the session.
     * @param source The source identifier for the session.
     */
    protected StreamSession(String tenant, String source, List<Rule> rules) {
        this.tenant = tenant;
        this.source = source;
        this.sessionId = source + "-" + tenant;
        this.rules = rules;

        kieServices = KieServices.Factory.get();
        config = kieServices.newKieBaseConfiguration();

        this.init();
    }

    /**
     * Reloads the rules for this session.
     * This method fetches the latest rules from the database and reinitializes the Drools session.
     */
    public void reloadRules() {
        System.out.println("Reloading rules for " + sessionId);

        this.loadRules();

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
            kieHelper.addContent(rule.getRule(), ResourceType.DRL);
        }

        config.setOption(EventProcessingOption.STREAM); // Configure Drools for event processing
        kieBase = kieHelper.build(config); // Build the KieBase
        kieSession = kieBase.newKieSession(); // Create a new session
        kieSession.setGlobal("kafkaProducer", kafkaProducerService);

    }


    private void consumeEventFacts() {
        StreamsBuilder builder = kafkaStreamsFactory.getBuilder();

        builder.stream(source + "-" + tenant, Consumed.with(Serdes.String(), CustomSerdes.AmrSerde()))
                .foreach((k, m) -> {
                    if (kieSession.getEntryPoint(source) == null)
                        System.out.println("There is no entrypoint with name: " + source);
                    else
                        kieSession.getEntryPoint(source).insert(m);
                });

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
     * Loads the rules for this session from the database.
     */
    @Override
    protected void loadRules() {
        rules = getRules(tenant, "stream", source);
        kieHelper = new KieHelper();
        for (Rule rule : rules) {
            System.out.println("Loading rule: " + rule);
            kieHelper.addContent(rule.getRule(), ResourceType.DRL);
        }
    }

    /**
     * Starts the Drools rule engine in a separate thread.
     */
    @Override
    protected void startRulesEngine() {
        kieSession.setGlobal("kafkaProducer", kafkaProducerService);
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
     * Fetches the rules for the given tenant and type.
     *
     * @param tenant The tenant identifier.
     * @param type   The type of rules to fetch (e.g., "stream").
     * @return The rules associated with the tenant and type.
     */
    public List<Rule> getRules(String tenant, String type, String source) {
        return rulesDao.getByTenantTypeAndSource(tenant, type, source).all();
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