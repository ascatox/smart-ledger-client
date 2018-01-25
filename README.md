# LedgerClient Library for Hyperledger Fabric 1.0

**LedgerClient** is a *Java 8* library to interface with an [Hyperledger Fabric](https://hyperledger-fabric.readthedocs.io/en/latest/) blockchain using the [Hyperledger Fabric SDK Java](https://github.com/hyperledger/fabric-sdk-java).

The [Chaincode](https://github.com/ascatox/smart-ledger-client/blob/master/src/main/java/fixture/sdkintegration/gocc/smartfactory/src/github.com/smartfactory/smartfactory.go) in Go Language, to interact with the Smart Factory Lab, is bundled with the project.
# Setup HLF
In order to use the Library, launch Fabric as described in the [official docs](https://hyperledger-fabric.readthedocs.io/en/latest/) in the section [Write your First Application](https://hyperledger-fabric.readthedocs.io/en/release/write_first_app.html).<br/>
# Install and instantiate the chaincode
`git clone https://github.com/ascatox/smart-ledger-client.git && cd smartLedgerClient`<br/>
`cp -rf ./smartLedgerClient/src/main/java/fixture/sdkintegration/gocc/smartfactory/src/github.com/smartfactory/ <YOUR_FABRIC_SAMPLES_DIR>/chaincode`<br/>
`docker exec -it cli bash`<br/>
`peer chaincode install -p github.com/smartfactory -n smartfactory -v 1.0`<br/>
`peer chaincode instantiate -n smartfactory -v 1.0 –c ’{“Args”:[]}’ –C mychannel `<br/>
	
# Configure the LedgerClient
Edit the file `config-network.properties`[*](https://github.com/ascatox/smart-ledger-client/blob/master/src/main/resources/config-network.properties) with your favourite text editor in order to configure the network as in your HLF previous installation. Under you can find a complete example of configured file: <br/>
`vim config-network.properties` 

	numberOrgs=1
	numberPeers=1

	peerOrg1.mspid: Org1MSP
	peerOrg1.domname: org1.example.com
	peerOrg1.ca_location: http://localhost:7054
	peerOrg1.caName: ca.example.com
	peerOrg1.peer_locations_0: peer0.org1.example.com@grpc://localhost:7051
	# peerOrg1.peer_locations_1: peer1.org1.example.com@grpc://localhost:8051
	peerOrg1.orderer_locations: orderer.example.com@grpc://localhost:7050
	peerOrg1.eventhub_locations: peer0.org1.example.com@grpc://localhost:7053

	integrationtests.tls=null
	cryptoConfigDir=/crypto-config
	channelName=mychannel

Copy your HLF `crypto-config` dir under your **HOME** directory <br/>
`mvn verify` (Launch the integration tests provided) <br/>
`mvn package && mvn install` (This command creates the jar file, ready to be included in your projects)

# JavaDoc
The **JavaDoc** documentation of the Library, is present in the `doc` folder of the project.<br/>
Clone the project or download the [zip file](https://github.com/ascatox/smart-ledger-client/blob/master/doc.zip) and open the `index.html` in the doc folder to explore the documentation, starting from `iSmartLedgerClient`.

# Usage
You can find simple [examples](https://github.com/ascatox/smart-ledger-client/blob/master/src/test/java/eu/faredge/smartledger/client/End2EndTestSmartLedgerClientDSM.java) of usage looking at the **End2End tests** in the `test` folder of projects.
