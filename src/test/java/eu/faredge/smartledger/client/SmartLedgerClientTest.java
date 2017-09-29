/**
 * @author ascatox
 */
package eu.faredge.smartledger.client;

import eu.faredge.smartledger.client.model.DCM;
import eu.faredge.smartledger.client.model.DSM;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class SmartLedgerClientTest {

    public static final String MYCHANNEL = "mychannel";
    private DSM dsm = null;
    private DCM dcm = null;

    @Before
    public void setup() {
        dsm = new DSM();
        dsm.setPhysicalArtifact("pippo");
        dsm.setUri("dsrc:51e5f645-38aa-4a14-90d3-0986608a9684");
        dsm.setMacAddress("ffaab1292002ddd");
        dsm.setConnectionParameters("ldldldlld");
    }

   //@Test
    public void testGetAllDataSourceManifestsWithInstallChaincode() {
        try {
            SmartLedgerClient client = new SmartLedgerClient(MYCHANNEL);
            //client.installChaincode(false);
            List<String[]> allDSMs = client.getAllDataSourceManifests();
            assertNotNull(allDSMs);
            assertFalse(allDSMs.isEmpty());
        } catch (Exception e) {
            assertFalse(e.getMessage(), true);
        }
    }

    //@Test
    public void testGetAllDataSourceManifests() {
        try {
            SmartLedgerClient client = new SmartLedgerClient(MYCHANNEL);
            List<String[]> allDSMs = client.getAllDataSourceManifests();
            assertNotNull(allDSMs);
            assertFalse(allDSMs.isEmpty());
        } catch (Exception e) {
            assertFalse(e.getMessage(), true);
        }
    }

    @Test
    public void testRegisterDSM() {
        SmartLedgerClient client = new SmartLedgerClient(MYCHANNEL);
        try {
            client.installChaincode(false);
            client.register(dsm);
            List<String[]> allDSMs = client.getAllDataSourceManifests();
            assertNotNull(allDSMs);
            assertFalse(allDSMs.isEmpty());
        } catch (Exception e) {
            assertFalse(e.getMessage(), true);
        }
    }


}
