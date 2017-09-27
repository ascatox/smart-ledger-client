package eu.faredge.smartledger.client.helper;

import eu.faredge.smartledger.client.model.SampleOrg;
import eu.faredge.smartledger.client.model.SampleStore;
import eu.faredge.smartledger.client.model.SampleUser;
import eu.faredge.smartledger.client.testutils.TestConfig;
import eu.faredge.smartledger.client.util.Util;
import org.apache.commons.codec.binary.Hex;
import org.hyperledger.fabric.protos.ledger.rwset.kvrwset.KvRwset;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.*;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hyperledger.fabric.sdk.BlockInfo.EnvelopeType.TRANSACTION_ENVELOPE;

public class SmartLedgerClientHelper {
    private static ResourceBundle finder = ResourceBundle.getBundle("smart-ledger");
    private static final TestConfig testConfig = TestConfig.getConfig();
    private static final String TEST_ADMIN_NAME = finder.getString("TEST_ADMIN_NAME");
    private static final String TESTUSER_1_NAME = finder.getString("TESTUSER_1_NAME");
    private static final String TEST_FIXTURES_PATH = finder.getString("TEST_FIXTURES_PATH");

    private static final String CHAIN_CODE_NAME = finder.getString("CHAIN_CODE_NAME");
    private static final String CHAIN_CODE_PATH = finder.getString("CHAIN_CODE_PATH");
    private static final String CHAIN_CODE_VERSION = finder.getString("CHAIN_CODE_VERSION");

    private static final String FOO_CHANNEL_NAME = finder.getString("FOO_CHANNEL_NAME");

    private static final byte[] EXPECTED_EVENT_DATA = "!".getBytes(UTF_8);
    private static final String EXPECTED_EVENT_NAME = "event";

    private static String testTxID = null;  // save the CC invoke TxID and use in queries

    private static Collection<SampleOrg> testSampleOrgs;
    private static ChaincodeID chaincodeID = null;
    private static final int DELTA = 100;
    private static HFClient client = null;
    private static SampleOrg sampleOrg = null;
    private static Collection<Orderer> orderers = null;

    private static final Map<String, String> TX_EXPECTED;

    static {
        TX_EXPECTED = new HashMap<>();
        TX_EXPECTED.put("readset1", "Missing readset for channel bar block 1");
        TX_EXPECTED.put("writeset1", "Missing writeset for channel bar block 1");
    }


    public static void checkConfig() throws NoSuchFieldException, SecurityException, IllegalArgumentException,
            IllegalAccessException, MalformedURLException, org.hyperledger
            .fabric_ca.sdk.exception.InvalidArgumentException {
        Util.out("\n\n\nRUNNING: SmartLedgerClient.\n");

        chaincodeID = ChaincodeID.newBuilder().setName(CHAIN_CODE_NAME)
                .setVersion(CHAIN_CODE_VERSION)
                .setPath(CHAIN_CODE_PATH).build();

        testSampleOrgs = testConfig.getIntegrationTestsSampleOrgs();
        //Set up hfca for each sample org

        for (SampleOrg sampleOrg : testSampleOrgs) {
            String caName = sampleOrg.getCAName(); //Try one of each name and no name.
            if (caName != null && !caName.isEmpty()) {
                sampleOrg.setCAClient(HFCAClient.createNewInstance(caName, sampleOrg.getCALocation(), sampleOrg
                        .getCAProperties()));
            } else {
                sampleOrg.setCAClient(HFCAClient.createNewInstance(sampleOrg.getCALocation(), sampleOrg
                        .getCAProperties()));
            }
        }
    }


    public static void setup() {
        try {

            ////////////////////////////
            // Setup client

            //Create instance of client.
            client = HFClient.createNewInstance();
            client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());

            // client.setMemberServices(peerOrg1FabricCA);

            ////////////////////////////
            //Set up USERS

            //Persistence is not part of SDK. Sample file store is for demonstration purposes only!
            //   MUST be replaced with more robust application implementation  (Database, LDAP)
            File sampleStoreFile = new File(System.getProperty("java.io.tmpdir") + "/HFCSampletest.properties");
            if (sampleStoreFile.exists()) { //For testing start fresh
                sampleStoreFile.delete();
            }

            final SampleStore sampleStore = new SampleStore(sampleStoreFile);
            //  sampleStoreFile.deleteOnExit();

            //SampleUser can be any implementation that implements org.hyperledger.fabric.sdk.User Interface

            ////////////////////////////
            // get users for all orgs

            for (SampleOrg sampleOrg : testSampleOrgs) {

                HFCAClient ca = sampleOrg.getCAClient();

                final String orgName = sampleOrg.getName();
                final String mspid = sampleOrg.getMSPID();
                ca.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());

                SampleUser admin = sampleStore.getMember(TEST_ADMIN_NAME, orgName);
                if (!admin.isEnrolled()) {  //Preregistered admin only needs to be enrolled with Fabric caClient.
                    admin.setEnrollment(ca.enroll(admin.getName(), "adminpw"));
                    admin.setMspId(mspid);
                }
                sampleOrg.setAdmin(admin); // The admin of this org --

                SampleUser user = sampleStore.getMember(TESTUSER_1_NAME, sampleOrg.getName());
//TODO @ascatox Understand how to mark an User as registered and enrolled
//                if (!user.isRegistered()) {  // users need to be registered AND enrolled
//                    RegistrationRequest rr = new RegistrationRequest(user.getName(), "org1.department1");
//                    user.setEnrollmentSecret(ca.register(rr, admin));
//                }
//                if (!user.isEnrolled()) {
//                    user.setEnrollment(ca.enroll(user.getName(), user.getEnrollmentSecret()));
//                    user.setMspId(mspid);
//                }
                sampleOrg.addUser(user); //Remember user belongs to this Org

                final String sampleOrgName = sampleOrg.getName();
                final String sampleOrgDomainName = sampleOrg.getDomainName();

                SampleUser peerOrgAdmin = sampleStore.getMember(sampleOrgName + "Admin", sampleOrgName, sampleOrg
                                .getMSPID(),
                        Util.findFileSk(Paths.get(testConfig.getTestChannelPath(),
                                "crypto-config-fabcar/peerOrganizations/",
                                sampleOrgDomainName, format("/users/Admin@%s/msp/keystore", sampleOrgDomainName))
                                .toFile()),
                        Paths.get(testConfig.getTestChannelPath(), "crypto-config-fabcar/peerOrganizations/",
                                sampleOrgDomainName,
                                format("/users/Admin@%s/msp/signcerts/Admin@%s-cert.pem", sampleOrgDomainName,
                                        sampleOrgDomainName)).toFile());

                sampleOrg.setPeerAdmin(peerOrgAdmin); //A special user that can create channels, join peers and
                // install chaincode
            }
            sampleOrg = testConfig.getIntegrationTestsSampleOrg("peerOrg1"); //TODO
        } catch (Exception e) {
            e.printStackTrace();
            Util.fail(e.getMessage());
        }
    }

//    private static void runChannel(HFClient client, Channel channel, boolean installChaincode, boolean
// instantiateChaincode, SampleOrg sampleOrg, int delta) {
//
//        try {
//            Collection<ProposalResponse> successful = new LinkedList<>();
//            Collection<ProposalResponse> failed = new LinkedList<>();
//            installChaincode(client, channel, sampleOrg, successful, failed);
//            instantiateChaincode(client, channel, new String[]{}, successful, failed, true, orderers).thenApply
// (transactionEvent ->
//            {
//                waitOnFabric(0);
//                Util.out("Finished instantiate transaction with transaction id %s", transactionEvent
// .getTransactionID());
//                return invokeChaincode(client, channel, sampleOrg, successful, failed);
//            }).thenApply(transactionEvent -> {
//                //return queryChainCode(client, channel, "invoke", chaincodeID, transactionEvent);
//                return null;
//            }).exceptionally(e -> {
//                if (e instanceof TransactionEventException) {
//                    BlockEvent.TransactionEvent te = ((TransactionEventException) e).getTransactionEvent();
//                    if (te != null) {
//                        Util.fail(format("Transaction with txid %s failed. %s", te.getTransactionID(), e.getMessage
// ()));
//                    }
//                }
//                Util.fail(format("Test failed with %s exception %s", e.getClass().getName(), e.getMessage()));
//
//                return null;
//            }).get(testConfig.getTransactionWaitTime(), TimeUnit.SECONDS);
//
//            // Channel queries
//            // We can only send channel queries to peers that are in the same org as the SDK user context
//            // Get the peers from the current org being used and pick one randomly to send the queries to.
//            Set<Peer> peerSet = sampleOrg.getPeers();
//            //  Peer queryPeer = peerSet.iterator().next();
//            //   out("Using peer %s for channel queries", queryPeer.getName());
//
//            BlockchainInfo channelInfo = channel.queryBlockchainInfo();
//            Util.out("Channel info for : " + channel.getName());
//            Util.out("Channel height: " + channelInfo.getHeight());
//            String chainCurrentHash = Hex.encodeHexString(channelInfo.getCurrentBlockHash());
//            String chainPreviousHash = Hex.encodeHexString(channelInfo.getPreviousBlockHash());
//            Util.out("Chain current block hash: " + chainCurrentHash);
//            Util.out("Chainl previous block hash: " + chainPreviousHash);
//
//            // Query by block number. Should return latest block, i.e. block number 2
//            BlockInfo returnedBlock = channel.queryBlockByNumber(channelInfo.getHeight() - 1);
//            String previousHash = Hex.encodeHexString(returnedBlock.getPreviousHash());
//            Util.out("queryBlockByNumber returned correct block with blockNumber " + returnedBlock.getBlockNumber()
//                    + " \n previous_hash " + previousHash);
//
//            // Query by block hash. Using latest block's previous hash so should return block number 1
//            byte[] hashQuery = returnedBlock.getPreviousHash();
//            returnedBlock = channel.queryBlockByHash(hashQuery);
//            Util.out("queryBlockByHash returned block with blockNumber " + returnedBlock.getBlockNumber());
//
//            // Query block by TxID. Since it's the last TxID, should be block 2
//            returnedBlock = channel.queryBlockByTransactionID(testTxID);
//            Util.out("queryBlockByTxID returned block with blockNumber " + returnedBlock.getBlockNumber());
//
//            // query transaction by ID
//            TransactionInfo txInfo = channel.queryTransactionByID(testTxID);
//            Util.out("QueryTransactionByID returned TransactionInfo: txID " + txInfo.getTransactionID()
//                    + "\n     validation code " + txInfo.getValidationCode().getNumber());
//            Util.out("Running for Channel %s done", channel.getName());
//
//        } catch (Exception e) {
//            Util.out("Caught an exception running channel %s", channel.getName());
//            e.printStackTrace();
//            Util.fail("Test failed with error : " + e.getMessage());
//        }
//    }

    public static List<String[]> queryChainCode(Channel channel, String functionName, String[] args) {
        return queryChainCode(client, channel, functionName, null, args);
    }

    private static List<String[]> queryChainCode(HFClient client, Channel channel, String functionName,
                                                 BlockEvent.TransactionEvent transactionEvent, String[] args) {
        try {
            if (null != transactionEvent) {
                waitOnFabric(0);
                Util.out("Finished transaction with transaction id %s", transactionEvent.getTransactionID());
                testTxID = transactionEvent.getTransactionID(); // used in the channel queries later
            }
            ////////////////////////////
            // Send Query Proposal to all peers
            //
            String expect = "" + (300 + DELTA);
            Util.out("Now query chaincode for the value of b.");
            QueryByChaincodeRequest queryByChaincodeRequest = client.newQueryProposalRequest();
            queryByChaincodeRequest.setArgs(args);
            queryByChaincodeRequest.setFcn(functionName);
            queryByChaincodeRequest.setChaincodeID(chaincodeID);

            Map<String, byte[]> tm2 = new HashMap<>();
            tm2.put("HyperLedgerFabric", "QueryByChaincodeRequest:JavaSDK".getBytes(UTF_8));
            tm2.put("method", "QueryByChaincodeRequest".getBytes(UTF_8));
            queryByChaincodeRequest.setTransientMap(tm2);
            List<String[]> payloads = new ArrayList<>();

            Collection<ProposalResponse> queryProposals = channel.queryByChaincode(queryByChaincodeRequest, channel
                    .getPeers());
            for (ProposalResponse proposalResponse : queryProposals) {
                if (!proposalResponse.isVerified() || proposalResponse.getStatus() != ProposalResponse.Status.SUCCESS) {
                    Util.out("Failed query proposal from peer " + proposalResponse.getPeer().getName() + " status: "
                            + proposalResponse.getStatus() +
                            ". Messages: " + proposalResponse.getMessage()
                            + ". Was verified : " + proposalResponse.isVerified());
                    String[] returnPayload = new String[2];
                    returnPayload[0] = proposalResponse.getPeer().getName();
                    returnPayload[1] = null;
                    payloads.add(returnPayload);
                } else {
                    String payload = proposalResponse.getProposalResponse().getResponse().getPayload().toStringUtf8();
                    Util.out("Query payload of b from peer %s returned %s", proposalResponse.getPeer().getName(),
                            payload);
                    String[] returnPayload = new String[2];
                    returnPayload[0] = proposalResponse.getPeer().getName();
                    returnPayload[1] = payload;
                    payloads.add(returnPayload);
                }
            }
            return payloads;
        } catch (Exception e) {
            Util.out("Caught exception while running query");
            e.printStackTrace();
            Util.fail("Failed during chaincode query with error : " + e.getMessage());
        }
        return null;
    }

    public static CompletableFuture<BlockEvent.TransactionEvent> invokeChaincode(Channel channel, String[] args, User
            user) {
        return invokeChaincode(client, channel, chaincodeID, args, user);
    }

    private static CompletableFuture<BlockEvent.TransactionEvent> invokeChaincode(HFClient client, Channel channel,
                                                                                  ChaincodeID chaincodeID, String[]
                                                                                          args, User user) {
        try {
            Collection<ProposalResponse> successful = new LinkedList<>();
            Collection<ProposalResponse> failed = new LinkedList<>();

            ///////////////
            /// Send transaction proposal to all peers
            TransactionProposalRequest transactionProposalRequest = client.newTransactionProposalRequest();
            transactionProposalRequest.setChaincodeID(chaincodeID);
            transactionProposalRequest.setFcn("invoke");
            transactionProposalRequest.setArgs(args);  //new String[]{"move", "a", "b", moveAmount}
            transactionProposalRequest.setProposalWaitTime(testConfig.getProposalWaitTime());
            if (user != null) { // specific user use that
                transactionProposalRequest.setUserContext(user);
            }
            Util.out("sending transaction proposal to all peers with arguments: (", args[0] + "\"" + args[1] + "\"" +
                    args[1] + "\"" + args[2]);

            Collection<ProposalResponse> invokePropResp = channel.sendTransactionProposal(transactionProposalRequest);
            for (ProposalResponse response : invokePropResp) {
                if (response.getStatus() == ChaincodeResponse.Status.SUCCESS) {
                    Util.out("Successful transaction proposal response Txid: %s from peer %s", response
                            .getTransactionID(), response.getPeer().getName());
                    successful.add(response);
                } else {
                    failed.add(response);
                }
            }

            Util.out("Received %d transaction proposal responses. Successful+verified: %d . Failed: %d",
                    invokePropResp.size(), successful.size(), failed.size());
            if (failed.size() > 0) {
                ProposalResponse firstTransactionProposalResponse = failed.iterator().next();

                throw new ProposalException(format("Not enough endorsers for invoke(" + args[0] + "):%d endorser " +
                                "error:%s. Was verified:%b",
                        args[args.length - 1], firstTransactionProposalResponse.getStatus().getStatus(),
                        firstTransactionProposalResponse.getMessage(),
                        firstTransactionProposalResponse.isVerified()));
            }
            Util.out("Successfully received transaction proposal responses.");

            ////////////////////////////
            // Send transaction to orderer
            Util.out("Sending chaincode transaction(move a,b,%s) to orderer.", args[args.length - 1]);
            if (user != null) {
                return channel.sendTransaction(successful, user);
            }
            return channel.sendTransaction(successful);
        } catch (Exception e) {

            throw new CompletionException(e);
        }
    }

    public static Channel constructChannel(String name) throws Exception {
        return constructChannel(name, client, sampleOrg);
    }

    private static Channel constructChannel(String name, HFClient client, SampleOrg sampleOrg) throws Exception {
        ////////////////////////////
        //Construct the channel
        //
        Util.out("Constructing channel %s", name);

        //Only peer Admin org
        client.setUserContext(sampleOrg.getPeerAdmin());

        orderers = new LinkedList<>();

        for (String orderName : sampleOrg.getOrdererNames()) {

            Properties ordererProperties = testConfig.getOrdererProperties(orderName);

            //example of setting keepAlive to avoid timeouts on inactive http2 connections.
            // Under 5 minutes would require changes to server side to accept faster ping rates.
            ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTime", new Object[]{5L, TimeUnit.MINUTES});
            ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTimeout", new Object[]{8L, TimeUnit
                    .SECONDS});

            orderers.add(client.newOrderer(orderName, sampleOrg.getOrdererLocation(orderName),
                    ordererProperties));
        }

        //Just pick the first orderer in the list to create the channel.

        Orderer anOrderer = orderers.iterator().next();
        //orderers.remove(anOrderer);

        // ChannelConfiguration channelConfiguration = new ChannelConfiguration(new File(TEST_FIXTURES_PATH +
        // "/sdkintegration/e2e-2Orgs/channel/" + name + ".tx"));

        //Create channel that has only one signer that is this orgs peer admin. If channel creation policy needed
        // more signature they would need to be added too.
        //Channel newChannel = client.newChannel(name, anOrderer, channelConfiguration, client
        // .getChannelConfigurationSignature(channelConfiguration, sampleOrg.getPeerAdmin()));
        Channel newChannel = client.getChannel(name);
        if (null == newChannel) {
            // @ascatox Constructs a new channel
            newChannel = client.newChannel(name);
        }
        Util.out("Created channel %s", name);
        newChannel.addOrderer(anOrderer);

        for (String peerName : sampleOrg.getPeerNames()) {
            String peerLocation = sampleOrg.getPeerLocation(peerName);

            Properties peerProperties = testConfig.getPeerProperties(peerName); //test properties for peer.. if any.
            if (peerProperties == null) {
                peerProperties = new Properties();
            }
            //Example of setting specific options on grpc's NettyChannelBuilder
            peerProperties.put("grpc.NettyChannelBuilderOption.maxInboundMessageSize", 9000000);

            Peer peer = client.newPeer(peerName, peerLocation, peerProperties);
//            newChannel.joinPeer(peer);
            newChannel.addPeer(peer);
            Util.out("Peer %s joined channel %s", peerName, name);
            sampleOrg.addPeer(peer);
        }
/*
        for (Orderer orderer : orderers) { //add remaining orderers if any.
            newChannel.addOrderer(orderer);
        }
        for (String eventHubName : sampleOrg.getEventHubNames()) {

            final Properties eventHubProperties = testConfig.getEventHubProperties(eventHubName);

            eventHubProperties.put("grpc.NettyChannelBuilderOption.keepAliveTime", new Object[]{5L, TimeUnit.MINUTES});
            eventHubProperties.put("grpc.NettyChannelBuilderOption.keepAliveTimeout", new Object[]{8L, TimeUnit
            .SECONDS});

            EventHub eventHub = client.newEventHub(eventHubName, sampleOrg.getEventHubLocation(eventHubName),
                    eventHubProperties);
            newChannel.addEventHub(eventHub);
        }
*/
        newChannel.initialize();

        Util.out("Finished initialization channel %s", name);

        return newChannel;

    }

    private static void waitOnFabric(int additional) {
        //NOOP today
    }



    void blockWalker(Channel channel) throws InvalidArgumentException, ProposalException, IOException {
        try {
            BlockchainInfo channelInfo = channel.queryBlockchainInfo();

            for (long current = channelInfo.getHeight() - 1; current > -1; --current) {
                BlockInfo returnedBlock = channel.queryBlockByNumber(current);
                final long blockNumber = returnedBlock.getBlockNumber();

                Util.out("current block number %d has data hash: %s", blockNumber, Hex.encodeHexString(returnedBlock
                        .getDataHash()));
                Util.out("current block number %d has previous hash id: %s", blockNumber, Hex.encodeHexString
                        (returnedBlock.getPreviousHash()));
                Util.out("current block number %d has calculated block hash is %s", blockNumber, Hex.encodeHexString
                        (SDKUtils.calculateBlockHash(blockNumber, returnedBlock
                        .getPreviousHash(), returnedBlock.getDataHash())));

                final int envelopeCount = returnedBlock.getEnvelopeCount();
                Util.out("current block number %d has %d envelope count:", blockNumber, returnedBlock
                        .getEnvelopeCount());
                int i = 0;
                for (BlockInfo.EnvelopeInfo envelopeInfo : returnedBlock.getEnvelopeInfos()) {
                    ++i;

                    Util.out("  Transaction number %d has transaction id: %s", i, envelopeInfo.getTransactionID());
                    final String channelId = envelopeInfo.getChannelId();

                    Util.out("  Transaction number %d has channel id: %s", i, channelId);
                    Util.out("  Transaction number %d has epoch: %d", i, envelopeInfo.getEpoch());
                    Util.out("  Transaction number %d has transaction timestamp: %tB %<te,  %<tY  %<tT %<Tp", i,
                            envelopeInfo.getTimestamp());
                    Util.out("  Transaction number %d has type id: %s", i, "" + envelopeInfo.getType());

                    if (envelopeInfo.getType() == TRANSACTION_ENVELOPE) {
                        BlockInfo.TransactionEnvelopeInfo transactionEnvelopeInfo = (BlockInfo
                                .TransactionEnvelopeInfo) envelopeInfo;

                        Util.out("  Transaction number %d has %d actions", i, transactionEnvelopeInfo
                                .getTransactionActionInfoCount());
                        Util.out("  Transaction number %d isValid %b", i, transactionEnvelopeInfo.isValid());
                        Util.out("  Transaction number %d validation code %d", i, transactionEnvelopeInfo
                                .getValidationCode());

                        int j = 0;
                        for (BlockInfo.TransactionEnvelopeInfo.TransactionActionInfo transactionActionInfo :
                                transactionEnvelopeInfo.getTransactionActionInfos()) {
                            ++j;
                            Util.out("   Transaction action %d has response status %d", j, transactionActionInfo
                                    .getResponseStatus());
                            Util.out("   Transaction action %d has response message bytes as string: %s", j,
                                    printableString(new String(transactionActionInfo.getResponseMessageBytes(),
                                            "UTF-8")));
                            Util.out("   Transaction action %d has %d endorsements", j, transactionActionInfo
                                    .getEndorsementsCount());

                            for (int n = 0; n < transactionActionInfo.getEndorsementsCount(); ++n) {
                                BlockInfo.EndorserInfo endorserInfo = transactionActionInfo.getEndorsementInfo(n);
                                Util.out("Endorser %d signature: %s", n, Hex.encodeHexString(endorserInfo
                                        .getSignature()));
                                Util.out("Endorser %d endorser: %s", n, new String(endorserInfo.getEndorser(),
                                        "UTF-8"));
                            }
                            Util.out("   Transaction action %d has %d chaincode input arguments", j,
                                    transactionActionInfo.getChaincodeInputArgsCount());
                            for (int z = 0; z < transactionActionInfo.getChaincodeInputArgsCount(); ++z) {
                                Util.out("     Transaction action %d has chaincode input argument %d is: %s", j, z,
                                        printableString(new String(transactionActionInfo.getChaincodeInputArgs(z),
                                                "UTF-8")));
                            }

                            Util.out("   Transaction action %d proposal response status: %d", j,
                                    transactionActionInfo.getProposalResponseStatus());
                            Util.out("   Transaction action %d proposal response payload: %s", j,
                                    printableString(new String(transactionActionInfo.getProposalResponsePayload())));

                            // Check to see if we have our expected event.
//                            if (blockNumber == 2) {
//                                ChaincodeEvent chaincodeEvent = transactionActionInfo.getEvent();
//                            }

                            TxReadWriteSetInfo rwsetInfo = transactionActionInfo.getTxReadWriteSet();
                            if (null != rwsetInfo) {
                                Util.out("   Transaction action %d has %d name space read write sets", j, rwsetInfo
                                        .getNsRwsetCount());

                                for (TxReadWriteSetInfo.NsRwsetInfo nsRwsetInfo : rwsetInfo.getNsRwsetInfos()) {
                                    final String namespace = nsRwsetInfo.getNamespace();
                                    KvRwset.KVRWSet rws = nsRwsetInfo.getRwset();

                                    int rs = -1;
                                    for (KvRwset.KVRead readList : rws.getReadsList()) {
                                        rs++;

                                        Util.out("     Namespace %s read set %d key %s  version [%d:%d]", namespace,
                                                rs, readList.getKey(),
                                                readList.getVersion().getBlockNum(), readList.getVersion().getTxNum());

                                        if ("bar".equals(channelId) && blockNumber == 2) {
                                            if ("example_cc_go".equals(namespace)) {
                                                if (rs == 0) {
                                                } else if (rs == 1) {
                                                } else {
                                                    Util.fail(format("unexpected readset %d", rs));
                                                }

                                                TX_EXPECTED.remove("readset1");
                                            }
                                        }
                                    }

                                    rs = -1;
                                    for (KvRwset.KVWrite writeList : rws.getWritesList()) {
                                        rs++;
                                        String valAsString = printableString(new String(writeList.getValue()
                                                .toByteArray(), "UTF-8"));

                                        Util.out("     Namespace %s write set %d key %s has value '%s' ", namespace, rs,
                                                writeList.getKey(),
                                                valAsString);

                                        if ("bar".equals(channelId) && blockNumber == 2) {
                                            if (rs == 0) {
                                            } else if (rs == 1) {
                                            } else {
                                                Util.fail(format("unexpected writeset %d", rs));
                                            }

                                            TX_EXPECTED.remove("writeset1");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (!TX_EXPECTED.isEmpty()) {
                Util.fail(TX_EXPECTED.get(0));
            }
        } catch (InvalidProtocolBufferRuntimeException e) {
            throw e.getCause();
        }
    }

    static String printableString(final String string) {
        int maxLogStringLength = 64;
        if (string == null || string.length() == 0) {
            return string;
        }

        String ret = string.replaceAll("[^\\p{Print}]", "?");

        ret = ret.substring(0, Math.min(ret.length(), maxLogStringLength)) + (ret.length() > maxLogStringLength ?
                "..." : "");

        return ret;
    }

    public static void installChaincode(Channel channel) throws ChaincodeEndorsementPolicyParseException,
            InvalidArgumentException, ProposalException, IOException {
        Collection<ProposalResponse> successful = new ArrayList<>();
        Collection<ProposalResponse> failed = new ArrayList<>();
        installChaincode(client, channel, sampleOrg, successful, failed);
    }

    private static void installChaincode(HFClient client, Channel channel, SampleOrg sampleOrg,
                                         Collection<ProposalResponse> successful, Collection<ProposalResponse> failed)
            throws InvalidArgumentException, ProposalException, IOException, ChaincodeEndorsementPolicyParseException {
        ////////////////////////////
        // Install Proposal Request

        final String channelName = channel.getName();
        boolean isFooChain = FOO_CHANNEL_NAME.equals(channelName);
        Util.out("Running channel %s", channelName);
        channel.setTransactionWaitTime(testConfig.getTransactionWaitTime());
        channel.setDeployWaitTime(testConfig.getDeployWaitTime());

        Collection<ProposalResponse> responses;
        //

        client.setUserContext(sampleOrg.getPeerAdmin());

        Util.out("Creating install proposal");

        InstallProposalRequest installProposalRequest = client.newInstallProposalRequest();
        installProposalRequest.setChaincodeID(chaincodeID);

        String chaincodePathPrefix = finder.getString("CHAIN_CODE_PATH_PREFIX");
        // "/sdkintegration/gocc/sample1";
        if (isFooChain) {
            // on foo chain install from directory.
            ////For GO language and serving just a single user, chaincodeSource is mostly likely the users GOPATH
            installProposalRequest.setChaincodeSourceLocation(new File(TEST_FIXTURES_PATH + chaincodePathPrefix));
        } else {
            // On bar chain install from an input stream.
            installProposalRequest.setChaincodeInputStream(Util.generateTarGzInputStream(
                    (Paths.get(TEST_FIXTURES_PATH, chaincodePathPrefix, "src", CHAIN_CODE_PATH).toFile()),
                    Paths.get("src", CHAIN_CODE_PATH).toString()));
        }

        installProposalRequest.setChaincodeVersion(CHAIN_CODE_VERSION);
        Util.out("Sending install proposal");

        ////////////////////////////
        // only a client from the same org as the peer can issue an install request
        int numInstallProposal = 0;
        //    Set<String> orgs = orgPeers.keySet();
        //   for (SampleOrg org : testSampleOrgs) {

        Set<Peer> peersFromOrg = sampleOrg.getPeers();
        numInstallProposal = numInstallProposal + peersFromOrg.size();
        responses = client.sendInstallProposal(installProposalRequest, peersFromOrg);

        for (ProposalResponse response : responses) {
            if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                Util.out("Successful install proposal response Txid: %s from peer %s", response.getTransactionID(),
                        response.getPeer().getName());
                successful.add(response);
            } else {
                failed.add(response);
            }
        }
        //   }
        Util.out("Received %d install proposal responses. Successful+verified: %d . Failed: %d", numInstallProposal,
                successful.size(), failed.size());
        if (failed.size() > 0) {
            ProposalResponse first = failed.iterator().next();
            Util.fail("Not enough endorsers for install :" + successful.size() + ".  " + first.getMessage());
        }

        //   client.setUserContext(sampleOrg.getUser(TEST_ADMIN_NAME));
        //  final ChaincodeID chaincodeID = firstInstallProposalResponse.getChaincodeID();
        // Note installing chaincode does not require transaction no need to
        // send to Orderers

    }

    public static CompletableFuture<BlockEvent.TransactionEvent> instantiateChaincode(Channel channel, String[] args)
            throws InvalidArgumentException, ProposalException,
            ChaincodeEndorsementPolicyParseException, IOException, ExecutionException, InterruptedException {
        Collection<ProposalResponse> successful = new ArrayList<>();
        Collection<ProposalResponse> failed = new ArrayList<>();
        return instantiateChaincode(client, channel, args, successful, failed, true, orderers);
    }

    private static CompletableFuture<BlockEvent.TransactionEvent> instantiateChaincode(HFClient client, Channel
            channel, String[] args,
                                                                                       Collection<ProposalResponse>
                                                                                               successful,
                                                                                       Collection<ProposalResponse>
                                                                                               failed, boolean
                                                                                               isFooChain,
                                                                                       Collection<Orderer> orderers)
            throws InvalidArgumentException, IOException, ChaincodeEndorsementPolicyParseException,
            ProposalException, ExecutionException, InterruptedException {
        Collection<ProposalResponse> responses;///////////////
        //// Instantiate chaincode.
        InstantiateProposalRequest instantiateProposalRequest = client.newInstantiationProposalRequest();
        instantiateProposalRequest.setProposalWaitTime(testConfig.getProposalWaitTime());
        instantiateProposalRequest.setChaincodeID(chaincodeID);
        instantiateProposalRequest.setFcn("init");
        //instantiateProposalRequest.setArgs(new String[]{"a", "500", "b", "" + (200 + DELTA)});
        instantiateProposalRequest.setArgs(args);
        Map<String, byte[]> tm = new HashMap<>();
        tm.put("HyperLedgerFabric", "InstantiateProposalRequest:JavaSDK".getBytes(UTF_8));
        tm.put("method", "InstantiateProposalRequest".getBytes(UTF_8));
        instantiateProposalRequest.setTransientMap(tm);

    /*
      policy OR(Org1MSP.member, Org2MSP.member) meaning 1 signature from someone in either Org1 or Org2
      See README.md Chaincode endorsement policies section for more details.
    */
        ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
        chaincodeEndorsementPolicy.fromYamlFile(new File(TEST_FIXTURES_PATH +
                "/sdkintegration/chaincodeendorsementpolicy.yaml"));
        instantiateProposalRequest.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);

        Util.out("Sending instantiateProposalRequest to all peers with arguments: " + args.toString() + " %s " +
                "respectively", "" + (200 + DELTA));
        successful.clear();
        failed.clear();

        if (isFooChain) {  //Send responses both ways with specifying peers and by using those on the channel.
            responses = channel.sendInstantiationProposal(instantiateProposalRequest, channel.getPeers());
        } else {
            responses = channel.sendInstantiationProposal(instantiateProposalRequest);
        }
        for (ProposalResponse response : responses) {
            if (response.isVerified() && response.getStatus() == ProposalResponse.Status.SUCCESS) {
                successful.add(response);
                Util.out("Succesful instantiate proposal response Txid: %s from peer %s", response.getTransactionID()
                        , response.getPeer().getName());
            } else {
                failed.add(response);
            }
        }
        Util.out("Received %d instantiate proposal responses. Successful+verified: %d . Failed: %d", responses.size()
                , successful.size(), failed.size());
        if (failed.size() > 0) {
            ProposalResponse first = failed.iterator().next();
            Util.fail(first.getMessage() + ". Was verified:" + first.isVerified());
        }
        ///////////////
        /// Send instantiate transaction to orderer
        Util.out("Sending instantiateTransaction to orderer %s respectively", "" + (200 + DELTA));
        return channel.sendTransaction(successful, orderers);
    }

}
