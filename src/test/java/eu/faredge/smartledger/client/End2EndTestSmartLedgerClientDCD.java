/**
 * @author ascatox
 */
package eu.faredge.smartledger.client;

import eu.faredge.dm.dcd.DCD;
import eu.faredge.dm.dcm.DCM;
import eu.faredge.dm.dsm.DSM;
import eu.faredge.smartledger.client.base.ISmartLedgerClient;
import eu.faredge.smartledger.client.exception.SmartLedgerClientException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class End2EndTestSmartLedgerClientDCD {

    static ISmartLedgerClient client = null;
    private static final Log logger = LogFactory.getLog(End2EndTestSmartLedgerClientDCD.class);
    private static List<DSM> dsmsToRemove = new ArrayList<>();
    private static List<DCM> dcmsToRemove = new ArrayList<>();


    @BeforeClass
    public static void begin() {
        client = new SmartLedgerClient();
    }

    @AfterClass
    public static void end() {
        client = null;
    }


    //@Test
    public void testRegisterDCD() {
        DCD dcd = null;
        try {
            dcd = doRegisterDCD();
          /*  DCM dataConsumerManifestById = client.getDataConsumerManifestById(dcm.getId());
            assertNotNull(dataConsumerManifestById);
            assertFalse(dataConsumerManifestById.getId().isEmpty());
            assertFalse(dataConsumerManifestById.getDataSourceDefinitionsIDs().isEmpty());*/
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        } catch (DatatypeConfigurationException e) {
            assertFalse(e.getMessage(), true);
        } finally {
            doRemoveDCD(dcd);
        }
    }


    //@Test
    public void testRemoveDCD() {
        try {
            DCD dcd = doRegisterDCD();
            doRemoveDCD(dcd);
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        } catch (DatatypeConfigurationException e) {
            assertFalse(e.getMessage(), true);
        }
    }

    public static DCD init() throws SmartLedgerClientException, DatatypeConfigurationException {
        Random random = new Random();
        DCD dcd = new DCD();
        GregorianCalendar now = new GregorianCalendar();
        XMLGregorianCalendar validFromNow = DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
        GregorianCalendar cal = new GregorianCalendar();
        cal.add(Calendar.HOUR, 1);
        XMLGregorianCalendar expiration = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
        dcd.setValidFrom(validFromNow);
        dcd.setExpirationDateTime(expiration);
        dcd.setId("channel://plant_station_" + Math.abs(random.nextInt(1000)));
        DCM dcm = doRegisterDCM();
        dcd.setDataConsumerManifestID(dcm.getId());
        dcmsToRemove.add(dcm);
        DSM dsm = doRegisterDSM();
        dcd.setDataSourceManifestID(dsm.getId());
        dsmsToRemove.add(dsm);
        return dcd;
    }

    private DCD doRegisterDCD() throws DatatypeConfigurationException, SmartLedgerClientException {
        DCD dcd = init();
        client.registerDCD(dcd);
        return dcd;
    }

    private void doRemoveDCD(DCD dcd) {
        try {
            client.removeDCD(dcd.getId());
        } catch (SmartLedgerClientException e) {
            logger.error(e);
        }
    }

    public static DCM initDCM() throws SmartLedgerClientException {
        Random random = new Random();
        DCM dcm = new DCM();
        dcm.setId("device://station_" + Math.abs(random.nextInt(1000)));
        dcm.setMacAddress("f8:d8:53:21:32:09:" + Math.abs(random.nextInt(100)));
        DSM dsm = doRegisterDSM();
        dcm.getDataSourceDefinitionsIDs().add(dsm.getDataSourceDefinitionID());
        return dcm;
    }

    public static DCM doRegisterDCM() throws SmartLedgerClientException {
        DCM dcm = initDCM();
        client.registerDCM(dcm);
        return dcm;
    }

    private void doRemoveDCM(DCM dcm) {
        try {
            client.removeDCM(dcm.getId());
        } catch (SmartLedgerClientException e) {
            logger.error(e);
        }
    }

    public static DSM doRegisterDSM() throws SmartLedgerClientException {
        DSM dsm = End2EndTestSmartLedgerClientDSM.init();
        client.registerDSM(dsm);
        return dsm;
    }

    public static void doRemoveDSM(DSM dsm) {
        try {
            client.removeDSM(dsm.getId());
        } catch (SmartLedgerClientException e) {
            logger.error(e);
        }
    }

    @After
    public void tearDown() {
        try {
            for (DCM dcm : dcmsToRemove) {
                doRemoveDCM(dcm);
            }
            for (DSM dsm : dsmsToRemove) {
                doRemoveDSM(dsm);
            }
        } catch (Exception e) {
            logger.warn("Final DSM Cleaning...\n");
            logger.warn(e);
        }
    }


}
