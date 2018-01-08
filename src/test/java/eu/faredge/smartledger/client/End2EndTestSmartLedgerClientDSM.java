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

public class End2EndTestSmartLedgerClientDSM {

    static final String MYCHANNEL = "mychannel";
    static ISmartLedgerClient client = null;

    @BeforeClass
    public static void begin() {
        client = new SmartLedgerClient(MYCHANNEL, "smartLedgerUser2");
       /* try {
            SmartLedgerClient smartLedgerClient = (SmartLedgerClient) client;
            smartLedgerClient.installChaincode(false, false);
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        }*/
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
        try {
            DSM dsm = new DSM();
            dsm.setPhysicalArtifact("DEVICE20");
            dsm.setUri("http://www.mht.it");
            dsm.setMacAddress("b8:e8:56:41:43:05");
            dsm.setConnectionParameters("connectionVals:21121");
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
        try {
            DSM dsm = new DSM();
            dsm.setPhysicalArtifact("DEVICE10");
            dsm.setUri("http://www.eng-mo.it");
            dsm.setMacAddress("b8:e8:56:41:43:07");
            dsm.setConnectionParameters("connectionVals:21121");
            client.registerDSM(dsm);
            client.removeDSM(dsm.getUri());
            DSM dsmBack = null;
            try {
                dsmBack = client.getDataSourceManifestByUri(dsm.getUri());
            } catch (SmartLedgerClientException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            assertNull(dsmBack);
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        }
    }

    @Test
    public void testEditRegisteredDSMWhenIsPresent() {
        try {
            DSM dsm = new DSM();
            dsm.setPhysicalArtifact("DEVICE10");
            dsm.setUri("http://www.eng-mo.it");
            dsm.setMacAddress("b8:e8:56:41:43:07");
            dsm.setConnectionParameters("connectionVals:21121");
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
