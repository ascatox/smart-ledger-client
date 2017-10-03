package eu.faredge.smartledger.client.base;

import eu.faredge.smartledger.client.model.DCM;
import eu.faredge.smartledger.client.model.DSM;

import java.util.List;

public interface ISmartLedgerClient {


    void registerDSM(DSM dsm) throws Exception;

    void registerDCM(DCM dcm) throws Exception;

    DSM getDataSourceManifest(String id) throws Exception;

    DCM getDataConsumerManifest(String id);

    List<DSM> getAllDataSourceManifests() throws Exception;

    void installChaincode(boolean instantiate, boolean upgrade) throws Exception;

    void instantiateOrUpgradeChaincode(boolean isUpgrade) throws Exception;

    List<DCM> getAllDataConsumerManifests() throws Exception;
}
