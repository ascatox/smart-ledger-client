package eu.faredge.smartledger.client.model;

import eu.faredge.smartledger.client.util.Util;
import eu.faredge.smartledger.client.util.Validator;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

public class DCM implements Serializable {

    @NotEmpty(message = Validator.PHYSICAL_ARTIFACT_CANNOT_BE_EMPTY_MESSAGE)
    private String physicalArtifact;
    @NotEmpty(message = Validator.URI_CANNOT_BE_EMPTY_MESSAGE)
    @URL(message = Validator.URI_INVALID_MESSAGE)
    private String uri;
    @Pattern(regexp = Util.REGEX_MAC_ADDRESS, message = Validator.MAC_ADDRESS_INVALID_MESSAGE)
    private String macAddress;
    private String dsds;
    private String type;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DCM dcm = (DCM) o;

        if (!physicalArtifact.equals(dcm.physicalArtifact)) return false;
        if (!uri.equals(dcm.uri)) return false;
        if (macAddress != null ? !macAddress.equals(dcm.macAddress) : dcm.macAddress != null) return false;
        if (dsds != null ? !dsds.equals(dcm.dsds) : dcm.dsds != null) return false;
        return type != null ? type.equals(dcm.type) : dcm.type == null;
    }

    @Override
    public int hashCode() {
        int result = physicalArtifact.hashCode();
        result = 31 * result + uri.hashCode();
        return result;
    }

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
