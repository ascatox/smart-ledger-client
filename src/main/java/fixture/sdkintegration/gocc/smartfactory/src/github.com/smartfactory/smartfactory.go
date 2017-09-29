/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * The sample smart contract for documentation topic:
 * Writing Your First Blockchain Application
 */

package main

/* Imports
 * 4 utility libraries for formatting, handling bytes, reading and writing JSON, and string manipulation
 * 2 specific Hyperledger Fabric specific libraries for Smart Contracts
 */
import (
	"bytes"
	"encoding/json"
	"fmt"
	"os"
	"strconv"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	sc "github.com/hyperledger/fabric/protos/peer"
)

var logger = shim.NewLogger("smartfactory")

// Define the device structure, with 4 properties.  Structure tags are used by encoding/json library
type DSM struct {
	PhysicalArtifact     string `json:"physicalArtifact"`
	URI                  string `json:"uri"`
	MACAddress           string `json:"macAddress"`
	DSD                  string `json:"dsd"`
	ConnectionParameters string `json:"connectionParameters"`
	Type                 string `json:"type"`
}

type DCM struct {
	PhysicalArtifact string `json:"physicalArtifact"`
	URI              string `json:"uri"`
	MACAddress       string `json:"macAddress"`
	DSDs             string `json:"dsds"`
	Type             string `json:"type"`
}

/*
 * The Init method is called when the Smart Contract "fabcar" is instantiated by the blockchain network
 * Best practice is to have any Ledger initialization in separate function -- see initLedger()
 */

func (s *DSM) Init(APIstub shim.ChaincodeStubInterface) sc.Response {
	DSMs := []DSM{
		DSM{
			PhysicalArtifact:     "qwerty",
			URI:                  "http://www.google.com",
			MACAddress:           "123:456:789",
			DSD:                  "poiuyt",
			ConnectionParameters: "lkjhgf",
			Type:                 "DSM",
		},
	}
	j := 0
	for j < len(DSMs) {
		fmt.Println("j is ", j)
		DSMAsBytes, _ := json.Marshal(DSMs[j])
		APIstub.PutState("DSM"+strconv.Itoa(j), DSMAsBytes)
		fmt.Println("Added", DSMs[j])
		j = j + 1
	}
	return shim.Success(nil)
}

func (c *DCM) Init(APIstub shim.ChaincodeStubInterface) sc.Response {
	DCMs := []DCM{
		DCM{
			PhysicalArtifact: "zxcvb",
			URI:              "http://www.eng.it",
			MACAddress:       "321:654:987",
			DSDs:             "lkjhg",
			Type:             "DCM",
		},
	}
	k := 0
	for k < len(DCMs) {
		fmt.Println("k is ", k)
		DCMAsBytes, _ := json.Marshal(DCMs[k])
		APIstub.PutState("DCM"+strconv.Itoa(k), DCMAsBytes)
		fmt.Println("Added", DCMs[k])
		k = k + 1
	}

	return shim.Success(nil)
}

func (d *DSM) queryDSM(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {

	if len(args) != 1 {
		return shim.Error("Incorrect number of arguments. Expecting 1")
	}

	dsmAsBytes, _ := APIstub.GetState(args[0])
	return shim.Success(dsmAsBytes)
}

func (d *DCM) queryDCM(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {

	if len(args) != 1 {
		return shim.Error("Incorrect number of arguments. Expecting 1")
	}

	dcmAsBytes, _ := APIstub.GetState(args[0])
	return shim.Success(dcmAsBytes)
}

func (s *DSM) createDSM(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {
	fmt.Println("Entering inside createDSM")
	if len(args) != 6 {
		lenght := strconv.Itoa(len(args))
		str := "Incorrect number of arguments. Expecting 6" + lenght
		return shim.Error(str)
	}

	var dsm = DSM{
		PhysicalArtifact:     args[1],
		URI:                  args[2],
		MACAddress:           args[3],
		DSD:                  args[4],
		ConnectionParameters: args[5],
		Type:                 "DSM",
	}
	// Convert keys to compound key
	dsmAsBytes, _ := json.Marshal(dsm)
	fmt.Println("createDSM PutState", dsmAsBytes)
	APIstub.PutState(args[1], dsmAsBytes)

	return shim.Success(nil)
}

func (c *DCM) createDCM(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {

	if len(args) != 5 {
		return shim.Error("Incorrect number of arguments. Expecting 5")
	}

	var dcm = DCM{
		PhysicalArtifact: args[1],
		URI:              args[2],
		MACAddress:       args[3],
		DSDs:             args[4],
		Type:             "DCM",
	}

	dcmAsBytes, _ := json.Marshal(dcm)
	APIstub.PutState(args[0], dcmAsBytes)

	return shim.Success(nil)
}

func (s *DSM) queryAllDSMs(APIstub shim.ChaincodeStubInterface) sc.Response {
	logger.Error("Entering in queryAllDSMs")
	queryString :=
		"{" +
			"\"selector\": {" +
			"	\"type\": \"DSM\"" +
			" }" +
			" }"
	buffer, error := getQueryResultForQueryString(APIstub, queryString)
	if error != nil {
		fmt.Printf("Error querying all DSM: %s", error)
		return shim.Error("Error querying all DSM") //TODO Error
	}
	return shim.Success(buffer)

}

func (c *DCM) queryAllDCMs(APIstub shim.ChaincodeStubInterface) sc.Response {
	queryString :=
		"{" +
			"\"selector\": {" +
			"	\"type\": \"DCM\"" +
			" }" +
			" }"
	buffer, error := getQueryResultForQueryString(APIstub, queryString)
	if error != nil {
		fmt.Printf("Error querying all DCM: %s", error)
		return shim.Error("Error querying all DCM")
	}
	return shim.Success(buffer)
}

func getQueryResultForQueryString(stub shim.ChaincodeStubInterface, queryString string) ([]byte, error) {
	fmt.Printf("- getQueryResultForQueryString queryString:\n%s\n", queryString)
	resultsIterator, err := stub.GetQueryResult(queryString)
	defer resultsIterator.Close()
	if err != nil {
		return nil, err
	}
	// buffer is a JSON array containing QueryRecords
	var buffer bytes.Buffer
	buffer.WriteString("[")
	bArrayMemberAlreadyWritten := false
	for resultsIterator.HasNext() {
		queryResponse,
			err := resultsIterator.Next()
		if err != nil {
			return nil, err
		}
		// Add a comma before array members, suppress it for the first array member
		if bArrayMemberAlreadyWritten == true {
			buffer.WriteString(",")
		}
		buffer.WriteString("{\"Key\":")
		buffer.WriteString("\"")
		buffer.WriteString(queryResponse.Key)
		buffer.WriteString("\"")
		buffer.WriteString(", \"Record\":")
		// Record is a JSON object, so we write as-is
		buffer.WriteString(string(queryResponse.Value))
		buffer.WriteString("}")
		bArrayMemberAlreadyWritten = true
	}
	buffer.WriteString("]")
	fmt.Printf("- getQueryResultForQueryString queryResult:\n%s\n", buffer.String())
	return buffer.Bytes(), nil
}

func (s *DSM) Invoke(stub shim.ChaincodeStubInterface) sc.Response {
	function, args := stub.GetFunctionAndParameters()
	if function == "qGetAllDSMs" {
		return s.queryAllDSMs(stub)
	} else if function == "iCreateDSM" {
		return s.createDSM(stub, args)
	}
	return shim.Success(nil)
}

func main() {
	fmt.Printf("Main Function")
	logger.SetLevel(shim.LogInfo)
	logLevel, _ := shim.LogLevel(os.Getenv("SHIM_LOGGING_LEVEL"))
	shim.SetLoggingLevel(logLevel)
	// Create a new Smart Contract
	err := shim.Start(new(DSM))
	if err != nil {
		fmt.Printf("Error creating new DSM: %s", err)
	}
	//	err2 := shim.Start(new(DCM))
	//	if err2 != nil {
	//		fmt.Printf("Error creating new DCM: %s", err2)
	//	}

}
