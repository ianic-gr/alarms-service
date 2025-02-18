package gr.ianic.rules;

import gr.ianic.kafkaStreams.KafkaStreamsFactory;
import gr.ianic.model.rules.Rule;
import gr.ianic.repositories.daos.RulesDao;
import gr.ianic.services.KafkaProducerService;
import gr.ianic.services.WaterMeterService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A factory class for managing stream and scheduled sessions.
 * This class provides methods to create, store, and retrieve sessions for different sources and tenants.
 * It uses thread-safe maps to store active sessions.
 */
@ApplicationScoped
public class SessionFactory {

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
     * @param source The source identifier for the session.
     * @param tenant The tenant identifier for the session.
     */
    public void createStreamSession(String source, String tenant, List<Rule> rules) {
        StreamSession streamSession = new StreamSession(tenant, source, rules);
        streamSession.rulesDao = rulesDao;
        streamSession.waterMeterService = waterMeterService;
        streamSession.kafkaStreamsFactory = kafkaStreamsFactory;
        streamSession.kafkaProducerService = kafkaProducerService;
        streamSession.startRulesEngine();
        addStreamSession(source, tenant, streamSession); // Add the session to the map
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
     * @param source  The source identifier for the session.
     * @param tenant  The tenant identifier for the session.
     * @param session The stream session to add.
     */
    private void addStreamSession(String source, String tenant, StreamSession session) {
        this.streamSessions.put(source + "-" + tenant, session);
    }

    /**
     * Retrieves a stream session for the given source and tenant.
     *
     * @param source The source identifier for the session.
     * @param tenant The tenant identifier for the session.
     * @return The stream session associated with the source and tenant, or null if not found.
     */
    public StreamSession getStreamSession(String source, String tenant) {
        return streamSessions.get(source + "-" + tenant);
    }

    /**
     * Adds a scheduled session to the map using a composite key of source and tenant.
     *
     * @param source  The source identifier for the session.
     * @param tenant  The tenant identifier for the session.
     * @param session The scheduled session to add.
     */
    private void addScheduledSession(String source, String tenant, ScheduledSession session) {
        this.scheduledSessions.put(source + "-" + tenant, session);
    }

    /**
     * Retrieves a scheduled session for the given source and tenant.
     *
     * @param source The source identifier for the session.
     * @param tenant The tenant identifier for the session.
     * @return The scheduled session associated with the source and tenant, or null if not found.
     */
    public ScheduledSession getScheduledSession(String source, String tenant) {
        return scheduledSessions.get(source + "-" + tenant);
    }


}