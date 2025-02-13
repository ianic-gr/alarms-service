package gr.ianic.rules;

import com.fasterxml.jackson.core.JsonProcessingException;
import gr.ianic.model.WaterMeter;
import gr.ianic.model.measurements.AmrMeasurement;
import gr.ianic.model.rules.Rule;
import jakarta.annotation.PreDestroy;
import org.eclipse.microprofile.reactive.messaging.Incoming;
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

    private KieSession kieSession; // Drools session for rule evaluation
    private KieBaseConfiguration config; // Configuration for the Drools KieBase
    private KieBase kieBase; // Drools KieBase containing the rules
    private KieServices kieServices; // Drools services for creating configurations and sessions

    private Rule rule; // The rule associated with this session
    private String tenant; // The tenant identifier for this session
    private String source; // The source identifier for this session

    /**
     * Default constructor.
     */
    public StreamSession() {
    }

    /**
     * Parameterized constructor for creating a session with a specific tenant and source.
     *
     * @param tenant The tenant identifier for the session.
     * @param source The source identifier for the session.
     */
    public StreamSession(String tenant, String source) {
        this.tenant = tenant;
        this.source = source;
    }

    /**
     * Reloads the rules for this session.
     * This method fetches the latest rules from the database and reinitializes the Drools session.
     */
    public void reloadRules() {
        System.out.println("Reloading rules for " + tenant);
        rule = rulesDao.getByTenantAndType(tenant, "stream");

        System.out.println("Loading rule: " + rule);
        KieHelper kieHelper = new KieHelper();
        kieHelper.addContent(rule.getRule(), ResourceType.DRL);

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
        loadRules(); // Load the rules for the session
        kieServices = KieServices.Factory.get();

        config = kieServices.newKieBaseConfiguration();
        config.setOption(EventProcessingOption.STREAM); // Configure Drools for event processing

        KieHelper kieHelper = new KieHelper();
        kieHelper.addContent(rule.getRule(), ResourceType.DRL);

        kieBase = kieHelper.build(config); // Build the KieBase
        kieSession = kieBase.newKieSession(); // Create a new session

        // Insert initial data (water meters) into the session
        List<WaterMeter> meters = getMeters(tenant);
        for (WaterMeter meter : meters) {
            kieSession.getEntryPoint("metersEntry").insert(meter);
        }

        startRulesEngine(); // Start the rule engine
    }

    /**
     * Consumes a measurement message from a Kafka topic and inserts it into the Drools session.
     *
     * @param message The JSON message containing the measurement data.
     */
    @Incoming("measurements")
    public void consumeAlarmMessage(String message) {
        AmrMeasurement m;
        try {
            m = mapper.readValue(message, AmrMeasurement.class); // Deserialize the message
            kieSession.getEntryPoint(source).insert(m); // Insert the measurement into the session
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e); // Handle deserialization errors
        }
    }

    // ===========================================================
    // ========== Implementation of abstract methods =============
    // ===========================================================

    /**
     * Loads the rules for this session from the database.
     */
    @Override
    protected void loadRules() {
        rule = getRules(tenant, "stream");
        System.out.println(rule.getRule());
    }

    /**
     * Starts the Drools rule engine in a separate thread.
     */
    @Override
    protected void startRulesEngine() {
        new Thread(kieSession::fireUntilHalt).start();
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
            System.out.println("Drools rule engine stopped.");
        }
    }
}