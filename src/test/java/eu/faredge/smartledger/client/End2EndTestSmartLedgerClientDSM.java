/**
 * @author ascatox
 */
package eu.faredge.smartledger.client;

import eu.faredge.dm.dcm.DCM;
import eu.faredge.dm.dsm.DSM;
import eu.faredge.smartledger.client.base.ISmartLedgerClient;
import eu.faredge.smartledger.client.exception.SmartLedgerClientException;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

public class End2EndTestSmartLedgerClientDSM {

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
    public void testGetDataSourceManifestById() {
        try {
            String uri = "http://www.google.com";
            DSM dataSourceManifestByUri = client.getDataSourceManifestById(uri);
            assertNotNull(dataSourceManifestByUri);
            assertFalse(StringUtils.isEmpty(dataSourceManifestByUri.getId()));
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
            assertFalse(StringUtils.isEmpty(dataSourceManifestByMacAddress.getId()));
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
    public void testGetGetCompatibleDSM() {
        try {
            testRegisterDSM();
            List<DSM> allDSMs = client.getCompatibleDSM( testRegisterDCM());
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


    private DCM testRegisterDCM() {
        try {
            DCM dcm = new DCM();
            dcm.setId("http://www.overit.it");
            dcm.setMacAddress("b8:e8:56:41:43:05");
            client.registerDCM(dcm);
            List<DCM> all = client.getAllDataConsumerManifests();
            assertNotNull(all);
            assertFalse(all.isEmpty());
            return dcm;
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        }
        return null;
    }

    @Test
    public void testRegisterDSMById() {
        try {
            DSM dsm = new DSM();
            dsm.setId("http://www.mht.it");
            dsm.setMacAddress("b8:e8:56:41:43:05");
            setDataDefinitionParameters(dsm);
            client.registerDSM(dsm);
            DSM dsmBack = client.getDataSourceManifestById(dsm.getId());
            assertNotNull(dsmBack);
            assertFalse(StringUtils.isEmpty(dsmBack.getId()));
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        }
    }


    @Test
    public void testRemoveDSM() {
        try {
            DSM dsm = new DSM();
            dsm.setId("http://www.eng-mo.it");
            dsm.setMacAddress("b8:e8:56:41:43:07");
            client.registerDSM(dsm);
            client.removeDSM(dsm.getId());
            DSM dsmBack = null;
            try {
                dsmBack = client.getDataSourceManifestById(dsm.getId());
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
            dsm.setId("http://www.eng-mo.it");
            dsm.setMacAddress("b8:e8:56:41:43:07");
            setDataDefinitionParameters(dsm);
            client.registerDSM(dsm);
            DSM dsmBack = client.getDataSourceManifestById(dsm.getId());
            assertEquals(dsm, dsmBack);
            client.editRegisteredDSM(dsm);
            DSM dsmBack2 = client.getDataSourceManifestById(dsm.getId());
            assertNotEquals(dsmBack2, not(dsm));
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        }
    }

    private DSM initDSM() {
        DSM dsm = new DSM();
        dsm.setId("http://www.google.it");
        dsm.setMacAddress("b8:e8:56:41:43:06");
        setDataDefinitionParameters(dsm);
        return dsm;
    }

    private void setDataDefinitionParameters(DSM dsm) {
        DSM.DataSourceDefinitionParameters.Parameters parameters = new DSM.DataSourceDefinitionParameters
                .Parameters();
        parameters.setKey("connection");
        parameters.setValue("connectionVals:21121");
        DSM.DataSourceDefinitionParameters definitionParameters = new DSM.DataSourceDefinitionParameters();
        definitionParameters.getParameters().add(parameters);
        dsm.setDataSourceDefinitionParameters(definitionParameters);
    }


}
