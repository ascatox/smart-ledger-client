package eu.faredge.smartledger.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RecordDSM {
    private String key;
    private DSM record;

    @JsonProperty("Key")
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
    @JsonProperty("Record")
    public DSM getRecord() {
        return record;
    }

    public void setRecord(DSM record) {
        this.record = record;
    }
}
