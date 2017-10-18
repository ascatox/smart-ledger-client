package eu.faredge.smartledger.client.model;

import eu.faredge.smartledger.client.util.Util;
import eu.faredge.smartledger.client.util.Validator;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
/**
 * Data Consumer Manifest is a manifest used to describe technologies producing data.
 */
public final class DSM implements Serializable {


    @NotEmpty(message = Validator.PHYSICAL_ARTIFACT_CANNOT_BE_EMPTY_MESSAGE)
    private String physicalArtifact;

    @NotEmpty(message = Validator.URI_CANNOT_BE_EMPTY_MESSAGE)
    @URL(message = Validator.URI_INVALID_MESSAGE)
    private String uri; //Primary key

    @Pattern(regexp = Util.REGEX_MAC_ADDRESS, message = Validator.MAC_ADDRESS_INVALID_MESSAGE)
    private String macAddress;
    private String dsd;
    private String type;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DSM dsm = (DSM) o;

        if (!physicalArtifact.equals(dsm.physicalArtifact)) return false;
        if (!uri.equals(dsm.uri)) return false;
        if (macAddress != null ? !macAddress.equals(dsm.macAddress) : dsm.macAddress != null) return false;
        if (dsd != null ? !dsd.equals(dsm.dsd) : dsm.dsd != null) return false;
        if (type != null ? !type.equals(dsm.type) : dsm.type != null) return false;
        return connectionParameters != null ? connectionParameters.equals(dsm.connectionParameters) : dsm
                .connectionParameters == null;
    }

    @Override
    public int hashCode() {
        int result = physicalArtifact.hashCode();
        result = 31 * result + uri.hashCode();
        result = 31 * result + (macAddress != null ? macAddress.hashCode() : 0);
        result = 31 * result + (dsd != null ? dsd.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (connectionParameters != null ? connectionParameters.hashCode() : 0);
        return result;
    }

    public DSM() {
        this.physicalArtifact = "";
        this.uri = "";
        this.macAddress = "";
        this.dsd = "";
        this.type = "DSM";
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
