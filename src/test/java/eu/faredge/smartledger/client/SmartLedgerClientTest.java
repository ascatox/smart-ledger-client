/**
 * @author ascatox
 */
package eu.faredge.smartledger.client;

import eu.faredge.smartledger.client.base.ISmartLedgerClient;
import eu.faredge.smartledger.client.model.DCM;
import eu.faredge.smartledger.client.model.DSM;
import org.junit.*;

import java.util.List;

import static org.junit.Assert.*;

public class SmartLedgerClientTest {

    static final String MYCHANNEL = "mychannel";
    static DSM dsm = null;
    static DCM dcm = null;
    static ISmartLedgerClient client = null;

    public static void initData() {
        dsm = new DSM();
        dsm.setPhysicalArtifact("pluto");
        dsm.setUri("13");
        dsm.setMacAddress("123456789");
        dsm.setConnectionParameters("connectionVals");
    }

    @BeforeClass
    public static void begin() {
        initData();
        client = new SmartLedgerClient(MYCHANNEL);
        try {
           // client.installChaincode(true, true);
        } catch (Exception e) {
            assertFalse(e.getMessage(), true);
        }
    }

    @AfterClass
    public static void end() {
        client = null;
    }

    //@Test
    public void testGetAllDataSourceManifestsWithInstallChaincode() {
        try {

            List<DSM> allDSMs = client.getAllDataSourceManifests();
            assertNotNull(allDSMs);
            assertFalse(allDSMs.isEmpty());
        } catch (Exception e) {
            assertFalse(e.getMessage(), true);
        }
    }

    @Test
    public void testGetDataSourceManifestByUri() {
        try {
            String uri = "http://www.google.com";
            DSM dataSourceManifestByUri = client.getDataSourceManifestByUri(uri);
            assertNotNull(dataSourceManifestByUri);
            assertFalse(dataSourceManifestByUri.isEmpty());
        } catch (Exception e) {
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
        } catch (Exception e) {
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
        } catch (Exception e) {
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
        } catch (Exception e) {
            assertFalse(e.getMessage(), true);
        }
    }

    //@Test
    public void testGetAllDataSourceManifests() {
        try {
            List<DSM> allDSMs = client.getAllDataSourceManifests();
            assertNotNull(allDSMs);
            assertFalse(allDSMs.isEmpty());
        } catch (Exception e) {
            assertFalse(e.getMessage(), true);
        }
    }

    //@Test
    public void testRegisterDSM() {
        SmartLedgerClient client = new SmartLedgerClient(MYCHANNEL);
        try {
            client.registerDSM(dsm);
            List<DSM> allDSMs = client.getAllDataSourceManifests();
            assertNotNull(allDSMs);
            assertFalse(allDSMs.isEmpty());
        } catch (Exception e) {
            assertFalse(e.getMessage(), true);
        }
    }

    //@Test
    public void testRegisterDSMByUri() {
        SmartLedgerClient client = new SmartLedgerClient(MYCHANNEL);
        try {
            String uri = "dsrc:51e5f645-38aa-4a14-90d3-0986608a9684";
            client.installChaincode(true, true);
            client.registerDSM(dsm);
            DSM dsm = client.getDataSourceManifestByUri(uri);
            assertNotNull(dsm);
            assertFalse(dsm.isEmpty());
        } catch (Exception e) {
            assertFalse(e.getMessage(), true);
        }
    }
}
