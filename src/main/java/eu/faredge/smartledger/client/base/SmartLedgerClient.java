package eu.faredge.smartledger.client.base;

import eu.faredge.smartledger.client.model.DCM;
import eu.faredge.smartledger.client.model.DSM;
import org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException;

import java.net.MalformedURLException;
import java.util.List;

public interface SmartLedgerClient {


    String register(DSM dsm) throws Exception;

    String register(DCM dcm) throws Exception;

    DSM getDataSourceManifest(String id) throws MalformedURLException, InvalidArgumentException, IllegalAccessException, NoSuchFieldException, Exception;

    DCM getDataConsumerManifest(String id);

    List<String[]> getAllDataSourceManifests() throws Exception;

    // List<Device> getAllDevices();


}
