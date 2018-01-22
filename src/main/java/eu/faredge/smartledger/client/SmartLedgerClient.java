package eu.faredge.smartledger.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.faredge.dm.dcd.DCD;
import eu.faredge.smartledger.client.base.ISmartLedgerClient;
import eu.faredge.smartledger.client.exception.SmartLedgerClientException;
import eu.faredge.smartledger.client.helper.SmartLedgerClientHelper;
import eu.faredge.dm.dcm.DCM;
import eu.faredge.dm.dsm.DSM;
import eu.faredge.smartledger.client.model.SampleOrg;
import eu.faredge.smartledger.client.testutils.TestConfig;
import eu.faredge.smartledger.client.util.Util;
import eu.faredge.smartledger.client.util.Validator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.Channel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class SmartLedgerClient implements ISmartLedgerClient {

    public static final int TIMEOUT = TestConfig.TIMEOUT;
    private SmartLedgerClientHelper helper;
    private Channel channel;
    private Validator validator; //TODO
    private static List<SampleOrg> sampleOrgs;
    private static final TestConfig testConfig = TestConfig.getConfig();

    public SmartLedgerClient() {
        doSmartLedgerClient(SmartLedgerClientHelper.CHANNEL_NAME, null, null);
    }

    public SmartLedgerClient(String username) {
        doSmartLedgerClient(SmartLedgerClientHelper.CHANNEL_NAME, username, null);
    }

    public SmartLedgerClient(String channelName, String username) {
        doSmartLedgerClient(channelName, username, null);
    }

    public SmartLedgerClient(String channelName, String username, String enrollmentSecret) {
        doSmartLedgerClient(channelName, username, enrollmentSecret);
    }

    private void doSmartLedgerClient(String channelName, String username, String enrollmentSecret) {
        try {
            validator = new Validator(); //TODO
            sampleOrgs = new ArrayList<>();
            helper = new SmartLedgerClientHelper();
            sampleOrgs.addAll(testConfig.getIntegrationTestsSampleOrgs());
            for (SampleOrg sampleOrg : sampleOrgs) {
                helper.checkConfig(sampleOrg);
                helper.setup(sampleOrg, username, enrollmentSecret);
                channel = helper.initializeChannel(channelName, sampleOrg);
            }
        } catch (Exception e) {
            Util.fail(e.getMessage());
        }
    }


    /**
     * @param instantiate
     * @param upgrade
     * @throws Exception
     * @exclude Installation function for the chaincode link @
     */
    //@Override
    public void installChaincode(boolean instantiate, boolean upgrade) throws SmartLedgerClientException {
        SmartLedgerClientHelper.installChaincode(channel, sampleOrgs.get(0)); //TODO Only the first
        Util.out("Chaincode installed correctly!!!");
        if (instantiate) {
            instantiateOrUpgradeChaincode(upgrade);
        }
    }

    /**
     * @param isUpgrade
     * @throws Exception
     * @exclude
     */
    //@Override
    public void instantiateOrUpgradeChaincode(boolean isUpgrade) throws SmartLedgerClientException {
        String[] args = {};
        CompletableFuture<BlockEvent.TransactionEvent> transactionEventCompletableFuture = null;
        try {
            transactionEventCompletableFuture = SmartLedgerClientHelper
                    .instantiateOrUpgradeChaincode(channel, args, isUpgrade);

            BlockEvent.TransactionEvent event = null;
            transactionEventCompletableFuture.get(TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            Util.out(e.getMessage());
        } catch (Exception e) {
            Util.out(e.getMessage());
            throw new SmartLedgerClientException(e);
        }
    }

    /**
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    public DSM getDataSourceManifestById(String id) throws SmartLedgerClientException {
        if (StringUtils.isEmpty(id)) throw new IllegalArgumentException("Error in method getDataSourceManifestById " +
                "id " +
                "cannot be empty");
        String[] args = {id};
        final List<String[]> payloads = SmartLedgerClientHelper.queryChainCode(channel, "qGetDSMByUri", args);
        List<DSM> dsms = Util.extractDSMFromPayloads(payloads);
        if (dsms.isEmpty())
            Util.fail("No DSM retrieved from getDataSourceManifestByUri with URI: '" + id + "'");
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
     * @param id
     * @return
     */
    @Override
    public DCM getDataConsumerManifestById(String id) throws SmartLedgerClientException {
        if (StringUtils.isEmpty(id))
            throw new IllegalArgumentException("Error in method getDataConsumerManifestById " +
                    "id " +
                    "cannot be empty");
        String[] args = {id};
        final List<String[]> payloads = SmartLedgerClientHelper.queryChainCode(channel, "qGetDCMByUri", args);
        List<DCM> dcms = Util.extractDCMFromPayloads(payloads);
        if (dcms.isEmpty())
            Util.fail("No DCM retrieved from getDataConsumerManifestByUri with URI: '" + id + "'");
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

    @Override
    public List<DSM> getCompatibleDSM(DCM dcm) throws SmartLedgerClientException {
        if (null == dcm || null == dcm.getDataSourceDefinitionsIDs() || dcm.getDataSourceDefinitionsIDs().size() == 0)
            throw new IllegalArgumentException("Error in method getAllDataSourceManifestsByDCM " +
                    "list of dsds  " +
                    "cannot be empty or null");
        final List<String[]> payloads = SmartLedgerClientHelper.queryChainCode(channel, "qGetAllDSMSByDsds", dcm
                .getDataSourceDefinitionsIDs()
                .toArray(new String[dcm.getDataSourceDefinitionsIDs().size()]));
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
    public String registerDSM(DSM dsm) throws SmartLedgerClientException {
        validator.validateBean(dsm); //TODO
        ObjectMapper mapper = new ObjectMapper();
        String json = null;
        try {
            json = mapper.writeValueAsString(dsm.getDataSourceDefinitionParameters());
        } catch (JsonProcessingException e) {
            Util.fail("Error in json conversion! " + e.getMessage());
        }
        String[] args = {dsm.getId(), dsm.getMacAddress(), dsm.getDataSourceDefinitionID(), json};
        BlockEvent.TransactionEvent event = null;
        try {
            SmartLedgerClientHelper.invokeChaincode(channel,
                    "iEditDSM", args).get(TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException | ConcurrentException e) {
            Util.out(e.getMessage());
        } catch (Exception e) {
            Util.out(e.getMessage());
            throw new SmartLedgerClientException(e);
        }
        return dsm.getId();
    }

    /**
     * @param dcm
     * @throws Exception
     */
    @Override
    public String registerDCM(DCM dcm) throws SmartLedgerClientException {
        validator.validateBean(dcm); //TODO
        String json = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            json = mapper.writeValueAsString(dcm);
        } catch (JsonProcessingException e) {
            Util.fail("Error in json conversion! " + e.getMessage());
        }
        String[] args = {dcm.getId(), dcm.getMacAddress(), dcm.getDataSourceDefinitionsIDs().stream().collect
                (Collectors.joining(";"))};
        BlockEvent.TransactionEvent event = null;
        try {
            SmartLedgerClientHelper.invokeChaincode(channel,
                    "iEditDCM", args)
                    .get(TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException | ConcurrentException e) {
            Util.out(e.getMessage());
        } catch (Exception e) {
            Util.out(e.getMessage());
            throw new SmartLedgerClientException(e);
        }
        return dcm.getId();
    }

    @Override
    public void editRegisteredDSM(DSM dsm) throws SmartLedgerClientException {
        validator.validateBean(dsm); //TODO
        DSM dataSourceManifestByUri = null;
        try {
            dataSourceManifestByUri = getDataSourceManifestById(dsm.getId());
        } catch (Exception e) {
            throw new SmartLedgerClientException(e);
        }
        if (StringUtils.isEmpty(dataSourceManifestByUri.getId())) {
            Util.out("DSM not registered");
            throw new SmartLedgerClientException("DSM not registered");
        }
        registerDSM(dsm);
    }

    @Override
    public void editRegisteredDCM(DCM dcm) throws SmartLedgerClientException {
        validator.validateBean(dcm); //TODO
        DSM dataSourceManifestByUri = null;
        try {
            dataSourceManifestByUri = getDataSourceManifestById(dcm.getId());
        } catch (Exception e) {
            throw new SmartLedgerClientException(e);
        }
        if (StringUtils.isEmpty(dataSourceManifestByUri.getId())) {
            Util.out("DCM not registered");
            throw new SmartLedgerClientException("DCM not registered");
        }
        registerDCM(dcm);
    }

    /**
     * @param id
     * @throws Exception
     */

    @Override
    public void removeDSM(String id) throws SmartLedgerClientException {
        if (StringUtils.isEmpty(id))
            throw new IllegalArgumentException("Error in method removeDSM " +
                    "id " +
                    "cannot be empty");
        String[] args = {id};
        BlockEvent.TransactionEvent event = null;
        try {
            SmartLedgerClientHelper.invokeChaincode(channel,
                    "iRemoveDSM", args).get(TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException | ConcurrentException e) {
            Util.out(e.getMessage());
        } catch (Exception e) {
            Util.out(e.getMessage());
            throw new SmartLedgerClientException("Error removing DSM: " + e.getMessage());
        }
    }

    /**
     * @param id
     * @throws Exception
     */
    @Override
    public void removeDCM(String id) throws SmartLedgerClientException {
        if (StringUtils.isEmpty(id))
            throw new IllegalArgumentException("Error in method removeDCM " +
                    "id " +
                    "cannot be empty");
        String[] args = {id};
        BlockEvent.TransactionEvent event = null;
        try {
            SmartLedgerClientHelper.invokeChaincode(channel,
                    "iRemoveDCM", args).get(TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException | ConcurrentException e) {
            Util.out(e.getMessage());
        } catch (Exception e) {
            Util.out(e.getMessage());
            throw new SmartLedgerClientException("Error removing DCM " + e.getMessage());
        }
    }

    @Override
    public String registerDCD(DCD dcd) throws SmartLedgerClientException {
        validator.validateBean(dcd); //TODO
        String json = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            json = mapper.writeValueAsString(dcd);
        } catch (JsonProcessingException e) {
            Util.fail("Error in json conversion! " + e.getMessage());
        }
        String[] args = {dcd.getExpirationDateTime().toString(), dcd.getValidFrom().toString(), dcd
                .getDataSourceManifestID(), dcd.getDataConsumerManifestID(), dcd.getId()};
        BlockEvent.TransactionEvent event = null;
        try {
            SmartLedgerClientHelper.invokeChaincode(channel,
                    "iEditDCD", args)
                    .get(TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException | ConcurrentException e) {
            Util.out(e.getMessage());
        } catch (Exception e) {
            Util.out(e.getMessage());
            throw new SmartLedgerClientException(e);
        }
        return dcd.getId();
    }

    @Override
    public void removeDCD(String id) throws SmartLedgerClientException {
        if (StringUtils.isEmpty(id))
            throw new IllegalArgumentException("Error in method removeDCD " +
                    "id " +
                    "cannot be empty");
        String[] args = {id};
        BlockEvent.TransactionEvent event = null;
        try {
            SmartLedgerClientHelper.invokeChaincode(channel,
                    "iRemoveDCD", args).get(TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException | ConcurrentException e) {
            Util.out(e.getMessage());
        } catch (Exception e) {
            Util.out(e.getMessage());
            throw new SmartLedgerClientException("Error removing DCD " + e.getMessage());
        }
    }


}//end Class