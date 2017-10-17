package eu.faredge.smartledger.client.base;

import eu.faredge.smartledger.client.exception.SmartLedgerClientException;
import eu.faredge.smartledger.client.model.DCM;
import eu.faredge.smartledger.client.model.DSM;

import java.util.List;

public interface ISmartLedgerClient {


    /**
     * This method is used to create a DSM object in the Ledger
     *
     * @param dsm
     * @throws SmartLedgerClientException
     */
    void registerDSM(DSM dsm) throws SmartLedgerClientException;

    /**
     * This method performs a modify in already created DSM
     *
     * @param dsm
     * @throws SmartLedgerClientException
     */
    void editRegisteredDSM(DSM dsm) throws SmartLedgerClientException;

    /**
     * This method performs a modify in already created DCM
     *
     * @param dcm
     * @throws SmartLedgerClientException
     */
    void editRegisteredDCM(DCM dcm) throws SmartLedgerClientException;

    /**
     * This method is used to create a DCM object in the ledger
     *
     * @param dcm
     * @throws SmartLedgerClientException
     */
    void registerDCM(DCM dcm) throws SmartLedgerClientException;

    /**
     * This method gives us a DSM object if present in the ledger, it uses the URI, a unique key of DSM
     * It gives us a null object if the object is not present
     *
     * @param uri
     * @return
     * @throws SmartLedgerClientException
     */

    DSM getDataSourceManifestByUri(String uri) throws SmartLedgerClientException;

    /**
     * This method gives us a DSM object if present in the ledger, it uses the MACAddress, a unique key of DSM
     * It gives us a null object if the object is not present
     *
     * @param macAddress
     * @return
     * @throws SmartLedgerClientException
     */
    DSM getDataSourceManifestByMacAddress(String macAddress) throws SmartLedgerClientException;

    /**
     * This method gives us a DCM object if present in the ledger, it uses the MACAddress, a unique key of DCM
     * It gives us a null object if the object is not present
     *
     * @param macAddress
     * @return
     * @throws SmartLedgerClientException
     */
    DCM getDataConsumerManifestByMacAddress(String macAddress) throws SmartLedgerClientException;

    /**
     * This method gives us a DCM object if present in the ledger, it uses the URI, a unique key of DCM
     * It gives us a null object if the object is not present
     *
     * @param uri
     * @return
     * @throws SmartLedgerClientException
     */
    DCM getDataConsumerManifestByUri(String uri) throws SmartLedgerClientException;


    /**
     * This method gives us a List of DSM objects if present in the ledger.
     *
     * @return
     * @throws SmartLedgerClientException
     */
    List<DSM> getAllDataSourceManifests() throws SmartLedgerClientException;

    /**
     * This is an administration method, needed to **install** a chaincode in all peers defined in TestConfig class
     * It's possible to **instantiate** or **upgrade** a chaincode using the boolean flags
     *
     * @param instantiate
     * @param upgrade
     * @throws SmartLedgerClientException
     */

    void installChaincode(boolean instantiate, boolean upgrade) throws SmartLedgerClientException;

    /**
     * This is an administration method, needed to **instantiate** or **upgrade** a chaincode in all peers defined in
     * TestConfig class
     *
     * @param isUpgrade
     * @throws SmartLedgerClientException
     */
    void instantiateOrUpgradeChaincode(boolean isUpgrade) throws SmartLedgerClientException;

    /**
     * This method gives us a List of DCM objects if present in the ledger.
     *
     * @return
     * @throws SmartLedgerClientException
     */
    List<DCM> getAllDataConsumerManifests() throws SmartLedgerClientException;

    /**
     * This method allows to remove a DSM.
     *
     * @param uri
     * @throws SmartLedgerClientException
     */
    void removeDSM(String uri) throws SmartLedgerClientException;

    /**
     * This method allows to remove a DCM.
     *
     * @param uri
     * @throws SmartLedgerClientException
     */
    void removeDCM(String uri) throws SmartLedgerClientException;
}
