package gr.ianic.rules;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.ianic.model.WaterMeter;
import gr.ianic.model.rules.Rule;
import gr.ianic.repositories.daos.RulesDao;
import gr.ianic.services.WaterMeterService;
import jakarta.inject.Inject;

import java.util.List;

/**
 * An abstract base class for managing sessions.
 * This class provides common functionality for loading rules and water meters,
 * as well as abstract methods for initializing and managing the rule engine.
 */
public abstract class Session {

    @Inject
    RulesDao rulesDao; // Data access object for fetching rules

    @Inject
    WaterMeterService waterMeterService; // Service for fetching water meters

    // ObjectMapper for JSON serialization and deserialization
    protected final static ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * Fetches the rules for the given tenant and type.
     *
     * @param tenant The tenant identifier.
     * @param type The type of rules to fetch (e.g., "stream").
     * @return The rule associated with the tenant and type.
     */
    public Rule getRules(String tenant, String type) {
        return rulesDao.getByTenantAndType(tenant, type);
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

    /**
     * Starts the rule engine.
     */
    protected abstract void startRulesEngine();

    /**
     * Stops the rule engine.
     */
    protected abstract void stopRulesEngine();

    /**
     * Initializes the session.
     */
    protected abstract void init();

    /**
     * Loads the rules for the session.
     */
    protected abstract void loadRules();
}