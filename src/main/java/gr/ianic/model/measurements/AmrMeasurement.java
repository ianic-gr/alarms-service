package gr.ianic.model.measurements;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import org.kie.api.definition.type.Role;

/**
 * Represents an AMR (Automatic Meter Reading) measurement.
 * This class includes fields for status, identifiers, volume, and debug information.
 * It is annotated as an event for use in rules engine.
 */
@Role(Role.Type.EVENT)
public class AmrMeasurement {

    // ========================== Status Fields ==========================

    private Integer power_low; // Indicates low power status
    private Integer hardware; // Indicates hardware issues
    private Integer empty_spool; // Indicates empty spool status
    private Integer reverse_flow; // Indicates reverse flow status
    private Integer leakage; // Indicates leakage status
    private Integer burst; // Indicates burst status
    private Integer freeze; // Indicates freeze status
    private Integer tamper; // Indicates tamper status
    private Integer rssi; // Received Signal Strength Indicator
    private Double snr; // Signal-to-Noise Ratio

    private Integer valid = 1; // Indicates if the measurement is valid
    private long noMeasurementDays = 0; // Number of days without measurements

    // ================== First Error Occurrence Dates ==================

    private String first_power_low_occurrence; // First occurrence of low power
    private String first_hardware_occurrence; // First occurrence of hardware issues
    private String first_empty_spool_occurrence; // First occurrence of empty spool
    private String first_reverse_flow_occurrence; // First occurrence of reverse flow
    private String first_leakage_occurrence; // First occurrence of leakage
    private String first_burst_occurrence; // First occurrence of burst
    private String first_freeze_occurrence; // First occurrence of freeze
    private String first_tamper_occurrence; // First occurrence of tamper

    // ====================== Identifier Fields ======================

    private String meter_address; // Address of the meter
    private String reading_date; // Date of the reading
    private String telegram; // Raw telegram data
    private String gateway_id; // ID of the gateway
    private String filename; // Name of the file
    private String source; // Source of the measurement
    private String operator_latitude; // Latitude of the operator
    private String operator_longitude; // Longitude of the operator
    private String notes; // Additional notes
    private String routelist_id; // ID of the routelist
    private String user_id; // ID of the user

    // ====================== Volume Fields ======================

    private Long volume; // Volume measurement
    private Long consumption; // Consumption measurement
    private Long summarizedConsumption; // Summarized consumption

    // ====================== Debug Fields ======================

    private Integer wasFirst = 0; // Indicates if this is the first measurement
    private Integer negativeDelta = 0; // Indicates negative delta in measurements
    private Integer outOfOrder = 0; // Indicates out-of-order measurements
    private int isSimulated = 0; // Indicates if the measurement is simulated
    private int isApproximation = 0; // Indicates if the measurement is an approximation
    private int isNovelty = 0; // Indicates if the measurement is a novelty
    private Integer last; // Indicates if this is the last measurement

    /**
     * Default constructor.
     */
    public AmrMeasurement() {
    }

    // ====================== Getters and Setters ======================

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
    public String getReading_date() {
        return reading_date;
    }

    @JsonSetter("reading_date")
    public void setReading_date(String reading_date) {
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