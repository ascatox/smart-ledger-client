package eu.faredge.smartledger.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.faredge.smartledger.client.base.ISmartLedgerClient;
import eu.faredge.smartledger.client.helper.SmartLedgerClientHelper;
import eu.faredge.smartledger.client.model.DCM;
import eu.faredge.smartledger.client.model.DSM;
import eu.faredge.smartledger.client.util.Util;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.Channel;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SmartLedgerClient implements ISmartLedgerClient {

    public static final int TIMEOUT = 360;
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
    public void installChaincode(boolean instantiate, boolean upgrade) throws Exception {
        SmartLedgerClientHelper.installChaincode(channel);
        Util.out("Chaincode installed correctly!!!");
        if (instantiate) {
            instantiateOrUpgradeChaincode(upgrade);
        }
    }

    @Override
    public void instantiateOrUpgradeChaincode(boolean isUpgrade) throws Exception {
        String[] args = {};
        CompletableFuture<BlockEvent.TransactionEvent> transactionEventCompletableFuture = SmartLedgerClientHelper
                .instantiateOrUpgradeChaincode(channel, args, isUpgrade);
        BlockEvent.TransactionEvent event = null;
        transactionEventCompletableFuture.complete(null);
        if (isUpgrade)
            Util.out("Chaincode upgraded correctly :-)");
        else
            Util.out("Chaincode instantiated correctly :-)");

    }

    @Override
    public DSM getDataSourceManifest(String id) throws Exception {
        String[] args = {id};
        final List<String[]> payloads = SmartLedgerClientHelper.queryChainCode(channel, "qGetDSMByUri", args);
        Util.out("Query Chaincode successful!!!Data retrieved: ");
        //TODO
        return Util.extractDSMFromPayloads(payloads).get(0);
    }

    @Override
    public DCM getDataConsumerManifest(String id) {
        String[] args = {id};
        final List<String[]> payloads = SmartLedgerClientHelper.queryChainCode(channel, "qGetDCMByUri", args);
        Util.out("Query Chaincode successful!!!Data retrieved: ");
        //TODO
        return Util.extractDCMFromPayloads(payloads).get(0);
    }


    @Override
    public List<DSM> getAllDataSourceManifests() throws Exception {
        String[] args = {};
        final List<String[]> payloads = SmartLedgerClientHelper.queryChainCode(channel, "qGetAllDSMs", args);
        Util.out("Query Chaincode successful!!!Data retrieved: ");
        return Util.extractDSMFromPayloads(payloads);
    }

    @Override
    public List<DCM> getAllDataConsumerManifests() throws Exception {
        String[] args = {};
        final List<String[]> payloads = SmartLedgerClientHelper.queryChainCode(channel, "qGetAllDCMs", args);
        Util.out("Query Chaincode successful!!!Data retrieved: ");
        return Util.extractDCMFromPayloads(payloads);
    }

    @Override
    public void registerDSM(DSM dsm) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(dsm);
        String[] args = {dsm.getPhysicalArtifact(), dsm.getUri(), dsm.getMacAddress(), dsm.getDsd(), dsm
                .getConnectionParameters()};
        BlockEvent.TransactionEvent event = null;
        SmartLedgerClientHelper.invokeChaincode(channel,
                "iCreateDSM", args).complete(null);
    }

    @Override
    public void registerDCM(DCM dcm) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(dcm);
        String[] args = {dcm.getPhysicalArtifact(), dcm.getUri(), dcm.getMacAddress(), dcm.getDsds()};
        BlockEvent.TransactionEvent event = null;
        SmartLedgerClientHelper.invokeChaincode(channel,
                "iCreateDCM", args).complete(null);
    }



}
