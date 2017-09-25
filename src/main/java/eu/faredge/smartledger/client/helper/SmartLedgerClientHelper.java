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
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hyperledger.fabric.sdk.BlockInfo.EnvelopeType.TRANSACTION_ENVELOPE;

public class SmartLedgerClientHelper {
    private static final TestConfig testConfig = TestConfig.getConfig();
    private static final String TEST_ADMIN_NAME = "admin";
    private static final String TESTUSER_1_NAME = "user1";
    private static final String TEST_FIXTURES_PATH = "src/main/java/fixture";

    private static final String CHAIN_CODE_NAME = "fabcar";
    private static final String CHAIN_CODE_PATH = "github.com/example_cc";
    private static final String CHAIN_CODE_VERSION = "1";

    private static final String FOO_CHANNEL_NAME = "mychannel";
    private static final String BAR_CHANNEL_NAME = "bar";

    private static final byte[] EXPECTED_EVENT_DATA = "!".getBytes(UTF_8);
    private static final String EXPECTED_EVENT_NAME = "event";

    private static String testTxID = null;  // save the CC invoke TxID and use in queries

    private static Collection<SampleOrg> testSampleOrgs;
    private static ChaincodeID chaincodeID = null;
    private static final int DELTA = 10;
    private static HFClient client = null;
    private static Channel fooChannel = null;
    private static SampleOrg sampleOrg = null;

    public static void main(String[] args) {
        try {
            checkConfig();
        } catch (NoSuchFieldException e) {
            fail(e.getMessage());
        } catch (IllegalAccessException e) {
            fail(e.getMessage());
        } catch (MalformedURLException e) {
            fail(e.getMessage());
        } catch (org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException e) {
            fail(e.getMessage());
        }
        setup();
    }


    public static void checkConfig() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, MalformedURLException, org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException {
        out("\n\n\nRUNNING: End2endIT.\n");
        chaincodeID = ChaincodeID.newBuilder().setName(CHAIN_CODE_NAME)
                .setVersion(CHAIN_CODE_VERSION)
                .setPath(CHAIN_CODE_PATH).build();

        testSampleOrgs = testConfig.getIntegrationTestsSampleOrgs();
        //Set up hfca for each sample org

        for (SampleOrg sampleOrg : testSampleOrgs) {
            String caName = sampleOrg.getCAName(); //Try one of each name and no name.
            if (caName != null && !caName.isEmpty()) {
                sampleOrg.setCAClient(HFCAClient.createNewInstance(caName, sampleOrg.getCALocation(), sampleOrg.getCAProperties()));
            } else {
                sampleOrg.setCAClient(HFCAClient.createNewInstance(sampleOrg.getCALocation(), sampleOrg.getCAProperties()));
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

                // src/test/fixture/sdkintegration/e2e-2Orgs/channel/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/keystore/

                SampleUser peerOrgAdmin = sampleStore.getMember(sampleOrgName + "Admin", sampleOrgName, sampleOrg.getMSPID(),
                        Util.findFileSk(Paths.get(testConfig.getTestChannelPath(), "crypto-config-fabcar/peerOrganizations/",
                                sampleOrgDomainName, format("/users/Admin@%s/msp/keystore", sampleOrgDomainName)).toFile()),
                        Paths.get(testConfig.getTestChannelPath(), "crypto-config-fabcar/peerOrganizations/", sampleOrgDomainName,
                                format("/users/Admin@%s/msp/signcerts/Admin@%s-cert.pem", sampleOrgDomainName, sampleOrgDomainName)).toFile());

                sampleOrg.setPeerAdmin(peerOrgAdmin); //A special user that can create channels, join peers and install chaincode
            }

            ////////////////////////////
            //Construct and run the channels
            sampleOrg = testConfig.getIntegrationTestsSampleOrg("peerOrg1");
            fooChannel = constructChannel(FOO_CHANNEL_NAME, client, sampleOrg);
            //runChannel(client, fooChannel, false, true, sampleOrg, 0);
 //           String[] args = {""};
 //           queryChainCode(fooChannel, chaincodeID,"queryAllCars", args);

//            fooChannel.shutdown(true); // Force foo channel to shutdown clean up resources.
////            out("\nTraverse the blocks for chain %s ", barChannel.getName());
////            blockWalker(barChannel);
//            out("That's all folks!");

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    private static void runChannel(HFClient client, Channel channel, boolean installChaincode, boolean instantiateChaincode, SampleOrg sampleOrg, int delta) {

        try {
            Collection<ProposalResponse> successful = new LinkedList<>();
            Collection<ProposalResponse> failed = new LinkedList<>();
            installChaincode(client, channel, sampleOrg, successful, failed).thenApply(transactionEvent ->
            {
                waitOnFabric(0);
                out("Finished instantiate transaction with transaction id %s", transactionEvent.getTransactionID());
                return invokeChaincode(client, channel, sampleOrg, successful, failed);
            }).thenApply(transactionEvent -> {
                //return queryChainCode(client, channel, "invoke", chaincodeID, transactionEvent);
                return null;
            }).exceptionally(e -> {
                if (e instanceof TransactionEventException) {
                    BlockEvent.TransactionEvent te = ((TransactionEventException) e).getTransactionEvent();
                    if (te != null) {
                        fail(format("Transaction with txid %s failed. %s", te.getTransactionID(), e.getMessage()));
                    }
                }
                fail(format("Test failed with %s exception %s", e.getClass().getName(), e.getMessage()));

                return null;
            }).get(testConfig.getTransactionWaitTime(), TimeUnit.SECONDS);

            // Channel queries

            // We can only send channel queries to peers that are in the same org as the SDK user context
            // Get the peers from the current org being used and pick one randomly to send the queries to.
            Set<Peer> peerSet = sampleOrg.getPeers();
            //  Peer queryPeer = peerSet.iterator().next();
            //   out("Using peer %s for channel queries", queryPeer.getName());

            BlockchainInfo channelInfo = channel.queryBlockchainInfo();
            out("Channel info for : " + channel.getName());
            out("Channel height: " + channelInfo.getHeight());
            String chainCurrentHash = Hex.encodeHexString(channelInfo.getCurrentBlockHash());
            String chainPreviousHash = Hex.encodeHexString(channelInfo.getPreviousBlockHash());
            out("Chain current block hash: " + chainCurrentHash);
            out("Chainl previous block hash: " + chainPreviousHash);

            // Query by block number. Should return latest block, i.e. block number 2
            BlockInfo returnedBlock = channel.queryBlockByNumber(channelInfo.getHeight() - 1);
            String previousHash = Hex.encodeHexString(returnedBlock.getPreviousHash());
            out("queryBlockByNumber returned correct block with blockNumber " + returnedBlock.getBlockNumber()
                    + " \n previous_hash " + previousHash);

            // Query by block hash. Using latest block's previous hash so should return block number 1
            byte[] hashQuery = returnedBlock.getPreviousHash();
            returnedBlock = channel.queryBlockByHash(hashQuery);
            out("queryBlockByHash returned block with blockNumber " + returnedBlock.getBlockNumber());

            // Query block by TxID. Since it's the last TxID, should be block 2
            returnedBlock = channel.queryBlockByTransactionID(testTxID);
            out("queryBlockByTxID returned block with blockNumber " + returnedBlock.getBlockNumber());

            // query transaction by ID
            TransactionInfo txInfo = channel.queryTransactionByID(testTxID);
            out("QueryTransactionByID returned TransactionInfo: txID " + txInfo.getTransactionID()
                    + "\n     validation code " + txInfo.getValidationCode().getNumber());

//            if (chaincodeEventListenerHandle != null) {
//                channel.unRegisterChaincodeEventListener(chaincodeEventListenerHandle);
//                //Should be two. One event in chaincode and two notification for each of the two event hubs
//
//                final int numberEventHubs = channel.getEventHubs().size();
//                //just make sure we get the notifications.
//                for (int i = 15; i > 0; --i) {
//                    if (chaincodeEvents.size() == numberEventHubs) {
//                        break;
//                    } else {
//                        Thread.sleep(90); // wait for the events.
//                    }
//
//                }
//            }
            out("Running for Channel %s done", channel.getName());

        } catch (Exception e) {
            out("Caught an exception running channel %s", channel.getName());
            e.printStackTrace();
            fail("Test failed with error : " + e.getMessage());
        }
    }

    public static List<String[]> queryChainCode(Channel channel, String functionName, String[] args) {
        return queryChainCode(client, channel, functionName, null, args);
    }

    private static List<String[]> queryChainCode(HFClient client, Channel channel, String functionName,
                                               BlockEvent.TransactionEvent transactionEvent, String[] args) {
        try {

            if (null != transactionEvent) {
                waitOnFabric(0);
                out("Finished transaction with transaction id %s", transactionEvent.getTransactionID());
                testTxID = transactionEvent.getTransactionID(); // used in the channel queries later
            }

            ////////////////////////////
            // Send Query Proposal to all peers
            //
            String expect = "" + (300 + DELTA);
            out("Now query chaincode for the value of b.");
            QueryByChaincodeRequest queryByChaincodeRequest = client.newQueryProposalRequest();
            queryByChaincodeRequest.setArgs(args);
            queryByChaincodeRequest.setFcn(functionName);
            queryByChaincodeRequest.setChaincodeID(chaincodeID);

            Map<String, byte[]> tm2 = new HashMap<>();
            tm2.put("HyperLedgerFabric", "QueryByChaincodeRequest:JavaSDK".getBytes(UTF_8));
            tm2.put("method", "QueryByChaincodeRequest".getBytes(UTF_8));
            queryByChaincodeRequest.setTransientMap(tm2);
            List<String[]> payloads = new ArrayList<>();

            Collection<ProposalResponse> queryProposals = channel.queryByChaincode(queryByChaincodeRequest, channel.getPeers());
            for (ProposalResponse proposalResponse : queryProposals) {
                if (!proposalResponse.isVerified() || proposalResponse.getStatus() != ProposalResponse.Status.SUCCESS) {
                    out("Failed query proposal from peer " + proposalResponse.getPeer().getName() + " status: " + proposalResponse.getStatus() +
                            ". Messages: " + proposalResponse.getMessage()
                            + ". Was verified : " + proposalResponse.isVerified());
                    String[] returnPayload = new String[2];
                    returnPayload[0] = proposalResponse.getPeer().getName();
                    returnPayload[1] = null;
                    payloads.add(returnPayload);
                } else {
                    String payload = proposalResponse.getProposalResponse().getResponse().getPayload().toStringUtf8();
                    out("Query payload of b from peer %s returned %s", proposalResponse.getPeer().getName(), payload);
                    String[] returnPayload = new String[2];
                    returnPayload[0] = proposalResponse.getPeer().getName();
                    returnPayload[1] = payload;
                    payloads.add(returnPayload);

                }
            }
            return payloads;
        } catch (Exception e) {
            out("Caught exception while running query");
            e.printStackTrace();
            fail("Failed during chaincode query with error : " + e.getMessage());
        }
        return null;
    }

    public static CompletableFuture<BlockEvent.TransactionEvent> invokeChaincode(Channel channel, String[] args, User user) {
        return invokeChaincode(client, channel, chaincodeID, args, user);
    }

    private static CompletableFuture<BlockEvent.TransactionEvent> invokeChaincode(HFClient client, Channel channel, ChaincodeID chaincodeID, String[] args, User user) {
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
            out("sending transaction proposal to all peers with arguments: (", args[0] + "\"" + args[1] + "\"" + args[1] + "\"" + args[2]);

            Collection<ProposalResponse> invokePropResp = channel.sendTransactionProposal(transactionProposalRequest);
            for (ProposalResponse response : invokePropResp) {
                if (response.getStatus() == ChaincodeResponse.Status.SUCCESS) {
                    out("Successful transaction proposal response Txid: %s from peer %s", response.getTransactionID(), response.getPeer().getName());
                    successful.add(response);
                } else {
                    failed.add(response);
                }
            }

            out("Received %d transaction proposal responses. Successful+verified: %d . Failed: %d",
                    invokePropResp.size(), successful.size(), failed.size());
            if (failed.size() > 0) {
                ProposalResponse firstTransactionProposalResponse = failed.iterator().next();

                throw new ProposalException(format("Not enough endorsers for invoke(" + args[0] + "):%d endorser error:%s. Was verified:%b",
                        args[args.length - 1], firstTransactionProposalResponse.getStatus().getStatus(), firstTransactionProposalResponse.getMessage(), firstTransactionProposalResponse.isVerified()));
            }
            out("Successfully received transaction proposal responses.");

            ////////////////////////////
            // Send transaction to orderer
            out("Sending chaincode transaction(move a,b,%s) to orderer.", args[args.length - 1]);
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
        out("Constructing channel %s", name);

        //Only peer Admin org
        client.setUserContext(sampleOrg.getPeerAdmin());

        Collection<Orderer> orderers = new LinkedList<>();

        for (String orderName : sampleOrg.getOrdererNames()) {

            Properties ordererProperties = testConfig.getOrdererProperties(orderName);

            //example of setting keepAlive to avoid timeouts on inactive http2 connections.
            // Under 5 minutes would require changes to server side to accept faster ping rates.
            ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTime", new Object[]{5L, TimeUnit.MINUTES});
            ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTimeout", new Object[]{8L, TimeUnit.SECONDS});

            orderers.add(client.newOrderer(orderName, sampleOrg.getOrdererLocation(orderName),
                    ordererProperties));
        }

        //Just pick the first orderer in the list to create the channel.

        Orderer anOrderer = orderers.iterator().next();
        orderers.remove(anOrderer);

        // ChannelConfiguration channelConfiguration = new ChannelConfiguration(new File(TEST_FIXTURES_PATH + "/sdkintegration/e2e-2Orgs/channel/" + name + ".tx"));

        //Create channel that has only one signer that is this orgs peer admin. If channel creation policy needed more signature they would need to be added too.
        //Channel newChannel = client.newChannel(name, anOrderer, channelConfiguration, client.getChannelConfigurationSignature(channelConfiguration, sampleOrg.getPeerAdmin()));
        Channel newChannel = client.getChannel(name);
        if (null == newChannel) {
            // @ascatox Constructs a new channel
            newChannel = client.newChannel(name);
        }
        out("Created channel %s", name);
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
            out("Peer %s joined channel %s", peerName, name);
            sampleOrg.addPeer(peer);
        }
/*
        for (Orderer orderer : orderers) { //add remaining orderers if any.
            newChannel.addOrderer(orderer);
        }
        for (String eventHubName : sampleOrg.getEventHubNames()) {

            final Properties eventHubProperties = testConfig.getEventHubProperties(eventHubName);

            eventHubProperties.put("grpc.NettyChannelBuilderOption.keepAliveTime", new Object[]{5L, TimeUnit.MINUTES});
            eventHubProperties.put("grpc.NettyChannelBuilderOption.keepAliveTimeout", new Object[]{8L, TimeUnit.SECONDS});

            EventHub eventHub = client.newEventHub(eventHubName, sampleOrg.getEventHubLocation(eventHubName),
                    eventHubProperties);
            newChannel.addEventHub(eventHub);
        }
*/
        newChannel.initialize();

        out("Finished initialization channel %s", name);

        return newChannel;

    }

    static void out(String format, Object... args) {

        System.err.flush();
        System.out.flush();

        System.out.println(format(format, args));
        System.err.flush();
        System.out.flush();

    }

    private static void waitOnFabric(int additional) {
        //NOOP today
    }

    private static final Map<String, String> TX_EXPECTED;

    static {
        TX_EXPECTED = new HashMap<>();
        TX_EXPECTED.put("readset1", "Missing readset for channel bar block 1");
        TX_EXPECTED.put("writeset1", "Missing writeset for channel bar block 1");
    }

    void blockWalker(Channel channel) throws InvalidArgumentException, ProposalException, IOException {
        try {
            BlockchainInfo channelInfo = channel.queryBlockchainInfo();

            for (long current = channelInfo.getHeight() - 1; current > -1; --current) {
                BlockInfo returnedBlock = channel.queryBlockByNumber(current);
                final long blockNumber = returnedBlock.getBlockNumber();

                out("current block number %d has data hash: %s", blockNumber, Hex.encodeHexString(returnedBlock.getDataHash()));
                out("current block number %d has previous hash id: %s", blockNumber, Hex.encodeHexString(returnedBlock.getPreviousHash()));
                out("current block number %d has calculated block hash is %s", blockNumber, Hex.encodeHexString(SDKUtils.calculateBlockHash(blockNumber, returnedBlock.getPreviousHash(), returnedBlock.getDataHash())));

                final int envelopeCount = returnedBlock.getEnvelopeCount();
                out("current block number %d has %d envelope count:", blockNumber, returnedBlock.getEnvelopeCount());
                int i = 0;
                for (BlockInfo.EnvelopeInfo envelopeInfo : returnedBlock.getEnvelopeInfos()) {
                    ++i;

                    out("  Transaction number %d has transaction id: %s", i, envelopeInfo.getTransactionID());
                    final String channelId = envelopeInfo.getChannelId();

                    out("  Transaction number %d has channel id: %s", i, channelId);
                    out("  Transaction number %d has epoch: %d", i, envelopeInfo.getEpoch());
                    out("  Transaction number %d has transaction timestamp: %tB %<te,  %<tY  %<tT %<Tp", i, envelopeInfo.getTimestamp());
                    out("  Transaction number %d has type id: %s", i, "" + envelopeInfo.getType());

                    if (envelopeInfo.getType() == TRANSACTION_ENVELOPE) {
                        BlockInfo.TransactionEnvelopeInfo transactionEnvelopeInfo = (BlockInfo.TransactionEnvelopeInfo) envelopeInfo;

                        out("  Transaction number %d has %d actions", i, transactionEnvelopeInfo.getTransactionActionInfoCount());
                        out("  Transaction number %d isValid %b", i, transactionEnvelopeInfo.isValid());
                        out("  Transaction number %d validation code %d", i, transactionEnvelopeInfo.getValidationCode());

                        int j = 0;
                        for (BlockInfo.TransactionEnvelopeInfo.TransactionActionInfo transactionActionInfo : transactionEnvelopeInfo.getTransactionActionInfos()) {
                            ++j;
                            out("   Transaction action %d has response status %d", j, transactionActionInfo.getResponseStatus());
                            out("   Transaction action %d has response message bytes as string: %s", j,
                                    printableString(new String(transactionActionInfo.getResponseMessageBytes(), "UTF-8")));
                            out("   Transaction action %d has %d endorsements", j, transactionActionInfo.getEndorsementsCount());

                            for (int n = 0; n < transactionActionInfo.getEndorsementsCount(); ++n) {
                                BlockInfo.EndorserInfo endorserInfo = transactionActionInfo.getEndorsementInfo(n);
                                out("Endorser %d signature: %s", n, Hex.encodeHexString(endorserInfo.getSignature()));
                                out("Endorser %d endorser: %s", n, new String(endorserInfo.getEndorser(), "UTF-8"));
                            }
                            out("   Transaction action %d has %d chaincode input arguments", j, transactionActionInfo.getChaincodeInputArgsCount());
                            for (int z = 0; z < transactionActionInfo.getChaincodeInputArgsCount(); ++z) {
                                out("     Transaction action %d has chaincode input argument %d is: %s", j, z,
                                        printableString(new String(transactionActionInfo.getChaincodeInputArgs(z), "UTF-8")));
                            }

                            out("   Transaction action %d proposal response status: %d", j,
                                    transactionActionInfo.getProposalResponseStatus());
                            out("   Transaction action %d proposal response payload: %s", j,
                                    printableString(new String(transactionActionInfo.getProposalResponsePayload())));

                            // Check to see if we have our expected event.
//                            if (blockNumber == 2) {
//                                ChaincodeEvent chaincodeEvent = transactionActionInfo.getEvent();
//                            }

                            TxReadWriteSetInfo rwsetInfo = transactionActionInfo.getTxReadWriteSet();
                            if (null != rwsetInfo) {
                                out("   Transaction action %d has %d name space read write sets", j, rwsetInfo.getNsRwsetCount());

                                for (TxReadWriteSetInfo.NsRwsetInfo nsRwsetInfo : rwsetInfo.getNsRwsetInfos()) {
                                    final String namespace = nsRwsetInfo.getNamespace();
                                    KvRwset.KVRWSet rws = nsRwsetInfo.getRwset();

                                    int rs = -1;
                                    for (KvRwset.KVRead readList : rws.getReadsList()) {
                                        rs++;

                                        out("     Namespace %s read set %d key %s  version [%d:%d]", namespace, rs, readList.getKey(),
                                                readList.getVersion().getBlockNum(), readList.getVersion().getTxNum());

                                        if ("bar".equals(channelId) && blockNumber == 2) {
                                            if ("example_cc_go".equals(namespace)) {
                                                if (rs == 0) {
                                                } else if (rs == 1) {
                                                } else {
                                                    fail(format("unexpected readset %d", rs));
                                                }

                                                TX_EXPECTED.remove("readset1");
                                            }
                                        }
                                    }

                                    rs = -1;
                                    for (KvRwset.KVWrite writeList : rws.getWritesList()) {
                                        rs++;
                                        String valAsString = printableString(new String(writeList.getValue().toByteArray(), "UTF-8"));

                                        out("     Namespace %s write set %d key %s has value '%s' ", namespace, rs,
                                                writeList.getKey(),
                                                valAsString);

                                        if ("bar".equals(channelId) && blockNumber == 2) {
                                            if (rs == 0) {
                                            } else if (rs == 1) {
                                            } else {
                                                fail(format("unexpected writeset %d", rs));
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
                fail(TX_EXPECTED.get(0));
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

        ret = ret.substring(0, Math.min(ret.length(), maxLogStringLength)) + (ret.length() > maxLogStringLength ? "..." : "");

        return ret;

    }

    private static CompletableFuture<BlockEvent.TransactionEvent> installChaincode(HFClient client, Channel channel, SampleOrg sampleOrg, Collection<ProposalResponse> successful, Collection<ProposalResponse> failed) throws InvalidArgumentException, ProposalException, IOException, ChaincodeEndorsementPolicyParseException {
        ////////////////////////////
        // Install Proposal Request

        final String channelName = channel.getName();
        boolean isFooChain = FOO_CHANNEL_NAME.equals(channelName);
        out("Running channel %s", channelName);
        channel.setTransactionWaitTime(testConfig.getTransactionWaitTime());
        channel.setDeployWaitTime(testConfig.getDeployWaitTime());

        Collection<Orderer> orderers = channel.getOrderers();

        Collection<ProposalResponse> responses;
        //

        client.setUserContext(sampleOrg.getPeerAdmin());

        out("Creating install proposal");

        InstallProposalRequest installProposalRequest = client.newInstallProposalRequest();
        installProposalRequest.setChaincodeID(chaincodeID);

        if (isFooChain) {
            // on foo chain install from directory.
            ////For GO language and serving just a single user, chaincodeSource is mostly likely the users GOPATH
            installProposalRequest.setChaincodeSourceLocation(new File(TEST_FIXTURES_PATH + "/sdkintegration/gocc/sample1"));
        } else {
            // On bar chain install from an input stream.
            installProposalRequest.setChaincodeInputStream(Util.generateTarGzInputStream(
                    (Paths.get(TEST_FIXTURES_PATH, "/sdkintegration/gocc/sample1", "src", CHAIN_CODE_PATH).toFile()),
                    Paths.get("src", CHAIN_CODE_PATH).toString()));
        }

        installProposalRequest.setChaincodeVersion(CHAIN_CODE_VERSION);
        out("Sending install proposal");

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
                out("Successful install proposal response Txid: %s from peer %s", response.getTransactionID(), response.getPeer().getName());
                successful.add(response);
            } else {
                failed.add(response);
            }
        }
        //   }
        out("Received %d install proposal responses. Successful+verified: %d . Failed: %d", numInstallProposal, successful.size(), failed.size());
        if (failed.size() > 0) {
            ProposalResponse first = failed.iterator().next();
            fail("Not enough endorsers for install :" + successful.size() + ".  " + first.getMessage());
        }


        //   client.setUserContext(sampleOrg.getUser(TEST_ADMIN_NAME));
        //  final ChaincodeID chaincodeID = firstInstallProposalResponse.getChaincodeID();
        // Note installing chaincode does not require transaction no need to
        // send to Orderers

        ///////////////
        //// Instantiate chaincode.
        InstantiateProposalRequest instantiateProposalRequest = client.newInstantiationProposalRequest();
        instantiateProposalRequest.setProposalWaitTime(testConfig.getProposalWaitTime());
        instantiateProposalRequest.setChaincodeID(chaincodeID);
        instantiateProposalRequest.setFcn("init");
        instantiateProposalRequest.setArgs(new String[]{"a", "500", "b", "" + (200 + DELTA)});
        Map<String, byte[]> tm = new HashMap<>();
        tm.put("HyperLedgerFabric", "InstantiateProposalRequest:JavaSDK".getBytes(UTF_8));
        tm.put("method", "InstantiateProposalRequest".getBytes(UTF_8));
        instantiateProposalRequest.setTransientMap(tm);

    /*
      policy OR(Org1MSP.member, Org2MSP.member) meaning 1 signature from someone in either Org1 or Org2
      See README.md Chaincode endorsement policies section for more details.
    */
        ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
        chaincodeEndorsementPolicy.fromYamlFile(new File(TEST_FIXTURES_PATH + "/sdkintegration/chaincodeendorsementpolicy.yaml"));
        instantiateProposalRequest.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);

        out("Sending instantiateProposalRequest to all peers with arguments: a and b set to 100 and %s respectively", "" + (200 + DELTA));
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
                out("Succesful instantiate proposal response Txid: %s from peer %s", response.getTransactionID(), response.getPeer().getName());
            } else {
                failed.add(response);
            }
        }
        out("Received %d instantiate proposal responses. Successful+verified: %d . Failed: %d", responses.size(), successful.size(), failed.size());
        if (failed.size() > 0) {
            ProposalResponse first = failed.iterator().next();
            fail("Not enough endorsers for instantiate :" + successful.size() + "endorser failed with " + first.getMessage() + ". Was verified:" + first.isVerified());
        }
        ///////////////
        /// Send instantiate transaction to orderer
        out("Sending instantiateTransaction to orderer with a and b set to 100 and %s respectively", "" + (200 + DELTA));
        return channel.sendTransaction(successful, orderers);

    }

    private static CompletableFuture<BlockEvent.TransactionEvent> invokeChaincode(HFClient client, Channel channel, SampleOrg sampleOrg, Collection<ProposalResponse> successful, Collection<ProposalResponse> failed) {
        try {

            client.setUserContext(sampleOrg.getUser(TESTUSER_1_NAME));

            ///////////////
            /// Send transaction proposal to all peers
            TransactionProposalRequest transactionProposalRequest = client.newTransactionProposalRequest();
            transactionProposalRequest.setChaincodeID(chaincodeID);
            transactionProposalRequest.setFcn("invoke");
            transactionProposalRequest.setProposalWaitTime(testConfig.getProposalWaitTime());
            transactionProposalRequest.setArgs(new String[]{"move", "a", "b", "100"});

            Map<String, byte[]> tm2 = new HashMap<>();
            tm2.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8)); //Just some extra junk in transient map
            tm2.put("method", "TransactionProposalRequest".getBytes(UTF_8)); // ditto
            tm2.put("result", ":)".getBytes(UTF_8));  // This should be returned see chaincode why.
            tm2.put(EXPECTED_EVENT_NAME, EXPECTED_EVENT_DATA);  //This should trigger an event see chaincode why.

            transactionProposalRequest.setTransientMap(tm2);

            out("sending transactionProposal to all peers with arguments: move(a,b,100)");

            Collection<ProposalResponse> transactionPropResp = channel.sendTransactionProposal(transactionProposalRequest, channel.getPeers());
            for (ProposalResponse response : transactionPropResp) {
                if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                    out("Successful transaction proposal response Txid: %s from peer %s", response.getTransactionID(), response.getPeer().getName());
                    successful.add(response);
                } else {
                    failed.add(response);
                }
            }

            // Check that all the proposals are consistent with each other. We should have only one set
            // where all the proposals above are consistent. Note the when sending to Orderer this is done automatically.
            //  Shown here as an example that applications can invoke and select.
            // See org.hyperledger.fabric.sdk.proposal.consistency_validation config property.
            Collection<Set<ProposalResponse>> proposalConsistencySets = SDKUtils.getProposalConsistencySets(transactionPropResp);
            if (proposalConsistencySets.size() != 1) {
                fail(format("Expected only one set of consistent proposal responses but got %d", proposalConsistencySets.size()));
            }

            out("Received %d transaction proposal responses. Successful+verified: %d . Failed: %d",
                    transactionPropResp.size(), successful.size(), failed.size());
            if (failed.size() > 0) {
                ProposalResponse firstTransactionProposalResponse = failed.iterator().next();
                fail("Not enough endorsers for invoke(move a,b,100):" + failed.size() + " endorser error: " +
                        firstTransactionProposalResponse.getMessage() +
                        ". Was verified: " + firstTransactionProposalResponse.isVerified());
            }
            out("Successfully received transaction proposal responses.");

            ProposalResponse resp = transactionPropResp.iterator().next();
            byte[] x = resp.getChaincodeActionResponsePayload(); // This is the data returned by the chaincode.
            String resultAsString = null;
            if (x != null) {
                resultAsString = new String(x, "UTF-8");
            }

            TxReadWriteSetInfo readWriteSetInfo = resp.getChaincodeActionResponseReadWriteSetInfo();
            //See blockwalker below how to transverse this

            ChaincodeID cid = resp.getChaincodeID();

            ////////////////////////////
            // Send Transaction Transaction to orderer
            out("Sending chaincode transaction(move a,b,100) to orderer.");
            return channel.sendTransaction(successful);
            //.get(testConfig.getTransactionWaitTime(), TimeUnit.SECONDS);

        } catch (Exception e) {
            out("Caught an exception while invoking chaincode");
            e.printStackTrace();
            fail("Failed invoking chaincode with error : " + e.getMessage());
        }

        return null;
    }

    public static void fail(String message) {
        if (message == null) {
            throw new RuntimeException();
        } else {
            throw new RuntimeException(message);
        }
    }

}
