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
        dsm.setPhysicalArtifact("pippo");
        dsm.setUri("12");
        dsm.setMacAddress("ffaab1292002ddd");
        dsm.setConnectionParameters("connParams");
    }

    @BeforeClass
    public static void begin() {
        initData();
        client = new SmartLedgerClient(MYCHANNEL);
        try {
            //client.installChaincode(true, true);
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
            String id = "dsrc:51e5f645-38aa-4a14-90d3-0986608a9684";
            client.installChaincode(true, true);
            client.registerDSM(dsm);
            DSM dsm = client.getDataSourceManifest(id);
            assertNotNull(dsm);
            assertFalse(dsm.isEmpty());
        } catch (Exception e) {
            assertFalse(e.getMessage(), true);
        }
    }


}
