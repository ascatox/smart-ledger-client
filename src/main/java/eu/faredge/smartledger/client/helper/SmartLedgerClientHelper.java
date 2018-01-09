package eu.faredge.smartledger.client.helper;

import eu.faredge.smartledger.client.exception.SmartLedgerClientException;
import eu.faredge.smartledger.client.model.SampleOrg;
import eu.faredge.smartledger.client.model.SampleStore;
import eu.faredge.smartledger.client.model.SampleUser;
import eu.faredge.smartledger.client.testutils.TestConfig;
import eu.faredge.smartledger.client.util.Util;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.hyperledger.fabric.protos.ledger.rwset.kvrwset.KvRwset;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.*;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hyperledger.fabric.sdk.BlockInfo.EnvelopeType.TRANSACTION_ENVELOPE;

public class SmartLedgerClientHelper {
    private static ResourceBundle finder = ResourceBundle.getBundle("smart-ledger");
    private static final TestConfig testConfig = TestConfig.getConfig();
    private static final String TEST_FIXTURES_PATH = finder.getString("TEST_FIXTURES_PATH");

    private static final String CHAIN_CODE_NAME = finder.getString("CHAIN_CODE_NAME");
    private static final String CHAIN_CODE_PATH = finder.getString("CHAIN_CODE_PATH");
    private static final String CHAIN_CODE_VERSION = finder.getString("CHAIN_CODE_VERSION");

    private static final String FOO_CHANNEL_NAME = finder.getString("CHANNEL_NAME");

    private static final byte[] EXPECTED_EVENT_DATA = "!".getBytes(UTF_8);
    private static final String EXPECTED_EVENT_NAME = "event";

    private static String testTxID = null;  // save the CC invoke TxID and use in queries

    private static ChaincodeID chaincodeID = null;
    private static final int DELTA = 100;
    private static HFClient client = HFClient.createNewInstance();
    private static Collection<Orderer> orderers = null;

    private static SampleUser user = null;
    private static SampleUser admin = null;
    //private static SampleUser peerOrgAdmin = null;

    private static final Map<String, String> TX_EXPECTED;

    static {
        TX_EXPECTED = new HashMap<>();
        TX_EXPECTED.put("readset1", "Missing readset for channel bar block 1");
        TX_EXPECTED.put("writeset1", "Missing writeset for channel bar block 1");
    }


    public static void checkConfig(SampleOrg sampleOrg) throws SmartLedgerClientException {
        Util.out("\n\n\nRUNNING: ISmartLedgerClient.\n");

        try {
            chaincodeID = ChaincodeID.newBuilder().setName(CHAIN_CODE_NAME)
                    .setVersion(CHAIN_CODE_VERSION)
                    .setPath(CHAIN_CODE_PATH).build();

            //Set up hfca for each sample org

            String caName = sampleOrg.getCAName(); //Try one of each name and no name.
            if (caName != null && !caName.isEmpty()) {
                sampleOrg.setCAClient(HFCAClient.createNewInstance(caName, sampleOrg.getCALocation(), sampleOrg
                        .getCAProperties()));
            } else {
                sampleOrg.setCAClient(HFCAClient.createNewInstance(sampleOrg.getCALocation(), sampleOrg
                        .getCAProperties()));
            }
        } catch (Exception e) {
            throw new SmartLedgerClientException(e);
        }
    }

    public static void setup(SampleOrg sampleOrg, String userName, String enrollmentSecret) throws
            SmartLedgerClientException {
        try {
            ////////////////////////////
            // Setup client

            client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
            // client.setMemberServices(peerOrg1FabricCA);

            ////////////////////////////
            //Set up USERS

            //Persistence is not part of SDK. Sample file store is for demonstration purposes only!
            //   MUST be replaced with more robust application implementation  (Database, LDAP)
            File sampleStoreFile = new File(System.getProperty("user.home") + "/.smartLedgerClientProps" +
                    ".properties");
            //if (sampleStoreFile.exists()) { //For testing start fresh
            //  sampleStoreFile.delete();
            //}

            final SampleStore sampleStore = new SampleStore(sampleStoreFile);
            HFCAClient ca = sampleOrg.getCAClient();

            final String orgName = sampleOrg.getName();
            final String mspid = sampleOrg.getMSPID();
            ca.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
/*
            admin = sampleStore.getMember(TEST_ADMIN_NAME, orgName);
            if (!admin.isEnrolled()) {  //Preregistered admin only needs to be enrolled with Fabric caClient.
                admin.setEnrollment(ca.enroll(admin.getName(), "adminpw"));
                admin.setMspId(mspid);
            }
            sampleOrg.setAdmin(admin); // The admin of this org --*/

            user = sampleStore.getMember(userName, sampleOrg.getName());
//TODO @ascatox Understand how to mark an CaUser as registered and enrolled
            /*if (!user.isRegistered()) {  // users need to be registered AND enrolled
                RegistrationRequest rr = new RegistrationRequest(user.getName(), "org1.department1");
                user.setEnrollmentSecret(ca.register(rr, admin));
                enrollmentSecret = user.getEnrollmentSecret();
            }
            if (!user.isEnrolled()) {
                user.setEnrollment(ca.enroll(user.getName(), enrollmentSecret));
                user.setMspId(mspid);
            }*/
            //sampleOrg.addUser(user); //Remember user belongs to this Org
            final String sampleOrgName = sampleOrg.getName();
            final String sampleOrgDomainName = sampleOrg.getDomainName();

            final Path path = Paths.get(testConfig.getHomeDirPath(),
                    "crypto-config/peerOrganizations/",
                    sampleOrgDomainName, format("/users/Admin@%s/msp/keystore", sampleOrgDomainName));
            final Path path1 = Paths.get(testConfig.getHomeDirPath(),
                    "crypto-config/peerOrganizations/",
                    sampleOrgDomainName,
                    format("/users/Admin@%s/msp/signcerts/Admin@%s-cert.pem", sampleOrgDomainName,
                            sampleOrgDomainName));

            SampleUser peerOrgAdmin = sampleStore.getMember(sampleOrgName + "Admin", sampleOrgName, sampleOrg
                            .getMSPID(),
                    Util.findFileSk(path
                            .toFile()),
                    path1.toFile());

          /*SampleUser peerOrgAdmin = sampleStore.getMember("peerAdmin", orgName);
            if (!peerOrgAdmin.isEnrolled()) {  //Preregistered admin only needs to be enrolled with Fabric caClient.
                peerOrgAdmin.setEnrollment(ca.enroll(peerOrgAdmin.getName(), "dyackLBnOLRM"));
                peerOrgAdmin.setMspId(mspid);
            }*/
            sampleOrg.setPeerAdmin(peerOrgAdmin); //A special user that can create channels, join peers and
            sampleOrg.addUser(peerOrgAdmin);
            user = peerOrgAdmin; //TODO
        } catch (Exception e) {
            throw new SmartLedgerClientException(e);
        }
    }

    public static List<String[]> queryChainCode(Channel channel, String functionName, String[] args) throws
            SmartLedgerClientException {
        return queryChainCode(client, channel, functionName, null, args);
    }

    private static List<String[]> queryChainCode(HFClient client, Channel channel, String functionName,
                                                 BlockEvent.TransactionEvent transactionEvent, String[] args) throws
            SmartLedgerClientException {
        try {
            if (null != transactionEvent) {
                waitOnFabric(0);
                Util.out("Finished transaction with transaction id %s", transactionEvent.getTransactionID());
                testTxID = transactionEvent.getTransactionID(); // used in the channel queries later
            }
            ////////////////////////////
            // Send Query Proposal to all peers
            //
            Util.out("Now query chaincode for the values rquired.");
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
                    Util.out("Query payload from peer %s returned %s", proposalResponse.getPeer().getName(),
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
            throw new SmartLedgerClientException("Failed during chaincode query with error : " + e.getMessage());
        }
    }

    public static CompletableFuture<BlockEvent.TransactionEvent> invokeChaincode(Channel channel, String
            functionName, String[] args)
            throws Exception {
        return invokeChaincode(client, channel, chaincodeID, functionName, args, user);
    }

    private static CompletableFuture<BlockEvent.TransactionEvent> invokeChaincode(HFClient client, Channel channel,
                                                                                  ChaincodeID chaincodeID,
                                                                                  String functionName, String[]
                                                                                          args, User user) throws
            SmartLedgerClientException {
        try {
            Collection<ProposalResponse> successful = new LinkedList<>();
            Collection<ProposalResponse> failed = new LinkedList<>();

            ///////////////
            /// Send transaction proposal to all peers
            TransactionProposalRequest transactionProposalRequest = client.newTransactionProposalRequest();
            transactionProposalRequest.setChaincodeID(chaincodeID);
            transactionProposalRequest.setFcn(functionName);
            transactionProposalRequest.setArgs(args);
            transactionProposalRequest.setProposalWaitTime(testConfig.getProposalWaitTime());
            if (user != null) { // specific user use that
                transactionProposalRequest.setUserContext(user);
            }
            Util.out("sending transaction proposal to all peers with arguments: (" + StringUtils.join(args, ",") +
                    "\"");

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

                throw new ProposalException(format("Not enough endorsers for invoke(" + StringUtils.join(args, ",") +
                                ")" +
                                ":%d " +
                                "endorser " +
                                "error:%s. Was verified:%b",
                        args[args.length - 1], firstTransactionProposalResponse.getStatus().getStatus(),
                        firstTransactionProposalResponse.getMessage(),
                        firstTransactionProposalResponse.isVerified()));
            }
            Util.out("Successfully received transaction proposal responses.");

            ////////////////////////////
            // Send transaction to orderer
            Util.out("Sending chaincode transaction " + functionName + " to orderer.");
            if (user != null) {
                return channel.sendTransaction(successful, user);
            }
            return channel.sendTransaction(successful);
        } catch (Exception e) {
            throw new SmartLedgerClientException(e);
        }
    }

    public static Channel initializeChannel(String name, SampleOrg sampleOrg) throws Exception {
        return initializeChannel(name, client, sampleOrg);
    }

    private static Channel initializeChannel(String name, HFClient client, SampleOrg sampleOrg) throws
            SmartLedgerClientException {
        ////////////////////////////
        //Initialize the channel
        //
        try {
            Util.out("Constructing channel java structures %s", name);

            //Only peer Admin org
            client.setUserContext(sampleOrg.getPeerAdmin());

            orderers = new LinkedList<>();

            for (String orderName : sampleOrg.getOrdererNames()) {

                Properties ordererProperties = testConfig.getOrdererProperties(orderName);
                //example of setting keepAlive to avoid timeouts on inactive http2 connections.
                // Under 5 minutes would require changes to server side to accept faster ping rates.
                ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTime", new Object[]{5L, TimeUnit
                        .MINUTES});
                ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTimeout", new Object[]{8L, TimeUnit
                        .SECONDS});

                orderers.add(client.newOrderer(orderName, sampleOrg.getOrdererLocation(orderName),
                        ordererProperties));
            }

            //Just pick the first orderer in the list to create the channel.

            Orderer anOrderer = orderers.iterator().next();
            Channel newChannel = client.getChannel(name);
            if (null == newChannel) {
                // @ascatox Constructs a new channel
                newChannel = client.newChannel(name);
            }
            //Util.out("Created channel %s", name);
            newChannel.addOrderer(anOrderer);

            for (String peerName : sampleOrg.getPeerNames()) {
                String peerLocation = sampleOrg.getPeerLocation(peerName);

                Properties peerProperties = testConfig.getPeerProperties(peerName); //CaUser properties for peer.. if
                // any.
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

            for (Orderer orderer : orderers) { //add remaining orderers if any.
                if (!orderer.equals(anOrderer))
                    newChannel.addOrderer(orderer);
            }
            for (String eventHubName : sampleOrg.getEventHubNames()) {

                final Properties eventHubProperties = testConfig.getEventHubProperties(eventHubName);

                eventHubProperties.put("grpc.NettyChannelBuilderOption.keepAliveTime", new Object[]{5L, TimeUnit
                        .MINUTES});
                eventHubProperties.put("grpc.NettyChannelBuilderOption.keepAliveTimeout", new Object[]{8L, TimeUnit
                        .SECONDS});

                EventHub eventHub = client.newEventHub(eventHubName, sampleOrg.getEventHubLocation(eventHubName),
                        eventHubProperties);
                newChannel.addEventHub(eventHub);
            }
            newChannel.initialize(); //There's no need to initialize the channel we are only building the java
            // structures.
            Util.out("Finished initialization channel java structures %s", name);
            return newChannel;
        } catch (InvalidArgumentException e) {
            throw new SmartLedgerClientException(e);
       /* } catch (TransactionException e) {
            throw new SmartLedgerClientException(e); */
        } catch (Exception e) {
            throw new SmartLedgerClientException(e);
        }
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

    public static void installChaincode(Channel channel, SampleOrg sampleOrg) throws SmartLedgerClientException {
        Collection<ProposalResponse> successful = new ArrayList<>();
        Collection<ProposalResponse> failed = new ArrayList<>();
        installChaincode(client, channel, sampleOrg, successful, failed);
    }

    private static void installChaincode(HFClient client, Channel channel, SampleOrg sampleOrg,
                                         Collection<ProposalResponse> successful, Collection<ProposalResponse> failed)
            throws SmartLedgerClientException {
        ////////////////////////////
        // Install Proposal Request
        try {
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
        } catch (InvalidArgumentException e) {
            throw new SmartLedgerClientException(e);
        } catch (IOException e) {
            throw new SmartLedgerClientException(e);
        } catch (ProposalException e) {
            throw new SmartLedgerClientException(e);
        } catch (Exception e) {
            throw new SmartLedgerClientException(e);
        }
    }


    public static CompletableFuture<BlockEvent.TransactionEvent> instantiateOrUpgradeChaincode(Channel channel,
                                                                                               String[] args, boolean
                                                                                                       isUpgrade)
            throws SmartLedgerClientException {
        if (isUpgrade)
            return upgradeChaincode(channel, args);
        else
            return instantiateChaincode(channel, args);
    }

    public static CompletableFuture<BlockEvent.TransactionEvent> instantiateChaincode(Channel channel, String[] args)
            throws SmartLedgerClientException {
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
            throws SmartLedgerClientException {
        try {
            Collection<ProposalResponse> responses;///////////////
            //// Instantiate chaincode.
            InstantiateProposalRequest instantiateProposalRequest = client.newInstantiationProposalRequest();
            instantiateProposalRequest.setProposalWaitTime(testConfig.getProposalWaitTime());
            instantiateProposalRequest.setChaincodeID(chaincodeID);
            String function = "init";
            instantiateProposalRequest.setFcn(function);
            instantiateProposalRequest.setArgs(args);
            Map<String, byte[]> tm = new HashMap<>();
            tm.put("HyperLedgerFabric", "InstantiateProposalRequest:JavaSDK".getBytes(UTF_8));
            tm.put("method", "InstantiateProposalRequest".getBytes(UTF_8));
            instantiateProposalRequest.setTransientMap(tm);

            ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
            chaincodeEndorsementPolicy.fromYamlFile(new File(TEST_FIXTURES_PATH +
                    "/sdkintegration/chaincodeendorsementpolicy.yaml"));
            instantiateProposalRequest.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);

            Util.out("Sending instantiateProposalRequest to all peers with arguments: " + StringUtils.join(args, ",") +
                    " %s" +
                    " " +
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
                    Util.out("Successful instantiate proposal response Txid: %s from peer %s", response
                                    .getTransactionID()
                            , response.getPeer().getName());
                } else {
                    failed.add(response);
                }
            }
            Util.out("Received %d instantiate proposal responses. Successful+verified: %d . Failed: %d", responses
                            .size()
                    , successful.size(), failed.size());
            if (failed.size() > 0) {
                ProposalResponse first = failed.iterator().next();
                Util.fail(first.getMessage() + ". Was verified:" + first.isVerified());
            }
            ///////////////
            /// Send instantiate transaction to orderer
            Util.out("Sending instantiateTransaction to orderer %s respectively", "" + (200 + DELTA));
            return channel.sendTransaction(successful, orderers);
        } catch (InvalidArgumentException e) {
            throw new SmartLedgerClientException(e);
        } catch (IOException e) {
            throw new SmartLedgerClientException(e);
        } catch (ChaincodeEndorsementPolicyParseException e) {
            throw new SmartLedgerClientException(e);
        } catch (ProposalException e) {
            throw new SmartLedgerClientException(e);
        } catch (Exception e) {
            throw new SmartLedgerClientException(e);
        }
    }


    public static CompletableFuture<BlockEvent.TransactionEvent> upgradeChaincode(Channel channel, String[] args
    )
            throws SmartLedgerClientException {
        Collection<ProposalResponse> successful = new ArrayList<>();
        Collection<ProposalResponse> failed = new ArrayList<>();
        return upgradeChaincode(client, channel, args, successful, failed, true, orderers);
    }

    private static CompletableFuture<BlockEvent.TransactionEvent> upgradeChaincode(HFClient client, Channel
            channel, String[] args,
                                                                                   Collection<ProposalResponse>
                                                                                           successful,
                                                                                   Collection<ProposalResponse>
                                                                                           failed, boolean
                                                                                           isFooChain,
                                                                                   Collection<Orderer> orderers)
            throws SmartLedgerClientException {
        try {
            Collection<ProposalResponse> responses;
            //// Upgrade chaincode.
            UpgradeProposalRequest upgradeProposalRequest = client.newUpgradeProposalRequest();
            upgradeProposalRequest.setProposalWaitTime(testConfig.getProposalWaitTime());
            upgradeProposalRequest.setChaincodeID(chaincodeID);
            String function = "init";
            upgradeProposalRequest.setFcn(function);
            upgradeProposalRequest.setArgs(args);
            Map<String, byte[]> tm = new HashMap<>();
            tm.put("HyperLedgerFabric", "UpgradeProposalRequest:JavaSDK".getBytes(UTF_8));
            tm.put("method", "UpgradeProposalRequest".getBytes(UTF_8));
            upgradeProposalRequest.setTransientMap(tm);

            ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
            chaincodeEndorsementPolicy.fromYamlFile(new File(TEST_FIXTURES_PATH +
                    "/sdkintegration/chaincodeendorsementpolicy.yaml"));
            upgradeProposalRequest.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);

            Util.out("Sending upgradeProposalRequest to all peers with arguments: " + StringUtils.join(args, ",") + "" +
                    " %s" +
                    " " +
                    "respectively", "" + (200 + DELTA));
            successful.clear();
            failed.clear();

            if (isFooChain) {  //Send responses both ways with specifying peers and by using those on the channel.
                responses = channel.sendUpgradeProposal(upgradeProposalRequest, channel.getPeers());
            } else {
                responses = channel.sendUpgradeProposal(upgradeProposalRequest);
            }
            for (ProposalResponse response : responses) {
                if (response.isVerified() && response.getStatus() == ProposalResponse.Status.SUCCESS) {
                    successful.add(response);
                    Util.out("Successful upgrade proposal response Txid: %s from peer %s", response.getTransactionID()
                            , response.getPeer().getName());
                } else {
                    failed.add(response);
                }
            }
            Util.out("Received %d upgrade proposal responses. Successful+verified: %d . Failed: %d", responses.size()
                    , successful.size(), failed.size());
            if (failed.size() > 0) {
                ProposalResponse first = failed.iterator().next();
                Util.fail(first.getMessage() + ". Was verified:" + first.isVerified());
            }
            ///////////////
            /// Send upgrade transaction to orderer
            Util.out("Sending upgradeTransaction to orderer %s respectively", "" + (200 + DELTA));
            return channel.sendTransaction(successful, orderers);
        } catch (InvalidArgumentException e) {
            throw new SmartLedgerClientException(e);
        } catch (IOException e) {
            throw new SmartLedgerClientException(e);
        } catch (ChaincodeEndorsementPolicyParseException e) {
            throw new SmartLedgerClientException(e);
        } catch (ProposalException e) {
            throw new SmartLedgerClientException(e);
        } catch (Exception e) {
            throw new SmartLedgerClientException(e);
        }
    }

}