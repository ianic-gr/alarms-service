package gr.ianic.rules;

import gr.ianic.kafkaStreams.KafkaStreamsFactory;
import gr.ianic.model.rules.Rule;
import gr.ianic.repositories.daos.RulesDao;
import gr.ianic.services.KafkaProducerService;
import gr.ianic.services.WaterMeterService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A factory class for managing stream and scheduled sessions.
 * This class provides methods to create, store, and retrieve sessions for different sources and tenants.
 * It uses thread-safe maps to store active sessions.
 */
@ApplicationScoped
public class SessionManager {

    @Inject
    RulesDao rulesDao; // Data access object for fetching rules

    @Inject
    WaterMeterService waterMeterService; // Service for fetching water meters

    @Inject
    KafkaStreamsFactory kafkaStreamsFactory;

    @Inject
    KafkaProducerService kafkaProducerService;


    // Thread-safe map to store active stream sessions, keyed by a combination of source and tenant.
    private Map<String, StreamSession> streamSessions;

    // Thread-safe map to store active scheduled sessions, keyed by a combination of source and tenant.
    private Map<String, ScheduledSession> scheduledSessions;

    /**
     * Initializes the session maps after the bean is constructed.
     * This method is automatically called by the container after dependency injection.
     */
    @PostConstruct
    private void init() {
        streamSessions = new ConcurrentHashMap<>();
        scheduledSessions = new ConcurrentHashMap<>();
    }

    /**
     * Creates and initializes a new stream session for the given source and tenant.
     *
     * @param tenant The tenant identifier for the session.
     */
    public void createStreamSession(Set<String> entryPoints, String tenant, List<Rule> rules) {
        StreamSession streamSession = new StreamSession(tenant, entryPoints, rules);
        streamSession.rulesDao = rulesDao;
        streamSession.waterMeterService = waterMeterService;
        streamSession.kafkaStreamsFactory = kafkaStreamsFactory;
        streamSession.kafkaProducerService = kafkaProducerService;
        streamSession.startRulesEngine();
        addStreamSession(tenant, streamSession); // Add the session to the map
    }

    /**
     * Creates a new scheduled session for the given source and tenant.
     *
     * @param source The source identifier for the session.
     * @param tenant The tenant identifier for the session.
     */
    public void createScheduledSession(String source, String tenant) {
        ScheduledSession scheduledSession = new ScheduledSession();
        scheduledSessions.put(source + "-" + tenant, scheduledSession); // Add the session to the map
    }

    /**
     * Adds a stream session to the map using a composite key of source and tenant.
     *
     * @param tenant  The tenant identifier for the session.
     * @param session The stream session to add.
     */
    private void addStreamSession(String tenant, StreamSession session) {
        this.streamSessions.put(tenant, session);
    }

    /**
     * Retrieves a stream session for the given source and tenant.
     *
     * @param tenant The tenant identifier for the session.
     * @return The stream session associated with the source and tenant, or null if not found.
     */
    public StreamSession getStreamSession(String tenant) {
        return streamSessions.get(tenant);
    }

    /**
     * Adds a scheduled session to the map using a composite key of source and tenant.
     *
     * @param tenant  The tenant identifier for the session.
     * @param session The scheduled session to add.
     */
    private void addScheduledSession(String tenant, ScheduledSession session) {
        this.scheduledSessions.put(tenant, session);
    }

    /**
     * Retrieves a scheduled session for the given source and tenant.
     *
     * @param tenant The tenant identifier for the session.
     * @return The scheduled session associated with the source and tenant, or null if not found.
     */
    public ScheduledSession getScheduledSession(String tenant) {
        return scheduledSessions.get(tenant);
    }

    public void reloadRulesForStreamSession(String tenant) {
        StreamSession session = streamSessions.get(tenant);
        if (session != null) {
            List<Rule> rules = rulesDao.getByTenantAndMode(tenant, "stream").all();
            Map<String, AbstractMap.SimpleEntry<Set<String>, List<Rule>>> organizedRules = organizeRules(rules);

            if (rules.isEmpty()) {
                // No rules exist, destroy the session
                destroyStreamSession(tenant);
            } else {
                // Reload rules in the session
                organizedRules.forEach((_tenant, erTuple) -> {
                    System.out.println("Reloading rules for tenant: " + tenant);
                    session.reloadRules(erTuple.getKey(), erTuple.getValue());
                    erTuple.getValue().forEach(rule -> System.out.println("    Rule: " + rule.getName()));
                });
            }
        }
    }

    private void destroyStreamSession(String tenant) {
        StreamSession session = streamSessions.remove(tenant);
        if (session != null) {
            session.stopRulesEngine();
            System.out.println("Session destroyed for tenant: " + tenant);
        }
    }


    private Map<String, Map<String, List<Rule>>> organizeRulesByTenantAndEntrypoint(@NotNull List<Rule> rules) {
        // Map to store the result
        Map<String, Map<String, List<Rule>>> tenantMap = new HashMap<>();

        for (Rule rule : rules) {
            String tenant = rule.getTenant();
            Set<String> entrypoints = rule.getEntryPoints();

            // If the tenant is not already in the map, add them
            tenantMap.putIfAbsent(tenant, new HashMap<>());

            // Get the map of entry points to rules for the current tenant
            Map<String, List<Rule>> entrypointMap = tenantMap.get(tenant);

            // Iterate through each entrypoint and add the rule to the corresponding list
            for (String entrypoint : entrypoints) {
                entrypointMap.putIfAbsent(entrypoint, new ArrayList<>());
                entrypointMap.get(entrypoint).add(rule);
            }
        }

        return tenantMap;
    }

    private Map<String, AbstractMap.SimpleEntry<Set<String>, List<Rule>>> flattenTenantRules(@NotNull Map<String, Map<String, List<Rule>>> tenantMap) {
        Map<String, AbstractMap.SimpleEntry<Set<String>, List<Rule>>> result = new HashMap<>();

        for (Map.Entry<String, Map<String, List<Rule>>> tenantEntry : tenantMap.entrySet()) {
            String tenant = tenantEntry.getKey();
            Map<String, List<Rule>> entrypointMap = tenantEntry.getValue();

            // Collect all unique entry points
            Set<String> allEntrypoints = new HashSet<>(entrypointMap.keySet());

            // Collect all rules (flattened)
            List<Rule> allRules = new ArrayList<>();
            for (List<Rule> rules : entrypointMap.values()) {
                allRules.addAll(rules);
            }

            // Add to the result map
            result.put(tenant, new AbstractMap.SimpleEntry<>(allEntrypoints, allRules));
        }

        return result;
    }

    protected Map<String, AbstractMap.SimpleEntry<Set<String>, List<Rule>>> organizeRules(List<Rule> rules) {
        Map<String, Map<String, List<Rule>>> tenantMap = organizeRulesByTenantAndEntrypoint(rules);
        return flattenTenantRules(tenantMap);
    }

}
