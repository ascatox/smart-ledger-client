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

    /**
     * Installation function for the chaincode link @
     *
     * @param instantiate
     * @param upgrade
     * @throws Exception
     */
    @Override
    public void installChaincode(boolean instantiate, boolean upgrade) throws Exception {
        SmartLedgerClientHelper.installChaincode(channel);
        Util.out("Chaincode installed correctly!!!");
        if (instantiate) {
            instantiateOrUpgradeChaincode(upgrade);
        }
    }

    /**
     * @param isUpgrade
     * @throws Exception
     */
    @Override
    public void instantiateOrUpgradeChaincode(boolean isUpgrade) throws Exception {
        String[] args = {};
        CompletableFuture<BlockEvent.TransactionEvent> transactionEventCompletableFuture = SmartLedgerClientHelper
                .instantiateOrUpgradeChaincode(channel, args, isUpgrade);
        BlockEvent.TransactionEvent event = null;
        transactionEventCompletableFuture.thenAccept((transactionEvent) -> {
            if (isUpgrade)
                Util.out("Chaincode upgraded correctly :-)");
            else
                Util.out("Chaincode instantiated correctly :-)");
        }).exceptionally((error) -> {
            Util.fail(error.getMessage());
            return null;
        });
    }

    /**
     * @param uri
     * @return
     * @throws Exception
     */
    @Override
    public DSM getDataSourceManifestByUri(String uri) throws Exception {

        String[] args = {uri};
        final List<String[]> payloads = SmartLedgerClientHelper.queryChainCode(channel, "qGetDSMByUri", args);
        List<DSM> dsms = Util.extractDSMFromPayloads(payloads);
        if (dsms.isEmpty())
            Util.fail("No DSM retrieved from getDataSourceManifestByUri with URI: '" + uri + "'");
        return dsms.get(0);
    }

    /**
     * @param macAddress
     * @return
     * @throws Exception
     */
    @Override
    public DSM getDataSourceManifestByMacAddress(String macAddress) throws Exception {

        String[] args = {macAddress};
        final List<String[]> payloads = SmartLedgerClientHelper.queryChainCode(channel, "qGetDSMByMacAdd", args);
        List<DSM> dsms = Util.extractDSMFromPayloads(payloads);
        if (dsms.isEmpty())
            Util.fail("No DSM retrieved from getDataSourceManifestByUri with MAC Address: '" + macAddress + "'");
        return dsms.get(0);
    }

    /**
     * @param macAddress
     * @return
     * @throws Exception
     */
    @Override
    public DCM getDataConsumerManifestByMacAddress(String macAddress) throws Exception {

        String[] args = {macAddress};
        final List<String[]> payloads = SmartLedgerClientHelper.queryChainCode(channel, "qGetDCMByMacAdd", args);
        List<DCM> dcms = Util.extractDCMFromPayloads(payloads);
        if (dcms.isEmpty())
            Util.fail("No DSM retrieved from getDataSourceManifestByUri with MAC Address: '" + macAddress + "'");
        return dcms.get(0);
    }

    /**
     * @param uri
     * @return
     */
    @Override
    public DCM getDataConsumerManifestByUri(String uri) {
        String[] args = {uri};
        final List<String[]> payloads = SmartLedgerClientHelper.queryChainCode(channel, "qGetDCMByUri", args);
        List<DCM> dcms = Util.extractDCMFromPayloads(payloads);
        if (dcms.isEmpty())
            Util.fail("No DCM retrieved from getDataConsumerManifestByUri with URI: '" + uri + "'");
        return dcms.get(0);
    }

    /**
     * @return
     * @throws Exception
     */
    @Override
    public List<DSM> getAllDataSourceManifests() throws Exception {
        String[] args = {};
        final List<String[]> payloads = SmartLedgerClientHelper.queryChainCode(channel, "qGetAllDSMs", args);
        return Util.extractDSMFromPayloads(payloads);
    }

    /**
     * @return
     * @throws Exception
     */
    @Override
    public List<DCM> getAllDataConsumerManifests() throws Exception {
        String[] args = {};
        final List<String[]> payloads = SmartLedgerClientHelper.queryChainCode(channel, "qGetAllDCMs", args);
        return Util.extractDCMFromPayloads(payloads);
    }

    /**
     * @param dsm
     * @throws Exception
     */
    @Override
    public void registerDSM(DSM dsm) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(dsm);
        String[] args = {dsm.getPhysicalArtifact(), dsm.getUri(), dsm.getMacAddress(), dsm.getDsd(), dsm
                .getConnectionParameters()};
        BlockEvent.TransactionEvent event = null;
        SmartLedgerClientHelper.invokeChaincode(channel,
                "iCreateDSM", args).thenAccept(transactionEvent -> {
            Util.out("Register DSM completed succesfully ");
        }).exceptionally((error) -> {
            Util.fail(error.getMessage());
            return null;
        });
    }

    /**
     * @param dcm
     * @throws Exception
     */
    @Override
    public void registerDCM(DCM dcm) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(dcm);
        String[] args = {dcm.getPhysicalArtifact(), dcm.getUri(), dcm.getMacAddress(), dcm.getDsds()};
        BlockEvent.TransactionEvent event = null;
        SmartLedgerClientHelper.invokeChaincode(channel,
                "iCreateDCM", args).thenAccept(transactionEvent -> {
            Util.out("Register DCM completed succesfully ");
        }).exceptionally((error) -> {
            Util.fail(error.getMessage());
            return null;
        });
    }
}