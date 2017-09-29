package eu.faredge.smartledger.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.faredge.smartledger.client.helper.SmartLedgerClientHelper;
import eu.faredge.smartledger.client.model.DCM;
import eu.faredge.smartledger.client.model.DSM;
import eu.faredge.smartledger.client.util.Util;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.Channel;

import java.util.List;
import java.util.concurrent.CompletableFuture;

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
        String[] args = {"iCreateDSM", dsm.getPhysicalArtifact(), dsm.getUri(), dsm.getMacAddress(), dsm.getDsd(), dsm
                .getConnectionParameters()};
        if (SmartLedgerClientHelper.invokeChaincode(channel,
                "", args).isDone())
            channel.shutdown(false);
        return null;
    }

    public void installChaincode(boolean instantiate) throws Exception {
        SmartLedgerClientHelper.installChaincode(channel);
        Util.out("Chaincode installed correctly!!!");
        if (instantiate) {
            instantiateChaincode();
        }
    }

    public void instantiateChaincode() throws Exception {
        String[] args = {};
        CompletableFuture<BlockEvent.TransactionEvent> transactionEventCompletableFuture = SmartLedgerClientHelper
                .instantiateChaincode(channel, args);
        if (transactionEventCompletableFuture.isDone())
            Util.out("Chaincode instantiated correctly!!!");
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
        return doGetAllDataSourceManifests();
    }

    private List<String[]> doGetAllDataSourceManifests() {
        String[] args = {"qGetAllDSMs"};
        final List<String[]> values = SmartLedgerClientHelper.queryChainCode(channel, "", args);
        Util.out("Query Chaincode successful!!!Data retrieved", values.toString());
        //TODO JSON Unmarshall to have a list of DSM objects!!!
        return values;
    }
}
