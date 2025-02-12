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

public class StreamSession extends Session {

    private KieSession kieSession;
    private KieBaseConfiguration config;
    private KieBase kieBase;
    private KieServices kieServices;

    private Rule rule;
    private String tenant;
    private String source;

    public StreamSession() {
    }

    public StreamSession(String tenant, String source) {
        this.tenant = tenant;
        this.source = source;
    }

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

        // Create session
        kieSession = kieBase.newKieSession();

        startRulesEngine();
    }

    @Override
    protected void init() {
        kieServices = KieServices.Factory.get();

        config = kieServices.newKieBaseConfiguration();
        config.setOption(EventProcessingOption.STREAM);

        KieHelper kieHelper = new KieHelper();
        kieHelper.addContent(rule.getRule(), ResourceType.DRL);

        kieBase = kieHelper.build(config);

        kieSession = kieBase.newKieSession();

        List<WaterMeter> meters = getMeters(tenant);
        for (WaterMeter meter : meters)
            kieSession.getEntryPoint("metersEntry").insert(meter);

        startRulesEngine();
    }

    @Incoming("measurements")
    public void consumeAlarmMessage(String message) {
        AmrMeasurement m;
        try {
            m = mapper.readValue(message, AmrMeasurement.class);
            kieSession.getEntryPoint(source).insert(m);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    // ===========================================================
    // ========== Implementation of abstract methods =============
    // ===========================================================

    @Override
    protected void loadRules() {
        rule = getRules("testTenant", "stream");
        System.out.println(rule.getRule());
    }

    @Override
    protected void startRulesEngine() {
        new Thread(kieSession::fireUntilHalt).start();
        System.out.println("Drools rule engine started...");
    }

    @Override
    @PreDestroy
    protected void stopRulesEngine() {
        if (kieSession != null) {
            kieSession.halt();
            kieSession.dispose();
            System.out.println("Drools rule engine stopped.");
        }
    }


    // ===========================================================
    // =================== Setters/Getters =======================
    // ===========================================================


}
