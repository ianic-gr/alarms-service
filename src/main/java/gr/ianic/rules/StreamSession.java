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
import org.kie.api.runtime.rule.EntryPoint;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.internal.utils.KieHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a session for processing rules in a streaming environment.
 * This class extends the {@link Session} class and provides functionality
 * to manage Drools rule sessions, Kafka Streams, and water meter data.
 */
public class StreamSession extends Session {

    private final String tenant; // The tenant identifier for this session
    private final String sessionId; // Unique identifier for the session
    private final KieBaseConfiguration config; // Configuration for the Drools KieBase
    protected KafkaProducerService kafkaProducerService; // Service for producing Kafka messages
    protected KafkaStreamsFactory kafkaStreamsFactory; // Factory for creating Kafka Streams
    protected WaterMeterService waterMeterService; // Service for fetching water meter data
    protected RulesDao rulesDao; // Data Access Object for rules
    private Set<String> entryPoints; // Entry points for the session
    private KieSession kieSession; // Drools session for rule evaluation
    private KieBase kieBase; // Drools KieBase containing the rules
    private KieHelper kieHelper; // Helper for building KieBase
    private List<Rule> rules; // List of rules associated with this session

    /**
     * Constructs a new StreamSession for the specified tenant, entry points, and rules.
     *
     * @param tenant      The tenant identifier.
     * @param entryPoints The entry points for the session.
     * @param rules       The list of rules to be applied.
     */
    protected StreamSession(String tenant, Set<String> entryPoints, List<Rule> rules) {
        this.tenant = tenant;
        this.entryPoints = entryPoints;
        this.sessionId = tenant;
        this.rules = rules;

        // Initialize Drools services and configuration
        KieServices kieServices = KieServices.Factory.get();
        config = kieServices.newKieBaseConfiguration();

        this.init();
    }

    /**
     * Reloads the rules for this session with new entry points and rules.
     *
     * @param entryPoints The new entry points for the session.
     * @param rules       The new list of rules to be applied.
     */
    protected void reloadRules(Set<String> entryPoints, List<Rule> rules) {
        System.out.println("Reloading rules for " + sessionId);

        this.rules = rules;
        this.entryPoints = entryPoints;

        // Add new rules to the KieHelper
        for (Rule rule : rules) {
            System.out.println("Loading rule: " + rule);
            kieHelper.addContent(rule.getDrl(), ResourceType.DRL);
        }

        // Dispose the old session
        stopRulesEngine();

        // Build KieBase with the new configuration
        kieBase = kieHelper.build(config);

        // Create a new session
        kieSession = kieBase.newKieSession();

        // Start the rules engine with the new rules
        startRulesEngine();
    }

    /**
     * Initializes the session by setting up the KieHelper, KieBase, and KieSession.
     */
    @Override
    protected void init() {
        kieHelper = new KieHelper();
        // Add rules to the KieHelper
        for (Rule rule : rules) {
            System.out.println("Loading rule: " + rule);
            kieHelper.addContent(rule.getDrl(), ResourceType.DRL);
        }

        // Configure Drools for event processing
        config.setOption(EventProcessingOption.STREAM);

        // Build the KieBase
        kieBase = kieHelper.build(config);

        // Create a new KieSession
        kieSession = kieBase.newKieSession();

        // Set global Kafka producer for use in rules
        kieSession.setGlobal("kafkaProducer", kafkaProducerService);
    }

    /**
     * Consumes event facts from Kafka topics and inserts them into the Drools session.
     */
    private void consumeEventFacts() {
        StreamsBuilder builder = kafkaStreamsFactory.getNewBuilder();

        // For each entry point, create a Kafka stream and insert facts into the session
        entryPoints.forEach((entryPoint) ->
        {
            EntryPoint kieSessionEntryPoint = kieSession.getEntryPoint(entryPoint);
            if (kieSessionEntryPoint != null) {
                builder.stream(entryPoint + "-" + tenant, Consumed.with(Serdes.String(), CustomSerdes.AmrSerde()))
                        .foreach((k, m) -> kieSession.getEntryPoint(entryPoint).insert(m));
            } else
                System.out.println("No entry point '" + entryPoint + "' found for tenant '" + tenant + "'");
        });

        // Start the Kafka stream
        kafkaStreamsFactory.startStream(sessionId, builder);
    }

    private final Map<String, FactHandle> factHandlesMap = new HashMap<>();

    /**
     * Loads initial water meter facts into the Drools session.
     */
    private void loadEntitiesFacts() {
        // Fetch water meters for the tenant
        List<WaterMeter> meters = getMeters(tenant);

        // Insert each water meter into the session and store its FactHandle
        for (WaterMeter meter : meters) {
            FactHandle factHandle = kieSession.getEntryPoint("metersEntry").insert(meter);
            factHandlesMap.put(meter.getCode(), factHandle); // Assuming WaterMeter has a unique ID
        }
    }

    protected void updateWaterMeter(WaterMeter updatedMeter) {
        // Retrieve the FactHandle for the specific WaterMeter
        FactHandle factHandle = factHandlesMap.get(updatedMeter.getCode());

        if (factHandle != null) {
            // Update the fact in the session
            kieSession.getEntryPoint("metersEntry").update(factHandle, updatedMeter);
        } else {
            // Handle the case where the fact is not found (e.g., insert it)
            factHandle = kieSession.getEntryPoint("metersEntry").insert(updatedMeter);
            factHandlesMap.put(updatedMeter.getCode(), factHandle);
        }
    }

    protected void deleteWaterMeter(WaterMeter updatedMeter) {
        // Retrieve the FactHandle for the specific WaterMeter
        FactHandle factHandle = factHandlesMap.get(updatedMeter.getCode());

        if (factHandle != null) {
            // Update the fact in the session
            kieSession.getEntryPoint("metersEntry").delete(factHandle);
        } else {
            // Handle the case where the fact is not found (e.g., insert it)
            factHandle = kieSession.getEntryPoint("metersEntry").insert(updatedMeter);
            factHandlesMap.put(updatedMeter.getCode(), factHandle);
        }
    }

    /**
     * Starts the Drools rule engine and consumes event facts.
     */
    @Override
    protected void startRulesEngine() {
        // Load initial water meter facts
        loadEntitiesFacts();

        // Start the Drools session in a new thread
        new Thread(kieSession::fireUntilHalt).start();

        // Consume event facts from Kafka
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
     * Retrieves water meters for the specified tenant.
     *
     * @param tenant The tenant identifier.
     * @return A list of water meters for the tenant.
     */
    public List<WaterMeter> getMeters(String tenant) {
        return waterMeterService.getWaterMetersByTenant(tenant).await().indefinitely();
    }
}