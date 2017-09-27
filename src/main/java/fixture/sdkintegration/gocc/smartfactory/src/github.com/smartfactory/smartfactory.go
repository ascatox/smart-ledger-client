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
	"strconv"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	sc "github.com/hyperledger/fabric/protos/peer"
)

// Define the Smart Contract structure
type SmartContract struct {
}

// Define the device structure, with 4 properties.  Structure tags are used by encoding/json library
type DSM struct {
	PhysicalArtifact     string `json:"physicalArtifact"`
	URI                  string `json:"uri"`
	MACAddress           string `json:"macAddress"`
	DSD                  string `json:"dsd"`
	ConnectionParameters string `json:"connectionParameters"`
}

type DCM struct {
	PhysicalArtifact string `json:"physicalArtifact"`
	URI              string `json:"uri"`
	MACAddress       string `json:"macAddress"`
	DSDs             string `json:"dsds"`
}

type Device struct {
	SerialNumber string `json:"serialnumber"`
}

/*
 * The Init method is called when the Smart Contract "fabcar" is instantiated by the blockchain network
 * Best practice is to have any Ledger initialization in separate function -- see initLedger()
 */

/*func (d *Device) Init(APIstub shim.ChaincodeStubInterface) sc.Response {
	return shim.Success(nil)
}*/

func (s *DSM) Init(APIstub shim.ChaincodeStubInterface) sc.Response {
	DSMs := []DSM{
		DSM{
			PhysicalArtifact:     "qwerty",
			URI:                  "http://www.google.com",
			MACAddress:           "123:456:789",
			DSD:                  "poiuyt",
			ConnectionParameters: "lkjhgf",
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

/*func (c *DCM) Init(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	return nil, nil
}*/

/*
 * The Invoke method is called as a result of an application request to run the Smart Contract "fabcar"
 * The calling application program has also specified the particular smart contract function to be called, with arguments
 */

// func (d *Device) Invoke(APIstub shim.ChaincodeStubInterface) sc.Response {

// 	// Retrieve the requested Smart Contract function and arguments
// 	function, args := APIstub.GetFunctionAndParameters()
// 	// Route to the appropriate handler function to interact with the ledger appropriately
// 	if function == "queryDevice" {
// 		return d.queryDevice(APIstub, args)
// 	} else if function == "createDevice" {
// 		return d.createDevice(APIstub, args)
// 	} else if function == "queryAllDevices" {
// 		return d.queryAllDevices(APIstub)
// 		/*} else if function == "changeCarOwner" {
// 		return s.changeCarOwner(APIstub, args)*/
// 	}

// 	return shim.Error("Invalid Smart Contract function name.")
// }

func (s *DSM) Invoke(APIstub shim.ChaincodeStubInterface) sc.Response {

	// Retrieve the requested Smart Contract function and arguments
	function, args := APIstub.GetFunctionAndParameters()
	// Route to the appropriate handler function to interact with the ledger appropriately
	if function == "queryDSM" {
		return s.queryDSM(APIstub, args)
	} else if function == "createDSM" {
		return s.createDSM(APIstub, args)
	} else if function == "queryAllDSMs" {
		return s.queryAllDSMs(APIstub)
	}
	return shim.Error("Invalid Smart Contract function name.")
}

func (c *DCM) Invoke(APIstub shim.ChaincodeStubInterface) sc.Response {

	// Retrieve the requested Smart Contract function and arguments
	function, args := APIstub.GetFunctionAndParameters()
	// Route to the appropriate handler function to interact with the ledger appropriately
	if function == "queryDCM" {
		return c.queryDCM(APIstub, args)
	} else if function == "createDCM" {
		return c.createDCM(APIstub, args)
	} else if function == "queryAllDCMs" {
		return c.queryAllDCMs(APIstub)
	}
	return shim.Error("Invalid Smart Contract function name.")
}

// func (d *Device) queryDevice(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {

// 	if len(args) != 1 {
// 		return shim.Error("Incorrect number of arguments. Expecting 1")
// 	}

// 	deviceAsBytes, _ := APIstub.GetState(args[0])
// 	return shim.Success(deviceAsBytes)
// }

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

/*
func (d *Device) initLedger(APIstub shim.ChaincodeStubInterface) sc.Response {
	devices := []Device{
		Device{
			SerialNumber: "123456789",
		},
		Device{
			SerialNumber: "987654321",
		},
	}
	i := 0
	for i < len(devices) {
		fmt.Println("i is ", i)
		deviceAsBytes, _ := json.Marshal(devices[i])
		APIstub.PutState("DEVICE"+strconv.Itoa(i), deviceAsBytes)
		fmt.Println("Added", devices[i])
		i = i + 1
	}
	return shim.Success(nil)
}

func (s *DSM) InitLedger(APIstub shim.ChaincodeStubInterface) sc.Response {
	DSMs := []DSM{
		DSM{
			PhysicalArtifact:     "qwerty",
			URI:                  "http://www.google.com",
			MACAddress:           "123:456:789",
			DSD:                  "poiuyt",
			ConnectionParameters: "lkjhgf",
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

func (c *DCM) initLedger(APIstub shim.ChaincodeStubInterface) sc.Response {
	DCMs := []DCM{
		DCM{
			PhysicalArtifact: "zxcvb",
			URI:              "http://www.eng.it",
			MACAddress:       "321:654:987",
			DSDs:             "lkjhg",
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
*/
// func (d *Device) createDevice(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {

// 	if len(args) != 2 {
// 		return shim.Error("Incorrect number of arguments. Expecting 2")
// 	}

// 	var device = Device{SerialNumber: args[2]}

// 	deviceAsBytes, _ := json.Marshal(device)
// 	APIstub.PutState(args[0], deviceAsBytes)

// 	return shim.Success(nil)
// }

func (s *DSM) createDSM(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {

	if len(args) != 6 {
		return shim.Error("Incorrect number of arguments. Expecting 6")
	}

	var dsm = DSM{
		PhysicalArtifact:     args[1],
		URI:                  args[2],
		MACAddress:           args[3],
		DSD:                  args[4],
		ConnectionParameters: args[5],
	}

	dsmAsBytes, _ := json.Marshal(dsm)
	APIstub.PutState(args[0], dsmAsBytes)

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
		DSDs:             args[4]}

	dcmAsBytes, _ := json.Marshal(dcm)
	APIstub.PutState(args[0], dcmAsBytes)

	return shim.Success(nil)
}

/*
func (d *Device) queryAllDevices(APIstub shim.ChaincodeStubInterface) sc.Response {

	startKey := "DEV0"
	endKey := "DEV999"

	resultsIterator, err := APIstub.GetStateByRange(startKey, endKey)
	if err != nil {
		return shim.Error(err.Error())
	}
	defer resultsIterator.Close()

	// buffer is a JSON array containing QueryResults
	var buffer bytes.Buffer
	buffer.WriteString("[")

	bArrayMemberAlreadyWritten := false
	for resultsIterator.HasNext() {
		queryResponse, err := resultsIterator.Next()
		if err != nil {
			return shim.Error(err.Error())
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

	fmt.Printf("- queryAllDevices:\n%s\n", buffer.String())

	return shim.Success(buffer.Bytes())
}
*/

func (s *DSM) queryAllDSMs(APIstub shim.ChaincodeStubInterface) sc.Response {

	startKey := "DSM0"
	endKey := "DSM999"

	resultsIterator, err := APIstub.GetStateByRange(startKey, endKey)
	if err != nil {
		return shim.Error(err.Error())
	}
	defer resultsIterator.Close()

	// buffer is a JSON array containing QueryResults
	var buffer bytes.Buffer
	buffer.WriteString("[")

	bArrayMemberAlreadyWritten := false
	for resultsIterator.HasNext() {
		queryResponse, err := resultsIterator.Next()
		if err != nil {
			return shim.Error(err.Error())
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

	fmt.Printf("- queryAllDevices:\n%s\n", buffer.String())

	return shim.Success(buffer.Bytes())
}

func (c *DCM) queryAllDCMs(APIstub shim.ChaincodeStubInterface) sc.Response {

	startKey := "DCM0"
	endKey := "DCM999"

	resultsIterator, err := APIstub.GetStateByRange(startKey, endKey)
	if err != nil {
		return shim.Error(err.Error())
	}
	defer resultsIterator.Close()

	// buffer is a JSON array containing QueryResults
	var buffer bytes.Buffer
	buffer.WriteString("[")

	bArrayMemberAlreadyWritten := false
	for resultsIterator.HasNext() {
		queryResponse, err := resultsIterator.Next()
		if err != nil {
			return shim.Error(err.Error())
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

	fmt.Printf("- queryAllDevices:\n%s\n", buffer.String())

	return shim.Success(buffer.Bytes())
}

/*func (s *SmartContract) changeCarOwner(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {

	 if len(args) != 2 {
		 return shim.Error("Incorrect number of arguments. Expecting 2")
	 }

	 carAsBytes, _ := APIstub.GetState(args[0])
	 car := Car{}

	 json.Unmarshal(carAsBytes, &car)
	 car.Owner = args[1]

	 carAsBytes, _ = json.Marshal(car)
	 APIstub.PutState(args[0], carAsBytes)

	 return shim.Success(nil)
 }
*/
// The main function is only relevant in unit test mode. Only included here for completeness.
func main() {
	fmt.Printf("Main Function")
	// Create a new Smart Contract
	err := shim.Start(new(DSM))
	if err != nil {
		fmt.Printf("Error creating new DSM: %s", err)
	}
	err2 := shim.Start(new(DCM))
	if err2 != nil {
		fmt.Printf("Error creating new DCM: %s", err2)
	}

}
