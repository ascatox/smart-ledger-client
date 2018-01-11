/**
 * @author ascatox
 */
package eu.faredge.smartledger.client;

import eu.faredge.smartledger.client.base.ISmartLedgerClient;
import eu.faredge.smartledger.client.exception.SmartLedgerClientException;
import org.junit.BeforeClass;

public class End2EndTestInstallChaincode {

    static ISmartLedgerClient client = null;

    @BeforeClass
    public static void begin() {
        client = new SmartLedgerClient();
        try {
            SmartLedgerClient smartLedgerClient = (SmartLedgerClient) client;
            smartLedgerClient.installChaincode(true, false);
        } catch (SmartLedgerClientException e) {
            System.err.println(e.getMessage());
        }

    }

}
