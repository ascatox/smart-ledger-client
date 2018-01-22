/**
 * @author ascatox
 */
package eu.faredge.smartledger.client;

import eu.faredge.dm.dcd.DCD;
import eu.faredge.dm.dcm.DCM;
import eu.faredge.dm.dsm.DSM;
import eu.faredge.smartledger.client.base.ISmartLedgerClient;
import eu.faredge.smartledger.client.exception.SmartLedgerClientException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertFalse;

public class End2EndTestSmartLedgerClientDCD {

    static ISmartLedgerClient client = null;

    @Mock
    private DCD dcd;

    @BeforeClass
    public static void begin() {
        client = new SmartLedgerClient();
    }

    @AfterClass
    public static void end() {
        client = null;
    }

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRegisterDCD() {
        try {
            //TODO Inserimento DSM a DCM
            client.registerDCD(dcd);
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        }
    }

    @Test
    public void testRemoveDCD() {
        try {
            client.registerDCD(dcd);
            client.removeDCM(dcd.getId());
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        }
    }



}
