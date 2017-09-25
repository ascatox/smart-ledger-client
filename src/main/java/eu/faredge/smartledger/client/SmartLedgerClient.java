package eu.faredge.smartledger.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.faredge.smartledger.client.helper.SmartLedgerClientHelper;
import eu.faredge.smartledger.client.model.DCM;
import eu.faredge.smartledger.client.model.DSM;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.Channel;

import java.util.List;

public class SmartLedgerClient implements eu.faredge.smartledger.client.base.SmartLedgerClient {

    private SmartLedgerClientHelper helper;
    private Channel channel;


    public SmartLedgerClient(String channelName) {
        helper = new SmartLedgerClientHelper();
        try {
            helper.checkConfig();
            helper.setup();
            channel = helper.constructChannel(channelName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String register(DSM dsm) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(dsm);
        String[] args = {"createDevice", json};
        BlockEvent.TransactionEvent transactionEvent = SmartLedgerClientHelper.invokeChaincode(channel, args, null).get();
        channel.shutdown(false);
        return null;
    }

    @Override
    public String register(DCM dcm) throws Exception {
        return null;
    }

    @Override
    public DSM getDataSourceManifest(String id) throws Exception {
        return null;
    }

    @Override
    public DCM getDataConsumerManifest(String id) {
        return null;
    }


    @Override
    public List<String[]> getAllDataSourceManifests() throws Exception {
        String[] args = {};
        final List<String[]> values = SmartLedgerClientHelper.queryChainCode(channel, "queryAllCars", args);
        if (null == values || values.isEmpty())
            throw new Exception("Value from chaincode is Empty");
        return values;
    }
}
