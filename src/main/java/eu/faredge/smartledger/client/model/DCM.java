package eu.faredge.smartledger.client.model;

import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

public class DCM implements Serializable {

    @NotEmpty(message = "physicalArtifact cannot be empty")
    private String physicalArtifact;
    @NotEmpty(message = "uri cannot be empty")
    @URL(message = "uri should be a valid URL")
    private String uri;
    @Pattern(regexp = "^((([0-9A-Fa-f]{2}:){5})|(([0-9A-Fa-f]{2}-){5}))[0-9A-Fa-f]{2}$\n", message = "MAC Address is " +
            "invalid")
    private String macAddress;
    private String dsds;
    private String type;

    public DCM() {
        this.physicalArtifact = "";
        this.uri = ""; //Primary Key
        this.macAddress = "";
        this.dsds = "";
    }

    @Override
    public String toString() {
        return "DCM{" +
                "physicalArtifact='" + physicalArtifact + '\'' +
                ", uri='" + uri + '\'' +
                ", macAddress='" + macAddress + '\'' +
                ", dsds='" + dsds + '\'' +
                '}';
    }

    public String getPhysicalArtifact() {
        return physicalArtifact;
    }

    public void setPhysicalArtifact(String physicalArtifact) {
        this.physicalArtifact = physicalArtifact;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getDsds() {
        return dsds;
    }

    public void setDsds(String dsds) {
        this.dsds = dsds;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isEmpty() {
        return (this.getUri().isEmpty() || this.getMacAddress().isEmpty());
    }
}
