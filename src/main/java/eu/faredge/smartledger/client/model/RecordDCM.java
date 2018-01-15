
package eu.faredge.smartledger.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.faredge.dm.dcm.DCM;

public class RecordDCM {

    private String key;
    private DCM record;

    @JsonProperty("Key")
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @JsonProperty("Record")
    public DCM getRecord() {
        return record;
    }

    public void setRecord(DCM record) {
        this.record = record;
    }
}

