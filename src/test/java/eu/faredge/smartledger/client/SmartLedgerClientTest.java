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

    private DSM dsm = null;
    private DCM dcm = null;

    @Before
    public void setup() {
        dsm = new DSM();
        dsm.setUri("dsrc:51e5f645-38aa-4a14-90d3-0986608a9684");
        dsm.setMacAddress("ffaab1292002ddd");
        dsm.setConnectionParameters("ipaddr=162.232.7.15&ipport=9000");
    }

    //@Test
    public void testRegisterDSM() {
        SmartLedgerClient client = new SmartLedgerClient("mychannel");
        try {
            String register = client.register(dsm);
            assertNotNull(register);
            assertNotEquals(register, "");
        } catch (Exception e) {
            assertFalse(e.getMessage(), true);

        }
    }

   //@Test
    public void testGetAllDataSourceManifestsWithInstallChaincode() {
        try {
            SmartLedgerClient client = new SmartLedgerClient("mychannel");
            client.installChaincode(false);
            List<String[]> allDSMs = client.getAllDataSourceManifests();
            assertNotNull(allDSMs);
            assertFalse(allDSMs.isEmpty());
        } catch (Exception e) {
            assertFalse(e.getMessage(), true);
        }
    }

    @Test
    public void testGetAllDataSourceManifests() {
        try {
            SmartLedgerClient client = new SmartLedgerClient("mychannel");
           // client.instantiateChaincode();
            List<String[]> allDSMs = client.getAllDataSourceManifests();
            assertNotNull(allDSMs);
            assertFalse(allDSMs.isEmpty());
        } catch (Exception e) {
            assertFalse(e.getMessage(), true);
        }
    }

}
