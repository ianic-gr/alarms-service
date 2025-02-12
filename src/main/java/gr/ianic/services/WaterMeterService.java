package gr.ianic.services;

import gr.ianic.model.WaterMeter;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class WaterMeterService {

    @Inject
    PgPool client; // Inject the reactive PostgreSQL client

    /**
     * Fetch all water meters from the database.
     */
    public Uni<List<WaterMeter>> getWaterMetersByTenant(String tenant) {
        return client.preparedQuery("SELECT * FROM hydrometers WHERE client_id = $1")
                .execute(Tuple.of(tenant))
                .onItem().transform(rows -> {
                    List<WaterMeter> waterMeters = new ArrayList<>();
                    for (Row row : rows) {
                        waterMeters.add(mapRowToWaterMeter(row));
                    }
                    return waterMeters;
                });
    }

    /**
     * Fetch a water meter by its serial number.
     */
    public Uni<WaterMeter> getWaterMeterBySerialNumber(String serialNumber) {
        return client.preparedQuery("SELECT * FROM hydrometers WHERE sensor_id = $1")
                .execute(Tuple.of(serialNumber))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? mapRowToWaterMeter(iterator.next()) : null);
    }

    /**
     * Fetch a water meter by its serial number.
     */
    public Uni<WaterMeter> getWaterMeterByCode(String code) {
        return client.preparedQuery("SELECT * FROM hydrometers WHERE code = $1")
                .execute(Tuple.of(code))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? mapRowToWaterMeter(iterator.next()) : null);
    }

    /**
     * Map a database row to a WaterMeter object.
     */
    private WaterMeter mapRowToWaterMeter(Row row) {
        WaterMeter waterMeter = new WaterMeter();
        waterMeter.setSerialNumber(row.getString("serial_number"));
        waterMeter.setClientId(row.getString("client_id"));
        waterMeter.setCode(row.getString("code"));
        waterMeter.setUdrometro(row.getString("udrometro"));
        waterMeter.setOldHydrometer(row.getString("old_hydrometer"));
        waterMeter.setKwdikosKatanalwti(row.getString("kwdikos_katanalwti"));
        waterMeter.setRadioAddress(row.getString("radio_address"));
        waterMeter.setGeoLocation(row.getString("geo_location"));
        waterMeter.setLatitude(row.getDouble("latitude"));
        waterMeter.setLongitude(row.getDouble("longitude"));
        waterMeter.setAppId(row.getString("app_id"));
        waterMeter.setSectorId(row.getString("sector_id"));
        waterMeter.setRemovalIndication(row.getString("removal_indication"));
        waterMeter.setLength(row.getString("length"));
        waterMeter.setDiameter(row.getString("diameter"));
        waterMeter.setType(row.getString("type"));
        waterMeter.setManufacturer(row.getString("manufacturer"));
        waterMeter.setModel(row.getString("model"));
        waterMeter.setEncryptionKey(row.getString("encryption_key"));
        waterMeter.setEncryptionProtocol(row.getString("encryption_protocol"));
        waterMeter.setAddress(row.getString("address"));
        waterMeter.setContact(row.getString("contact"));
        waterMeter.setStatus(row.getString("status"));
        waterMeter.setDate(row.getLocalDateTime("date"));
        waterMeter.setCreatedAt(row.getLocalDateTime("created_at"));
        waterMeter.setUpdatedAt(row.getLocalDateTime("updated_at"));
        return waterMeter;
    }
}