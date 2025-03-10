package gr.ianic.rules;

import gr.ianic.kafkaStreams.KafkaStreamsFactory;
import gr.ianic.model.WaterMeter;
import gr.ianic.model.rules.Rule;
import gr.ianic.repositories.daos.RulesDao;
import gr.ianic.services.KafkaProducerService;
import gr.ianic.services.WaterMeterService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the creation, retrieval, and destruction of stream and scheduled sessions.
 * This class is responsible for handling sessions for different tenants and modes.
 */
@ApplicationScoped
public class SessionManager {

    @Inject
    RulesDao rulesDao; // Data access object for fetching rules

    @Inject
    WaterMeterService waterMeterService; // Service for fetching water meters

    @Inject
    KafkaStreamsFactory kafkaStreamsFactory; // Factory for creating Kafka Streams

    @Inject
    KafkaProducerService kafkaProducerService; // Service for producing Kafka messages

    // Thread-safe map to store active stream sessions, keyed by tenant.
    private Map<String, StreamSession> streamSessions;

    // Thread-safe map to store active scheduled sessions, keyed by tenant.
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

    // ===================================================================================================================================
    // ========================================================= STREAM SESSION ==========================================================
    // ===================================================================================================================================

    /**
     * Creates and initializes a new stream session for the given tenant.
     *
     * @param entryPoints The set of entry points for the session.
     * @param tenant      The tenant identifier for the session.
     * @param rules       The list of rules to be applied in the session.
     * @return {@code true} if the session was created successfully, {@code false} otherwise.
     */
    public boolean createStreamSession(Set<String> entryPoints, String tenant, List<Rule> rules) {
        System.out.println("Creating stream session for tenant " + tenant + " with entry points " + entryPoints);
        try {
            StreamSession streamSession = new StreamSession(tenant, entryPoints, rules);
            streamSession.rulesDao = rulesDao;
            streamSession.waterMeterService = waterMeterService;
            streamSession.kafkaStreamsFactory = kafkaStreamsFactory;
            streamSession.kafkaProducerService = kafkaProducerService;
            streamSession.startRulesEngine();
            addStreamSession(tenant, streamSession); // Add the session to the map
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Adds a stream session to the map using the tenant as the key.
     *
     * @param tenant  The tenant identifier for the session.
     * @param session The stream session to add.
     */
    private void addStreamSession(String tenant, StreamSession session) {
        this.streamSessions.put(tenant, session);
    }

    /**
     * Retrieves a stream session for the given tenant.
     *
     * @param tenant The tenant identifier for the session.
     * @return The stream session associated with the tenant, or {@code null} if not found.
     */
    public StreamSession getStreamSession(String tenant) {
        return streamSessions.get(tenant);
    }

    /**
     * Reloads the rules for a stream session associated with the given tenant.
     * If no rules exist for the tenant, the session is destroyed.
     *
     * @param tenant The tenant identifier for the session.
     */
    public void reloadRulesForStreamSession(String tenant) {
        StreamSession session = streamSessions.get(tenant);
        if (session != null) {
            // Fetch rules for the tenant
            List<Rule> rules = rulesDao.getByTenantAndMode(tenant, "stream").all();

            if (rules.isEmpty()) {
                // No rules exist, destroy the session
                destroyStreamSession(tenant);
            } else {
                // Process rules for the single tenant
                AbstractMap.SimpleEntry<Set<String>, List<Rule>> result = organizeSingleTenantRules(rules);

                // Extract entrypoints and rules
                Set<String> entrypoints = result.getKey();
                List<Rule> tenantRules = result.getValue();

                // Reload rules in the session
                System.out.println("Reloading rules for tenant: " + tenant);
                session.reloadRules(entrypoints, tenantRules);

                // Print the rules being reloaded
                tenantRules.forEach(rule -> System.out.println("    Rule: " + rule.getName()));
            }
        }
    }

    public boolean updateWaterMetersFact(String tenant, WaterMeter waterMeter) {
        StreamSession session = streamSessions.get(tenant);
        if (session != null) {
            session.updateWaterMeter(waterMeter);
            return true;
        }
        else
            return false;
    }

    /**
     * Destroys the stream session for the given tenant.
     *
     * @param tenant The tenant identifier for the session.
     */
    private void destroyStreamSession(String tenant) {
        StreamSession session = streamSessions.remove(tenant);
        if (session != null) {
            session.stopRulesEngine();
            System.out.println("Session destroyed for tenant: " + tenant);
        }
    }

    // ===================================================================================================================================
    // ======================================================== SCHEDULED SESSION ========================================================
    // ===================================================================================================================================

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
     * Adds a scheduled session to the map using the tenant as the key.
     *
     * @param tenant  The tenant identifier for the session.
     * @param session The scheduled session to add.
     */
    private void addScheduledSession(String tenant, ScheduledSession session) {
        this.scheduledSessions.put(tenant, session);
    }

    /**
     * Retrieves a scheduled session for the given tenant.
     *
     * @param tenant The tenant identifier for the session.
     * @return The scheduled session associated with the tenant, or {@code null} if not found.
     */
    public ScheduledSession getScheduledSession(String tenant) {
        return scheduledSessions.get(tenant);
    }

    // ===================================================================================================================================
    // ============================================================== RULES ==============================================================
    // ===================================================================================================================================

    /**
     * Organizes rules by tenant and entry point.
     *
     * @param rules The list of rules to organize.
     * @return A map where the key is the tenant and the value is another map.
     * The inner map's key is the entry point, and the value is the list of rules for that entry point.
     */
    private @NotNull Map<String, Map<String, List<Rule>>> organizeRulesByTenantAndEntrypoint(@NotNull List<Rule> rules) {
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

    /**
     * Flattens the rules organized by tenant and entry point into a simpler structure.
     *
     * @param tenantMap The map of rules organized by tenant and entry point.
     * @return A map where the key is the tenant and the value is a pair containing:
     * - A set of all unique entry points for the tenant.
     * - A list of all rules for the tenant.
     */
    private @NotNull Map<String, AbstractMap.SimpleEntry<Set<String>, List<Rule>>> flattenTenantRules(@NotNull Map<String, Map<String, List<Rule>>> tenantMap) {
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

    /**
     * Organizes rules by tenant and entry point, then flattens the structure.
     *
     * @param rules The list of rules to organize.
     * @return A map where the key is the tenant and the value is a pair containing:
     * - A set of all unique entry points for the tenant.
     * - A list of all rules for the tenant.
     */
    public Map<String, AbstractMap.SimpleEntry<Set<String>, List<Rule>>> organizeRules(List<Rule> rules) {
        Map<String, Map<String, List<Rule>>> tenantMap = organizeRulesByTenantAndEntrypoint(rules);
        return flattenTenantRules(tenantMap);
    }

    /**
     * Organizes rules for a single tenant.
     *
     * @param rules The list of rules to organize.
     * @return A pair containing:
     * - A set of all unique entry points for the tenant.
     * - A list of all rules for the tenant.
     */
    @Contract("_ -> new")
    public AbstractMap.@NotNull SimpleEntry<Set<String>, List<Rule>> organizeSingleTenantRules(@NotNull List<Rule> rules) {
        // Set to store all unique entry points
        Set<String> allEntrypoints = new HashSet<>();

        // List to store all rules (flattened)
        List<Rule> allRules = new ArrayList<>();

        // Iterate through the rules
        for (Rule rule : rules) {
            // Add all entrypoints to the set
            allEntrypoints.addAll(rule.getEntryPoints());

            // Add the rule to the list
            allRules.add(rule);
        }

        // Return the result as a Pair (or AbstractMap.SimpleEntry)
        return new AbstractMap.SimpleEntry<>(allEntrypoints, allRules);
    }
}