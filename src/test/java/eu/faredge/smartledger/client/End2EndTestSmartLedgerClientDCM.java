/**
 * @author ascatox
 */
package eu.faredge.smartledger.client;

import eu.faredge.smartledger.client.base.ISmartLedgerClient;
import eu.faredge.smartledger.client.exception.SmartLedgerClientException;
import eu.faredge.smartledger.client.model.DCM;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

public class End2EndTestSmartLedgerClientDCM {

    static final String MYCHANNEL = "mychannel";
    static ISmartLedgerClient client = null;

    @BeforeClass
    public static void begin() {
        client = new SmartLedgerClient(MYCHANNEL);
    }

    @AfterClass
    public static void end() {
        client = null;
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
    public void testGetAllDataConsumerManifests() {
        try {
            List<DCM> all = client.getAllDataConsumerManifests();
            assertNotNull(all);
            assertFalse(all.isEmpty());
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        }
    }

    @Test
    public void testRegisterDCM() {
        try {
            DCM dcm = new DCM();
            dcm.setPhysicalArtifact("DEVICE30");
            dcm.setUri("http://www.overit.it");
            dcm.setMacAddress("b8:e8:56:41:43:05");
            client.registerDCM(dcm);
            List<DCM> all = client.getAllDataConsumerManifests();
            assertNotNull(all);
            assertFalse(all.isEmpty());
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        }
    }


    @Test
    public void testRemoveDCM() {
        try {
            DCM dcm = new DCM();
            dcm.setPhysicalArtifact("DEVICE31");
            dcm.setUri("http://www.overit.it");
            dcm.setMacAddress("b8:e8:56:41:43:05");
            client.registerDCM(dcm);
            client.removeDCM(dcm.getUri());
            DCM back = null;
            try {
                back = client.getDataConsumerManifestByUri(dcm.getUri());
            } catch (SmartLedgerClientException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            assertNull(back);
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        }
    }

    @Test
    public void testEditRegisteredDCMWhenIsPresent() {
        try {
            DCM dcm = new DCM();
            dcm.setPhysicalArtifact("DEVICE31");
            dcm.setUri("http://www.opcua.com");
            dcm.setMacAddress("b8:e8:56:41:43:08");
            client.registerDCM(dcm);
            DCM back = client.getDataConsumerManifestByUri(dcm.getUri());
            assertEquals(dcm, back);
            client.editRegisteredDCM(dcm);
            DCM back2 = client.getDataConsumerManifestByUri(dcm.getUri());
            assertNotEquals(back2, not(dcm));
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        }
    }


}
