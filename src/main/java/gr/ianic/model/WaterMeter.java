package gr.ianic.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Locale;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WaterMeter {

    private static final ObjectMapper mapper = new ObjectMapper();

    private String serialNumber;
    private String clientId;
    private String code;
    private String udrometro;
    private String oldHydrometer;
    private String kwdikosKatanalwti;
    private String radioAddress;
    private String geoLocation;
    private Double latitude;
    private Double longitude;
    private String appId;
    private String sectorId;
    private String removalIndication;
    private String length;
    private String diameter;
    private String type;
    private String manufacturer;
    private String model;
    private String encryptionKey;
    private String encryptionProtocol;
    private String address;
    private String contact;
    private String status;
    private LocalDateTime date;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public WaterMeter() {
    }

    // Getters and Setters with Jackson annotations
    @JsonGetter("serial_number")
    public String getSerialNumber() {
        return serialNumber;
    }

    @JsonSetter("serial_number")
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    @JsonGetter("client_id")
    public String getClientId() {
        return clientId;
    }

    @JsonSetter("client_id")
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @JsonGetter("code")
    public String getCode() {
        return code;
    }

    @JsonSetter("code")
    public void setCode(String code) {
        this.code = code;
    }

    @JsonGetter("udrometro")
    public String getUdrometro() {
        return udrometro;
    }

    @JsonSetter("udrometro")
    public void setUdrometro(String udrometro) {
        this.udrometro = udrometro;
    }

    @JsonGetter("old_hydrometer")
    public String getOldHydrometer() {
        return oldHydrometer;
    }

    @JsonSetter("old_hydrometer")
    public void setOldHydrometer(String oldHydrometer) {
        this.oldHydrometer = oldHydrometer;
    }

    @JsonGetter("kwdikos_katanalwti")
    public String getKwdikosKatanalwti() {
        return kwdikosKatanalwti;
    }

    @JsonSetter("kwdikos_katanalwti")
    public void setKwdikosKatanalwti(String kwdikosKatanalwti) {
        this.kwdikosKatanalwti = kwdikosKatanalwti;
    }

    @JsonGetter("radio_address")
    public String getRadioAddress() {
        return radioAddress;
    }

    @JsonSetter("radio_address")
    public void setRadioAddress(String radioAddress) {
        this.radioAddress = radioAddress;
    }

    @JsonGetter("geo_location")
    public String getGeoLocation() {
        return geoLocation;
    }

    @JsonSetter("geo_location")
    public void setGeoLocation(String geoLocation) {
        this.geoLocation = geoLocation;
    }

    @JsonGetter("latitude")
    public Double getLatitude() {
        return latitude;
    }

    @JsonSetter("latitude")
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    @JsonGetter("longitude")
    public Double getLongitude() {
        return longitude;
    }

    @JsonSetter("longitude")
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @JsonGetter("app_id")
    public String getAppId() {
        return appId;
    }

    @JsonSetter("app_id")
    public void setAppId(String appId) {
        this.appId = appId;
    }

    @JsonGetter("sector_id")
    public String getSectorId() {
        return sectorId;
    }

    @JsonSetter("sector_id")
    public void setSectorId(String sectorId) {
        this.sectorId = sectorId;
    }

    @JsonGetter("removal_indication")
    public String getRemovalIndication() {
        return removalIndication;
    }

    @JsonSetter("removal_indication")
    public void setRemovalIndication(String removalIndication) {
        this.removalIndication = removalIndication;
    }

    @JsonGetter("length")
    public String getLength() {
        return length;
    }

    @JsonSetter("length")
    public void setLength(String length) {
        this.length = length;
    }

    @JsonGetter("diameter")
    public String getDiameter() {
        return diameter;
    }

    @JsonSetter("diameter")
    public void setDiameter(String diameter) {
        this.diameter = diameter;
    }

    @JsonGetter("type")
    public String getType() {
        return type;
    }

    @JsonSetter("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonGetter("manufacturer")
    public String getManufacturer() {
        return manufacturer;
    }

    @JsonSetter("manufacturer")
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    @JsonGetter("model")
    public String getModel() {
        return model;
    }

    @JsonSetter("model")
    public void setModel(String model) {
        this.model = model;
    }

    @JsonGetter("encryption_key")
    public String getEncryptionKey() {
        return encryptionKey;
    }

    @JsonSetter("encryption_key")
    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    @JsonGetter("encryption_protocol")
    public String getEncryptionProtocol() {
        return encryptionProtocol;
    }

    @JsonSetter("encryption_protocol")
    public void setEncryptionProtocol(String encryptionProtocol) {
        this.encryptionProtocol = encryptionProtocol;
    }

    @JsonGetter("address")
    public String getAddress() {
        return address;
    }

    @JsonSetter("address")
    public void setAddress(String address) {
        this.address = address;
    }

    @JsonGetter("contact")
    public String getContact() {
        return contact;
    }

    @JsonSetter("contact")
    public void setContact(String contact) {
        this.contact = contact;
    }

    @JsonGetter("status")
    public String getStatus() {
        return status;
    }

    @JsonSetter("status")
    public void setStatus(String status) {
        this.status = status.toUpperCase(Locale.ROOT);
    }

    @JsonGetter("date")
    public LocalDateTime getDate() {
        return date;
    }

    @JsonSetter("date")
    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    @JsonGetter("created_at")
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @JsonSetter("created_at")
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @JsonGetter("updated_at")
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @JsonSetter("updated_at")
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}