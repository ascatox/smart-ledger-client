/**
 * @author ascatox
 */
package eu.faredge.smartledger.client;

import eu.faredge.smartledger.client.base.ISmartLedgerClient;
import eu.faredge.smartledger.client.exception.SmartLedgerClientException;
import eu.faredge.smartledger.client.model.DCM;
import eu.faredge.smartledger.client.model.DSM;
import org.junit.*;

import java.util.List;
import static org.hamcrest.CoreMatchers.*;

import static org.junit.Assert.*;

public class End2EndTestSmartLedgerClient {

    static final String MYCHANNEL = "mychannel";
    static ISmartLedgerClient client = null;

    @BeforeClass
    public static void begin() {
        client = new SmartLedgerClient(MYCHANNEL);
        try {
             client.installChaincode(true, false);
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        }
    }

    @AfterClass
    public static void end() {
        client = null;
    }

    @Test
    public void testGetDataSourceManifestByUri() {
        try {
            String uri = "http://www.google.com";
            DSM dataSourceManifestByUri = client.getDataSourceManifestByUri(uri);
            assertNotNull(dataSourceManifestByUri);
            assertFalse(dataSourceManifestByUri.isEmpty());
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        }
    }

    @Test
    public void testGetDataConsumerManifestByUri() {
        try {
            String uri = "http://www.eng.it";
            DCM dataConsumerManifestByUri = client.getDataConsumerManifestByUri(uri);
            assertNotNull(dataConsumerManifestByUri);
            assertFalse(dataConsumerManifestByUri.isEmpty());
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        }
    }

    @Test
    public void testGetDataSourceManifestByMacAddress() {
        try {
            String mac = "123:456:789";
            DSM dataSourceManifestByMacAddress = client.getDataSourceManifestByMacAddress(mac);
            assertNotNull(dataSourceManifestByMacAddress);
            assertFalse(dataSourceManifestByMacAddress.isEmpty());
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        }
    }


    @Test
    public void testGetDataConsumerManifestByMacAddress() {
        try {
            String mac = "321:654:987";
            DCM dataConsumerManifestByMacAddress = client.getDataConsumerManifestByMacAddress(mac);
            assertNotNull(dataConsumerManifestByMacAddress);
            assertFalse(dataConsumerManifestByMacAddress.isEmpty());
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        }
    }

    @Test
    public void testGetAllDataSourceManifests() {
        try {
            List<DSM> allDSMs = client.getAllDataSourceManifests();
            assertNotNull(allDSMs);
            assertFalse(allDSMs.isEmpty());
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        }
    }

    @Test
    public void testRegisterDSM() {
        SmartLedgerClient client = new SmartLedgerClient(MYCHANNEL);
        try {
            DSM dsm = initDSM();
            client.registerDSM(dsm);
            List<DSM> allDSMs = client.getAllDataSourceManifests();
            assertNotNull(allDSMs);
            assertFalse(allDSMs.isEmpty());
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        }
    }

    @Test
    public void testRegisterDSMByUri() {
        SmartLedgerClient client = new SmartLedgerClient(MYCHANNEL);
        try {
            DSM dsm = initDSM();
            client.installChaincode(true, true);
            client.registerDSM(dsm);
            DSM dsmBack = client.getDataSourceManifestByUri(dsm.getUri());
            assertNotNull(dsmBack);
            assertFalse(dsmBack.isEmpty());
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        }
    }


    @Test
    public void testRemoveDSM() {
        SmartLedgerClient client = new SmartLedgerClient(MYCHANNEL);
        try {
            DSM dsm = initDSM();
            client.removeDSM(dsm.getUri());
            DSM dsmBack = client.getDataSourceManifestByUri(dsm.getUri());
            assertNull(dsmBack);
            assertTrue(dsmBack.isEmpty());
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        }
    }

    @Test
    public void testEditRegisteredDSMWhenIsPresent() {
        SmartLedgerClient client = new SmartLedgerClient(MYCHANNEL);
        try {
            DSM dsm = initDSM();
            client.registerDSM(dsm);
            DSM dsmBack = client.getDataSourceManifestByUri(dsm.getUri());
            assertEquals(dsm, dsmBack);
            client.editRegisteredDSM(dsm);
            DSM dsmBack2 = client.getDataSourceManifestByUri(dsm.getUri());
            assertNotEquals(dsmBack2, not(dsm));
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        }
    }

    private DSM initDSM() {
        DSM dsm = new DSM();
        dsm.setPhysicalArtifact("DEVICE00");
        dsm.setUri("http://www.google.it");
        dsm.setMacAddress("b8:e8:56:41:43:06");
        dsm.setConnectionParameters("connectionVals:12121");
        return dsm;
    }


}
