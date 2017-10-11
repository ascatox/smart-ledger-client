package eu.faredge.smartledger.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.faredge.smartledger.client.base.ISmartLedgerClient;
import eu.faredge.smartledger.client.exception.SmartLedgerClientException;
import eu.faredge.smartledger.client.helper.SmartLedgerClientHelper;
import eu.faredge.smartledger.client.model.DCM;
import eu.faredge.smartledger.client.model.DSM;
import eu.faredge.smartledger.client.util.Util;
import eu.faredge.smartledger.client.util.Validator;
import org.apache.commons.lang3.StringUtils;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.Channel;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SmartLedgerClient implements ISmartLedgerClient {

    private SmartLedgerClientHelper helper;
    private Channel channel;
    private Validator validator;

    public SmartLedgerClient(String channelName) {
        helper = new SmartLedgerClientHelper();
        try {
            helper.checkConfig();
            helper.setup();
            channel = helper.constructChannel(channelName);
            validator = new Validator();
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
    public void installChaincode(boolean instantiate, boolean upgrade) throws SmartLedgerClientException {
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
    public void instantiateOrUpgradeChaincode(boolean isUpgrade) throws SmartLedgerClientException {
        String[] args = {};
        CompletableFuture<BlockEvent.TransactionEvent> transactionEventCompletableFuture = null;
        try {
            transactionEventCompletableFuture = SmartLedgerClientHelper
                    .instantiateOrUpgradeChaincode(channel, args, isUpgrade);
        } catch (Exception e) {
           Util.out(e.getMessage());
           throw new SmartLedgerClientException(e);
        }
        BlockEvent.TransactionEvent event = null;
        transactionEventCompletableFuture.thenAccept((transactionEvent) -> {
            if (isUpgrade)
                Util.out("Chaincode upgraded correctly :-)");
            else
                Util.out("Chaincode instantiated correctly :-)");
        }).exceptionally((error) -> {
            Util.out(error.getMessage());
            return null;
        });
    }

    /**
     * @param uri
     * @return
     * @throws Exception
     */
    @Override
    public DSM getDataSourceManifestByUri(String uri) throws SmartLedgerClientException {
        if (StringUtils.isEmpty(uri)) throw new IllegalArgumentException("Error in method getDataSourceManifestByUri " +
                "uri " +
                "cannot be empty");
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
    public DSM getDataSourceManifestByMacAddress(String macAddress) throws SmartLedgerClientException {
        if (StringUtils.isEmpty(macAddress))
            throw new IllegalArgumentException("Error in method getDataSourceManifestByMacAddress " +
                    "macAddress " +
                    "cannot be empty");
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
    public DCM getDataConsumerManifestByMacAddress(String macAddress) throws SmartLedgerClientException {
        if (StringUtils.isEmpty(macAddress))
            throw new IllegalArgumentException("Error in method getDataConsumerManifestByMacAddress " +
                    "macAddress " +
                    "cannot be empty");
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
    public DCM getDataConsumerManifestByUri(String uri) throws SmartLedgerClientException{
        if (StringUtils.isEmpty(uri))
            throw new IllegalArgumentException("Error in method getDataConsumerManifestByUri " +
                    "uri " +
                    "cannot be empty");
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
    public List<DSM> getAllDataSourceManifests() throws SmartLedgerClientException {
        String[] args = {};
        final List<String[]> payloads = SmartLedgerClientHelper.queryChainCode(channel, "qGetAllDSMs", args);
        return Util.extractDSMFromPayloads(payloads);
    }

    /**
     * @return
     * @throws Exception
     */
    @Override
    public List<DCM> getAllDataConsumerManifests() throws SmartLedgerClientException {
        String[] args = {};
        final List<String[]> payloads = SmartLedgerClientHelper.queryChainCode(channel, "qGetAllDCMs", args);
        return Util.extractDCMFromPayloads(payloads);
    }

    /**
     * @throws Exception
     */
    @Override
    public void registerDSM(DSM dsm) throws SmartLedgerClientException {
        validator.validateBean(dsm);
        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(dsm);
        } catch (JsonProcessingException e) {
            Util.fail("Error in json conversion! " + e.getMessage());
        }
        String[] args = {dsm.getPhysicalArtifact(), dsm.getUri(), dsm.getMacAddress(), dsm.getDsd(), dsm
                .getConnectionParameters()};
        BlockEvent.TransactionEvent event = null;
        try {
            SmartLedgerClientHelper.invokeChaincode(channel,
                    "iEditDSM", args).thenAccept(transactionEvent -> {
                Util.out("Register DSM completed successfully ");
            }).exceptionally((error) -> {
                Util.out(error.getMessage());
                return null;
            });
        } catch (Exception e) {
            Util.out(e.getMessage());
            throw new SmartLedgerClientException(e);
        }
    }

    /**
     * @param dcm
     * @throws Exception
     */
    @Override
    public void registerDCM(DCM dcm) throws SmartLedgerClientException {
        validator.validateBean(dcm);
        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(dcm);
        } catch (JsonProcessingException e) {
            Util.fail("Error in json conversion! " + e.getMessage());
        }
        String[] args = {dcm.getPhysicalArtifact(), dcm.getUri(), dcm.getMacAddress(), dcm.getDsds()};
        BlockEvent.TransactionEvent event = null;
        try {
            SmartLedgerClientHelper.invokeChaincode(channel,
                    "iEditDCM", args).thenAccept(transactionEvent -> {
                Util.out("Register DCM completed successfully ");
            }).exceptionally((error) -> {
                Util.fail(error.getMessage());
                return null;
            });
        } catch (Exception e) {
            Util.out(e.getMessage());
            throw new SmartLedgerClientException(e);
        }
    }

    @Override
    public void editRegisteredDSM(DSM dsm) throws SmartLedgerClientException {
        validator.validateBean(dsm);
        DSM dataSourceManifestByUri = null;
        try {
            dataSourceManifestByUri = getDataSourceManifestByUri(dsm.getUri());
        } catch (Exception e) {
            throw new SmartLedgerClientException(e);
        }
        if (dataSourceManifestByUri.isEmpty()) {
            Util.out("DSM not registered");
            throw new SmartLedgerClientException("DSM not registered");
        }
        registerDSM(dsm);
    }

    @Override
    public void editRegisteredDCM(DCM dcm) throws SmartLedgerClientException {
        validator.validateBean(dcm);
        DSM dataSourceManifestByUri = null;
        try {
            dataSourceManifestByUri = getDataSourceManifestByUri(dcm.getUri());
        } catch (Exception e) {
            throw new SmartLedgerClientException(e);
        }
        if (dataSourceManifestByUri.isEmpty()) {
            Util.out("DCM not registered");
            throw new SmartLedgerClientException("DCM not registered");
        }
        registerDCM(dcm);
    }

    /**
     * @param uri
     * @throws Exception
     */

    @Override
    public void removeDSM(String uri) throws SmartLedgerClientException {
        if (StringUtils.isEmpty(uri))
            throw new IllegalArgumentException("Error in method removeDSM " +
                    "uri " +
                    "cannot be empty");
        String[] args = {uri};
        BlockEvent.TransactionEvent event = null;
        try {
            SmartLedgerClientHelper.invokeChaincode(channel,
                    "iRemoveDSM", args).thenAccept(transactionEvent -> {
                Util.out("Remove DSM completed successfully  with uri: " + uri);
            }).exceptionally((error) -> {
                Util.fail(error.getMessage());
                return null;
            });
        } catch (Exception e) {
            Util.out(e.getMessage());
            throw new SmartLedgerClientException(e.getMessage());
        }
    }

    /**
     * @param uri
     * @throws Exception
     */
    @Override
    public void removeDCM(String uri) throws SmartLedgerClientException {
        if (StringUtils.isEmpty(uri))
            throw new IllegalArgumentException("Error in method removeDCM " +
                    "uri " +
                    "cannot be empty");
        String[] args = {uri};
        BlockEvent.TransactionEvent event = null;
        try {
            SmartLedgerClientHelper.invokeChaincode(channel,
                    "iRemoveDCM", args).thenAccept(transactionEvent -> {
                Util.out("Remove DCM completed successfully  with uri: " + uri);
            }).exceptionally((error) -> {
                Util.fail(error.getMessage());
                return null;
            });
        } catch (Exception e) {
            Util.out(e.getMessage());
            throw new SmartLedgerClientException(e.getMessage());
        }
    }


}//end Class