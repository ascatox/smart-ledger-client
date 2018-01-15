/**
 * @author ascatox
 */
package eu.faredge.smartledger.client;

import eu.faredge.dm.dcm.DCM;
import eu.faredge.smartledger.client.base.ISmartLedgerClient;
import eu.faredge.smartledger.client.exception.SmartLedgerClientException;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

public class End2EndTestSmartLedgerClientDCM {

    static ISmartLedgerClient client = null;

    @BeforeClass
    public static void begin() {
        client = new SmartLedgerClient();
    }

    @AfterClass
    public static void end() {
        client = null;
    }


    @Test
    public void testGetDataConsumerManifestByUri() {
        try {
            String uri = "http://www.eng.it";
            DCM dataConsumerManifestByUri = client.getDataConsumerManifestById(uri);
            assertNotNull(dataConsumerManifestByUri);
            assertFalse(StringUtils.isEmpty(dataConsumerManifestByUri.getId()));
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
            assertFalse(StringUtils.isEmpty(dataConsumerManifestByMacAddress.getId()));
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
            dcm.setId("http://www.overit.it");
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
            dcm.setId("http://www.overit.it");
            dcm.setMacAddress("b8:e8:56:41:43:05");
            client.registerDCM(dcm);
            client.removeDCM(dcm.getId());
            DCM back = null;
            try {
                back = client.getDataConsumerManifestById(dcm.getId());
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
            dcm.setId("http://www.opcua.com");
            dcm.setMacAddress("b8:e8:56:41:43:08");
            client.registerDCM(dcm);
            DCM back = client.getDataConsumerManifestById(dcm.getId());
            assertEquals(dcm, back);
            client.editRegisteredDCM(dcm);
            DCM back2 = client.getDataConsumerManifestById(dcm.getId());
            assertNotEquals(back2, not(dcm));
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        }
    }


}
