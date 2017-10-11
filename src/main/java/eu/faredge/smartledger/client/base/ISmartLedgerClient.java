package eu.faredge.smartledger.client.base;

import eu.faredge.smartledger.client.exception.SmartLedgerClientException;
import eu.faredge.smartledger.client.model.DCM;
import eu.faredge.smartledger.client.model.DSM;

import java.util.List;

public interface ISmartLedgerClient {


    void registerDSM(DSM dsm) throws SmartLedgerClientException;

    void editRegisteredDSM(DSM dsm) throws SmartLedgerClientException;

    void editRegisteredDCM(DCM dcm) throws SmartLedgerClientException;

    void registerDCM(DCM dcm) throws SmartLedgerClientException;

    DSM getDataSourceManifestByUri(String uri) throws SmartLedgerClientException;

    DSM getDataSourceManifestByMacAddress(String macAddress) throws SmartLedgerClientException;

    DCM getDataConsumerManifestByMacAddress(String macAddress) throws SmartLedgerClientException;

    DCM getDataConsumerManifestByUri(String uri) throws SmartLedgerClientException;

    List<DSM> getAllDataSourceManifests() throws SmartLedgerClientException;

    void installChaincode(boolean instantiate, boolean upgrade) throws SmartLedgerClientException;

    void instantiateOrUpgradeChaincode(boolean isUpgrade) throws SmartLedgerClientException;

    List<DCM> getAllDataConsumerManifests() throws SmartLedgerClientException;

    void removeDSM(String uri) throws SmartLedgerClientException;

    void removeDCM(String uri) throws SmartLedgerClientException;
}
