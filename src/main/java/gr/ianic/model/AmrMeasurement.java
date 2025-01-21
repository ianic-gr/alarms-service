package gr.ianic.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kie.api.definition.type.Role;
import org.kie.api.definition.type.Timestamp;

@Role(Role.Type.EVENT)
@Timestamp("reading_date")
public class AmrMeasurement {


    // status fields
    private Integer power_low;
    private Integer hardware;
    private Integer empty_spool;
    private Integer reverse_flow;
    private Integer leakage;
    private Integer burst;
    private Integer freeze;
    private Integer tamper;
    private Integer rssi;
    private Double snr;


    private Integer valid = 1;
    private long noMeasurementDays = 0;


    // first error occurrence date
    private String first_power_low_occurrence;
    private String first_hardware_occurrence;
    private String first_empty_spool_occurrence;
    private String first_reverse_flow_occurrence;
    private String first_leakage_occurrence;
    private String first_burst_occurrence;
    private String first_freeze_occurrence;
    private String first_tamper_occurrence;


    // identifier fields
    private String meter_address;
    private String reading_date;
    private String telegram;
    private String gateway_id;
    private String filename;
    private String source;
    private String operator_latitude;
    private String operator_longitude;
    private String notes;
    private String routelist_id;
    private String user_id;

    // volume field
    private Long volume;
    private Long consumption;
    private Long summarizedConsumption;


    // debug
    private Integer wasFirst = 0;
    private Integer negativeDelta = 0;
    private Integer outOfOrder = 0;
    private int isSimulated = 0;
    private int isApproximation = 0;
    private int isNovelty = 0;
    private Integer last;

    public AmrMeasurement() {
    }

    /* Setters/Getters */
    @JsonGetter("isSimulated")
    public int isSimulated() {
        return isSimulated;
    }

    @JsonSetter("isSimulated")
    public void setSimulated(int simulated) {
        isSimulated = simulated;
    }

    @JsonGetter("isApproximation")
    public int isApproximation() {
        return isApproximation;
    }

    public void setApproximation(int approximation) {
        isApproximation = approximation;
    }

    @JsonGetter("isNovelty")
    public int isNovelty() {
        return isNovelty;
    }


    public void setNovelty(int novelty) {
        isNovelty = novelty;
    }

    @JsonIgnore
    public Integer getLast() {
        return last;
    }

    @JsonSetter
    public void setLast(Integer last) {
        this.last = last;
    }

    @JsonGetter("rssi")
    public Integer getRssi() {
        return rssi;
    }

    @JsonSetter("rssi")
    public void setRssi(Integer rssi) {
        this.rssi = rssi;
    }

    @JsonGetter("snr")
    public Double getSnr() {
        return snr;
    }

    @JsonSetter("snr")
    public void setSnr(Double snr) {
        this.snr = snr;
    }

    @JsonGetter("outOfOrder")
    public Integer getOutOfOrder() {
        return this.outOfOrder;
    }

    @JsonSetter("outOfOrder")
    public void setOutOfOrder(Integer outOfOrder) {
        this.outOfOrder = outOfOrder;
    }

    @JsonGetter("negativeDelta")
    public Integer getNegativeDelta() {
        return this.negativeDelta;
    }

    @JsonSetter("negativeDelta")
    public void setNegativeDelta(Integer negativeDelta) {
        this.negativeDelta = negativeDelta;
    }

    @JsonGetter("wasFirst")
    public Integer getWasFirst() {
        return this.wasFirst;
    }

    @JsonSetter("wasFirst")
    public void setWasFirst(Integer wasFirst) {
        this.wasFirst = wasFirst;
    }

    @JsonGetter("volume")
    public Long getVolume() {
        return volume;
    }

    @JsonSetter("volume")
    public void setVolume(Long volume) {
        this.volume = volume;
    }

    @JsonGetter("power_low")
    public Integer getPowerLow() {
        return power_low;
    }

    @JsonSetter("power_low")
    public void setPowerLow(Integer power_low) {
        this.power_low = power_low;
    }

    @JsonGetter("hardware")
    public Integer getHardware() {
        return hardware;
    }

    @JsonSetter("hardware")
    public void setHardware(Integer hardware) {
        this.hardware = hardware;
    }

    @JsonGetter("empty_spool")
    public Integer getEmptySpool() {
        return empty_spool;
    }

    @JsonSetter("empty_spool")
    public void setEmptySpool(Integer empty_spool) {
        this.empty_spool = empty_spool;
    }

    @JsonGetter("reverse_flow")
    public Integer getReverseFlow() {
        return reverse_flow;
    }

    @JsonSetter("reverse_flow")
    public void setReverseFlow(Integer reverse_flow) {
        this.reverse_flow = reverse_flow;
    }

    @JsonGetter("leakage")
    public Integer getLeakage() {
        return leakage;
    }

    @JsonSetter("leakage")
    public void setLeakage(Integer leakage) {
        this.leakage = leakage;
    }

    @JsonGetter("burst")
    public Integer getBurst() {
        return burst;
    }

    @JsonSetter("burst")
    public void setBurst(Integer burst) {
        this.burst = burst;
    }

    @JsonGetter("freeze")
    public Integer getFreeze() {
        return freeze;
    }

    @JsonSetter("freeze")
    public void setFreeze(Integer freeze) {
        this.freeze = freeze;
    }

    @JsonGetter("tamper")
    public Integer getTamper() {
        return tamper;
    }

    @JsonSetter("tamper")
    public void setTamper(Integer tamper) {
        this.tamper = tamper;
    }

    @JsonGetter("meter_address")
    public String getMeterAddress() {
        return meter_address;
    }

    @JsonSetter("meter_address")
    public void setMeterAddress(String meter_address) {
        this.meter_address = meter_address;
    }

    @JsonGetter("operator_latitude")
    public String getOperatorLatitude() {
        return operator_latitude;
    }

    @JsonSetter("operator_latitude")
    public void setOperatorLatitude(String operator_latitude) {
        this.operator_latitude = operator_latitude;
    }

    @JsonGetter("operator_longitude")
    public String getOperatorLongitude() {
        return operator_longitude;
    }

    @JsonSetter("operator_longitude")
    public void setOperatorLongitude(String operator_longitude) {
        this.operator_longitude = operator_longitude;
    }

    @JsonGetter("notes")
    public String getNotes() {
        return notes;
    }

    @JsonSetter("notes")
    public void setNotes(String notes) {
        this.notes = notes;
    }

    @JsonGetter("routelist_id")
    public String getRoutelistId() {
        return routelist_id;
    }

    @JsonSetter("routelist_id")
    public void setRoutelistId(String routelist_id) {
        this.routelist_id = routelist_id;
    }

    @JsonGetter("user_id")
    public String getUserId() {
        return user_id;
    }

    @JsonSetter("user_id")
    public void setUserId(String user_id) {
        this.user_id = user_id;
    }

    @JsonGetter("source")
    public String getSource() {
        return source;
    }

    @JsonSetter("source")
    public void setSource(String source) {
        this.source = source;
    }

    @JsonGetter("reading_date")
    public String getReadingDate() {
        return reading_date;
    }

    @JsonSetter("reading_date")
    public void setReadingDate(String reading_date) {
        this.reading_date = reading_date;
    }

    @JsonGetter("telegram")
    public String getTelegram() {
        return telegram;
    }

    @JsonSetter("telegram")
    public void setTelegram(String raw_telegram) {
        this.telegram = raw_telegram;
    }

    @JsonGetter("gateway_id")
    public String getGatewayId() {
        return gateway_id;
    }

    @JsonSetter("gateway_id")
    public void setGatewayId(String gateway_id) {
        this.gateway_id = gateway_id;
    }

    @JsonGetter("filename")
    public String getFilename() {
        return filename;
    }

    @JsonSetter("filename")
    public void setFilename(String filename) {
        this.filename = filename;
    }

    @JsonGetter("consumption")
    public Long getConsumption() {
        return consumption;
    }

    @JsonSetter("consumption")
    public void setConsumption(Long consumption) {
        this.consumption = consumption;
    }

    @JsonGetter("first_power_low_occurrence")
    public String getFirstPowerLowOccurrence() {
        return first_power_low_occurrence;
    }

    @JsonSetter("first_power_low_occurrence")
    public void setFirstPowerLowOccurrence(String first_power_low_occurrence) {
        this.first_power_low_occurrence = first_power_low_occurrence;
    }

    @JsonGetter("first_hardware_occurrence")
    public String getFirstHardwareOccurrence() {
        return first_hardware_occurrence;
    }

    @JsonSetter("first_hardware_occurrence")
    public void setFirstHardwareOccurrence(String first_hardware_occurrence) {
        this.first_hardware_occurrence = first_hardware_occurrence;
    }

    @JsonGetter("first_empty_spool_occurrence")
    public String getFirstEmptySpoolOccurrence() {
        return first_empty_spool_occurrence;
    }

    @JsonSetter("first_empty_spool_occurrence")
    public void setFirstEmptySpoolOccurrence(String first_empty_spool_occurrence) {
        this.first_empty_spool_occurrence = first_empty_spool_occurrence;
    }

    @JsonGetter("first_reverse_flow_occurrence")
    public String getFirstReverseFlowOccurrence() {
        return first_reverse_flow_occurrence;
    }

    @JsonSetter("first_reverse_flow_occurrence")
    public void setFirstReverseFlowOccurrence(String first_reverse_flow_occurrence) {
        this.first_reverse_flow_occurrence = first_reverse_flow_occurrence;
    }

    @JsonGetter("first_leakage_occurrence")
    public String getFirstLeakageOccurrence() {
        return first_leakage_occurrence;
    }

    @JsonSetter("first_leakage_occurrence")
    public void setFirstLeakageOccurrence(String first_leakage_occurrence) {
        this.first_leakage_occurrence = first_leakage_occurrence;
    }

    @JsonGetter("first_burst_occurrence")
    public String getFirstBurstOccurrence() {
        return first_burst_occurrence;
    }

    @JsonSetter("first_burst_occurrence")
    public void setFirstBurstOccurrence(String first_burst_occurrence) {
        this.first_burst_occurrence = first_burst_occurrence;
    }

    @JsonGetter("first_freeze_occurrence")
    public String getFirstFreezeOccurrence() {
        return first_freeze_occurrence;
    }

    @JsonSetter("first_freeze_occurrence")
    public void setFirstFreezeOccurrence(String first_freeze_occurrence) {
        this.first_freeze_occurrence = first_freeze_occurrence;
    }

    @JsonGetter("first_tamper_occurrence")
    public String getFirstTamperOccurrence() {
        return first_tamper_occurrence;
    }

    @JsonSetter("first_tamper_occurrence")
    public void setFirstTamperOccurrence(String first_tamper_occurrence) {
        this.first_tamper_occurrence = first_tamper_occurrence;
    }

    public Integer getValid() {
        return valid;
    }

    public void setValid(Integer valid) {
        this.valid = valid;
    }

    public long getNoMeasurementDays() {
        return noMeasurementDays;
    }

    public void setNoMeasurementDays(long noMeasurementDays) {
        this.noMeasurementDays = noMeasurementDays;
    }

    public Long getSummarizedConsumption() {
        return summarizedConsumption;
    }

    public void setSummarizedConsumption(Long summarizedConsumption) {
        this.summarizedConsumption = summarizedConsumption;
    }
}
