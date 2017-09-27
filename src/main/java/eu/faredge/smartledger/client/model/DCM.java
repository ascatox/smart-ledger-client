package eu.faredge.smartledger.client.model;

import java.io.Serializable;

public class DCM implements Serializable {

    private String physicalArtifact;
    private String uri;
    private String macAddress;
    private String dsds;

    public DCM() {
        this.physicalArtifact = "";
        this.uri = "";
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
}
