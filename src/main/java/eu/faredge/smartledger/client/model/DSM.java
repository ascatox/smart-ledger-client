package eu.faredge.smartledger.client.model;

import java.io.Serializable;

public final class DSM implements Serializable {

    private String physicalArtifact;
    private String uri;
    private String macAddress;
    private String dsd;

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
}
