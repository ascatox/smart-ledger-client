package eu.faredge.smartledger.client.model;

import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

public final class DSM implements Serializable {

    @NotEmpty(message = "physicalArtifact cannot be empty")
    private String physicalArtifact;

    @NotEmpty(message = "uri cannot be empty")
    @URL(message = "uri should be a valid URL")
    private String uri; //Primary key

    @Pattern(regexp = "^((([0-9A-Fa-f]{2}:){5})|(([0-9A-Fa-f]{2}-){5}))[0-9A-Fa-f]{2}$\n", message = "MAC Address is " +
            "invalid")
    private String macAddress;
    private String dsd;
    private String type;

    public DSM() {
        this.physicalArtifact = "";
        this.uri = "";
        this.macAddress = "";
        this.dsd = "";
    }

    @Override
    public String toString() {
        return "DSM{" +
                "physicalArtifact='" + physicalArtifact + '\'' +
                ", uri='" + uri + '\'' +
                ", macAddress='" + macAddress + '\'' +
                ", dsd='" + dsd + '\'' +
                ", connectionParameters='" + connectionParameters + '\'' +
                '}';
    }

    private String connectionParameters;


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

    public String getDsd() {
        return dsd;
    }

    public void setDsd(String dsd) {
        this.dsd = dsd;
    }

    public String getConnectionParameters() {
        return connectionParameters;
    }

    public void setConnectionParameters(String connectionParameters) {
        this.connectionParameters = connectionParameters;
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
