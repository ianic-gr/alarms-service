package gr.ianic.rules;

import gr.ianic.entities.EntitiesClient;
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

    @Inject
    EntitiesClient entitiesClient;

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


    public boolean createStreamSession(String tenant, TenantRulesInfo tenantRulesInfo) {
        System.out.println("Creating stream session for tenant " + tenant + " with info " + tenantRulesInfo);
        try {
            StreamSession streamSession = new StreamSession(tenant, tenantRulesInfo.getEntrypoints(), tenantRulesInfo.getRules(), tenantRulesInfo.getEntities(), entitiesClient);
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
                @NotNull TenantRulesInfo tenantRulesInfo = organizeSingleTenantRules(rules);

                // Extract entrypoints and rules
                Set<String> entrypoints = tenantRulesInfo.getEntrypoints();
                List<Rule> tenantRules = tenantRulesInfo.getRules();

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
        } else
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

    /*public void createScheduledSession(String source, String tenant) {
        ScheduledSession scheduledSession = new ScheduledSession();
        scheduledSessions.put(source + "-" + tenant, scheduledSession); // Add the session to the map
    }*/

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
     * Organizes rules by tenant, entry point, and entity.
     *
     * @param rules The list of rules to organize.
     * @return A map where the key is the tenant and the value is another map.
     *         The inner map's key is the entry point, and the value is the list of rules for that entry point.
     *         Additionally, rules are grouped by entities.
     */
    private @NotNull Map<String, TenantRulesGrouped> organizeRulesByTenantEntrypointAndEntity(@NotNull List<Rule> rules) {
        Map<String, TenantRulesGrouped> tenantMap = new HashMap<>();

        for (Rule rule : rules) {
            String tenant = rule.getTenant();
            Set<String> entrypoints = rule.getEntryPoints();
            Set<String> entities = rule.getEntities();

            // Initialize for tenant if absent
            tenantMap.putIfAbsent(tenant, new TenantRulesGrouped());

            TenantRulesGrouped grouped = tenantMap.get(rule.getTenant());

            // Group by entrypoint
            for (String entrypoint : entrypoints) {
                grouped.entrypointMap.putIfAbsent(entrypoint, new ArrayList<>());
                grouped.entrypointMap.get(entrypoint).add(rule);
            }

            // Group by entity
            for (String entity : entities) {
                grouped.entityMap.putIfAbsent(entity, new ArrayList<>());
                grouped.entityMap.get(entity).add(rule);
            }
        }

        return tenantMap;
    }

    /**
     * Flattens the tenant rules map into a simpler format including entrypoints, entities, and rules.
     *
     * @param tenantMap The grouped tenant map.
     * @return A map where each tenant is associated with entrypoints, entities, and all rules.
     */
    private @NotNull Map<String, TenantRulesInfo> flattenTenantRules(@NotNull Map<String, TenantRulesGrouped> tenantMap) {
        Map<String, TenantRulesInfo> result = new HashMap<>();

        for (Map.Entry<String, TenantRulesGrouped> tenantEntry : tenantMap.entrySet()) {
            String tenant = tenantEntry.getKey();
            TenantRulesGrouped grouped = tenantEntry.getValue();

            Set<String> allEntrypoints = new HashSet<>(grouped.entrypointMap.keySet());
            Set<String> allEntities = new HashSet<>(grouped.entityMap.keySet());

            List<Rule> allRules = new ArrayList<>();
            for (List<Rule> rules : grouped.entrypointMap.values()) {
                allRules.addAll(rules);
            }

            result.put(tenant, new TenantRulesInfo(allEntrypoints, allEntities, allRules));
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
    public @NotNull Map<String, TenantRulesInfo> organizeRules(List<Rule> rules) {
        @NotNull Map<String, TenantRulesGrouped> tenantMap = organizeRulesByTenantEntrypointAndEntity(rules);
        return flattenTenantRules(tenantMap);
    }

    /**
     * Organizes rules for a single tenant, collecting entry points and entities.
     *
     * @param rules The list of rules to organize.
     * @return A {@link TenantRulesInfo} object containing:
     *         - A set of all unique entry points.
     *         - A set of all unique entities.
     *         - A list of all rules.
     */
    @Contract("_ -> new")
    public @NotNull TenantRulesInfo organizeSingleTenantRules(@NotNull List<Rule> rules) {
        Set<String> allEntrypoints = new HashSet<>();
        Set<String> allEntities = new HashSet<>();
        List<Rule> allRules = new ArrayList<>();

        for (Rule rule : rules) {
            allEntrypoints.addAll(rule.getEntryPoints());
            allEntities.addAll(rule.getEntities());
            allRules.add(rule);
        }

        return new TenantRulesInfo(allEntrypoints, allEntities, allRules);
    }





    // Container to hold grouped rules by entrypoint and entity for a tenant
    private static class TenantRulesGrouped {
        Map<String, List<Rule>> entrypointMap = new HashMap<>();
        Map<String, List<Rule>> entityMap = new HashMap<>();
    }


}