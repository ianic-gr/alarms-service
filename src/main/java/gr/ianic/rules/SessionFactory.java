package gr.ianic.rules;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class SessionFactory {

    private Map<String, StreamSession> streamSessions;
    private Map<String, ScheduledSession> scheduledSessions;

    @PostConstruct
    public void init() {
        streamSessions = new HashMap<String, StreamSession>();
        scheduledSessions = new HashMap<String, ScheduledSession>();
    }

    public void createStreamSession(String tenant, String source) {
        StreamSession streamSession = new StreamSession(tenant, source);
        streamSession.init();
        streamSessions.put(tenant, streamSession);
    }

    public void createScheduledSession(String tenant, String source) {
        //TODO: implement
    }


    private void addStreamSession(String tenant, String source, StreamSession session) {
        this.streamSessions.put(tenant + "-" + source, session);
    }

    public StreamSession getStreamSession(String tenant, String source) {
        return streamSessions.get(tenant + "-" + source);
    }

}
