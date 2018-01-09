# SmartLedgerClient Library for HyperLedger Fabric 1.0

**SmartLedgerClient** is a library to interface with an [Hyperledger Fabric](https://hyperledger-fabric.readthedocs.io/en/latest/) blockchain using the [Hyperledger Fabric SDK Java](https://github.com/hyperledger/fabric-sdk-java).

The [Chaincode](https://github.com/ascatox/smart-ledger-client/blob/master/src/main/java/fixture/sdkintegration/gocc/smartfactory/src/github.com/smartfactory/smartfactory.go) in Go Language, to interact with the Smart Factory Lab, is bundled with the project.

In order to use the Library, launch Fabric as described in the [official docs](https://hyperledger-fabric.readthedocs.io/en/latest/) in the section [Building your First Network](https://hyperledger-fabric.readthedocs.io/en/latest/build_network.html) **installing and instatiating the above Chaincode** and configuring correctly [CouchDB](http://hyperledger-fabric.readthedocs.io/en/release/build_network.html#using-couchdb).<br/>
<br/>Copy your `crypto-config` directory under `<home-dir>/.smart-ledger-creds`.
<br/>Edit the [properties file](https://github.com/ascatox/smart-ledger-client/blob/master/src/main/resources/smart-ledger.properties) with your settings, in particular the `FABRIC_HOST`and the `FABRIC_PEER_HOST` keys.
<br/>Launch `mvn verify` to test your environment.

The **JavaDoc** documentation of the Library, is present in the `doc` folder of the project.<br/>
Clone the project or download the [zip file](https://github.com/ascatox/smart-ledger-client/blob/master/doc.zip) and open the `index.html` in the doc folder to explore the documentation, starting from `iSmartLedgerClient`.

Happy Blockchain 😄
