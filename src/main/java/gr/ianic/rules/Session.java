package gr.ianic.rules;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.ianic.model.WaterMeter;
import gr.ianic.model.rules.Rule;
import gr.ianic.repositories.daos.RulesDao;
import gr.ianic.services.WaterMeterService;
import jakarta.inject.Inject;

import java.util.List;

public abstract class Session {

    @Inject
    RulesDao rulesDao;

    @Inject
    WaterMeterService waterMeterService;

    protected final static ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


    public Rule getRules(String tenant, String type) {
        return rulesDao.getByTenantAndType(tenant, type);
    }

    public List<WaterMeter> getMeters(String tenant) {
        return waterMeterService.getWaterMetersByTenant(tenant).await().indefinitely();
    }

    protected abstract void startRulesEngine();

    protected abstract void stopRulesEngine();

    protected abstract void init();

    protected abstract void loadRules();

}
