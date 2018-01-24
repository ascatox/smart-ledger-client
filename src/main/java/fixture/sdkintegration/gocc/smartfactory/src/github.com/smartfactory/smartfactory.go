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
	"strings"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	sc "github.com/hyperledger/fabric/protos/peer"
)

var logger = shim.NewLogger("smartfactory")

type Device struct{}

type DSM struct {
	Id            string `json:"id"`
	MACAddress    string `json:"macAddress"`
	DSD           string `json:"dsd"`
	DSMParameters string `json:"dsmParameters"`
	Type          string `json:"type"`
}

type DCM struct {
	Id         string `json:"id"`
	MACAddress string `json:"macAddress"`
	DSDs       string `json:"dsds"`
	Type       string `json:"type"`
}

type DCD struct {
	ExpirationDateTime string `json:"expirationDateTime"`
	ValidFrom          string `json:"validFrom"`
	DSMId              string `json:"dsmId"`
	DCMId              string `json:"dcmId"`
	Id                 string `json:"id"`
	Type               string `json:"type"`
}

/*
 * The Init method is called when the Smart Contract "fabcar" is instantiated by the blockchain network
 * Best practice is to have any Ledger initialization in separate function -- see initLedger()
 */

func (d *Device) Init(APIstub shim.ChaincodeStubInterface) sc.Response {
	DSMs := []DSM{
		DSM{
			Id:            "device://axyzk-dsm",
			MACAddress:    "123:456:789",
			DSD:           "Lorem",
			DSMParameters: "{'key':'value'}",
			Type:          "DSM",
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
	DCMs := []DCM{
		DCM{
			Id:         "device://byutj-dcm",
			MACAddress: "321:654:987",
			DSDs:       "Ipsum",
			Type:       "DCM",
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
	DCDs := []DCD{
		DCD{
			ExpirationDateTime: "2018-01-27",
			ValidFrom:          "2017-01-27",
			DSMId:              "device://axyzk-dsm",
			DCMId:              "device://byutj-dcm",
			Id:                 "channel://tytlt-dcd",
			Type:               "DCD",
		},
	}
	l := 0
	for l < len(DCDs) {
		fmt.Println("l is ", l)
		DCDAsBytes, _ := json.Marshal(DCDs[l])
		APIstub.PutState("DCD"+strconv.Itoa(l), DCDAsBytes)
		fmt.Println("Added", DCDs[l])
		l = l + 1
	}

	return shim.Success(nil)
}

func (d *Device) queryDSM(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {

	if len(args) != 1 {
		return shim.Error("Incorrect number of arguments. Expecting 1")
	}

	dsmAsBytes, _ := APIstub.GetState(args[0])
	return shim.Success(dsmAsBytes)
}

func (d *Device) queryDCM(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {

	if len(args) != 1 {
		return shim.Error("Incorrect number of arguments. Expecting 1")
	}
	dcmAsBytes, _ := APIstub.GetState(args[0])
	return shim.Success(dcmAsBytes)
}

func (d *Device) queryDCD(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {

	if len(args) != 1 {
		return shim.Error("Incorrect number of arguments. Expecting 1")
	}
	dcdAsBytes, _ := APIstub.GetState(args[0])
	return shim.Success(dcdAsBytes)
}

func (d *Device) editDSM(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {
	fmt.Println("Entering inside editDSM")

	if len(args) != 4 {
		lenght := strconv.Itoa(len(args))
		str := "Incorrect number of arguments. Expecting 4!" + "received: " + lenght
		return shim.Error(str)
	}

	dsm := DSM{
		Id:            args[0],
		MACAddress:    args[1],
		DSD:           args[2],
		DSMParameters: args[3],
		Type:          "DSM",
	}
	// Convert keys to compound ke

	dsmAsBytes, _ := json.Marshal(dsm)
	fmt.Println("editDSM PutState", dsmAsBytes)
	// Id Primary Key
	APIstub.PutState(args[0], dsmAsBytes)

	return shim.Success(nil)
}

func (d *Device) editDCM(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {

	if len(args) != 3 {
		return shim.Error("Incorrect number of arguments. Expecting 3")
	}

	dcm := DCM{
		Id:         args[0],
		MACAddress: args[1],
		DSDs:       args[2],
		Type:       "DCM",
	}

	dcmAsBytes, _ := json.Marshal(dcm)
	// Id Primary Key
	APIstub.PutState(args[0], dcmAsBytes)

	return shim.Success(nil)
}

func (d *Device) editDCD(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {

	if len(args) != 5 {
		return shim.Error("Incorrect number of arguments. Expecting 5")
	}

	dcd := DCD{
		ExpirationDateTime: args[0],
		ValidFrom:          args[1],
		DSMId:              args[2],
		DCMId:              args[3],
		Id:                 args[4],
		Type:               "DCD",
	}

	dcdAsBytes, _ := json.Marshal(dcd)
	// Id Primary Key
	APIstub.PutState(args[5], dcdAsBytes)

	return shim.Success(nil)
}

func (d *Device) queryAllDSMs(APIstub shim.ChaincodeStubInterface) sc.Response {
	fmt.Println("Entering inside queryAllDSMs")
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

func (d *Device) queryAllDSMsByDSDs(APIstub shim.ChaincodeStubInterface, dsds []string) sc.Response {
	fmt.Println("Entering inside queryAllDSMsBySDSs: ", len(dsds))
	logger.Info("Entering in queryAllDSMsBySDSs")
	if (len(dsds)) == 0 {
		return shim.Error("Incorrect number of arguments. Expecting dsds (array)")
	}
	isNotFirstElement := false
	var buffer bytes.Buffer
	for i := 0; i < len(dsds); i++ {

		queryString :=
			"{" +
				"\"selector\": {" +
				"	\"type\": \"DSM\"" +
				" ," +
				" \"dsd\": " +
				" \"" + dsds[i] +
				"\"} " +
				" }"

		queryResponse, error := getQueryResultForQueryString(APIstub, queryString)
		if error != nil {
			fmt.Printf("Error querying DSM by DSD: %s", error)
			return shim.Error("Error querying DSM by dsd " + dsds[i]) //TODO Error
		}

		if isNotFirstElement {
			buffer.WriteString(",")
		}
		n := bytes.IndexByte(queryResponse, 0)
		buffer.WriteString(string(queryResponse[:n]))
		isNotFirstElement = true
	}
	return shim.Success(buffer.Bytes())
	/* return buffer.Bytes(), nil */
}

func (d *Device) querDSMById(APIstub shim.ChaincodeStubInterface, id string) sc.Response {
	fmt.Println("Entering inside querDSMById")
	//	logger.Info("Entering in queryDSMById")
	if (len(strings.TrimSpace(id))) == 0 {
		return shim.Error("Incorrect number of arguments. Expecting Id")
	}
	queryString :=
		"{" +
			"\"selector\": {" +
			"	\"type\": \"DSM\"" +
			" ," +
			" \"id\": " +
			" \"" + id +
			"\"} " +
			" }"
	buffer, error := getQueryResultForQueryString(APIstub, queryString)
	if error != nil {
		fmt.Printf("Error querying DSM by Id: %s", error)
		return shim.Error("Error querying DSM by Id " + id) //TODO Error
	}
	return shim.Success(buffer)
}

func (d *Device) querDSMByMacAdd(APIstub shim.ChaincodeStubInterface, macAdd string) sc.Response {
	fmt.Println("Entering inside querDSMByMacAdd")
	//	logger.Info("Entering in queryDSMByMacAdd")
	if (len(strings.TrimSpace(macAdd))) == 0 {
		return shim.Error("Incorrect number of arguments. Expecting macAdd")
	}
	queryString :=
		"{" +
			"\"selector\": {" +
			"	\"type\": \"DSM\"" +
			" ," +
			" \"macAddress\": " +
			" \"" + macAdd +
			"\"} " +
			" }"
	buffer, error := getQueryResultForQueryString(APIstub, queryString)
	if error != nil {
		fmt.Printf("Error querying DSM by MACAddress: %s", error)
		return shim.Error("Error querying DSM by MACAddress " + macAdd) //TODO Error
	}
	return shim.Success(buffer)
}

func (d *Device) queryAllDCMs(APIstub shim.ChaincodeStubInterface) sc.Response {
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

func (d *Device) querDCMById(APIstub shim.ChaincodeStubInterface, id string) sc.Response {
	//	logger.Info("Entering in queryDCMById")
	if (len(strings.TrimSpace(id))) == 0 {
		return shim.Error("Incorrect number of arguments. Expecting Id")
	}
	queryString :=
		"{" +
			"\"selector\": {" +
			"	\"type\": \"DCM\"" +
			" ," +
			" \"id\": " +
			" \"" + id +
			"\"} " +
			" }"
	buffer, error := getQueryResultForQueryString(APIstub, queryString)
	if error != nil {
		fmt.Printf("Error querying DCM by Id: %c", error)
		return shim.Error("Error querying DCM by Id " + id) //TODO Error
	}
	return shim.Success(buffer)
}

func (d *Device) querDCMByMacAdd(APIstub shim.ChaincodeStubInterface, macAdd string) sc.Response {
	//	logger.Info("Entering in queryDCMByMacAdd")
	if (len(strings.TrimSpace(macAdd))) == 0 {
		return shim.Error("Incorrect number of arguments. Expecting MACAddress")
	}
	queryString :=
		"{" +
			"\"selector\": {" +
			"	\"type\": \"DCM\"" +
			" ," +
			" \"macAddress\": " +
			" \"" + macAdd +
			"\"} " +
			" }"
	buffer, error := getQueryResultForQueryString(APIstub, queryString)
	if error != nil {
		fmt.Printf("Error querying DCM by MACAddress: %c", error)
		return shim.Error("Error querying DCM by MACAddress " + macAdd) //TODO Error
	}
	return shim.Success(buffer)
}

func (d *Device) queryAllDCDs(APIstub shim.ChaincodeStubInterface) sc.Response {
	queryString :=
		"{" +
			"\"selector\": {" +
			"	\"type\": \"DCD\"" +
			" }" +
			" }"
	buffer, error := getQueryResultForQueryString(APIstub, queryString)
	if error != nil {
		fmt.Printf("Error querying all DCD: %s", error)
		return shim.Error("Error querying all DCD")
	}
	return shim.Success(buffer)
}

func (d *Device) querDCDById(APIstub shim.ChaincodeStubInterface, id string) sc.Response {
	//	logger.Info("Entering in queryDCDById")
	if (len(strings.TrimSpace(id))) == 0 {
		return shim.Error("Incorrect number of arguments. Expecting Id")
	}
	queryString :=
		"{" +
			"\"selector\": {" +
			"	\"type\": \"DCD\"" +
			" ," +
			" \"id\": " +
			" \"" + id +
			"\"} " +
			" }"
	buffer, error := getQueryResultForQueryString(APIstub, queryString)
	if error != nil {
		fmt.Printf("Error querying DCD by Id: %c", error)
		return shim.Error("Error querying DCD by Id " + id) //TODO Error
	}
	return shim.Success(buffer)
}

func (d *Device) removeDSM(stub shim.ChaincodeStubInterface, key string) sc.Response {
	if (len(strings.TrimSpace(key))) == 0 {
		return shim.Error("Incorrect number of arguments. Expecting DSM key")
	}
	dsmAsBytes, _ := stub.GetState(key)
	if dsmAsBytes == nil {
		return shim.Error("Error DSM object with key: " + key + " not found")
	}

	var dsm DSM
	err := json.Unmarshal(dsmAsBytes, &dsm)
	if err != nil {
		return shim.Error("Error removing DSM with key: " + key)
	}
	if dsm.Type != "DSM" {
		return shim.Error("Error removing DSM type of the object for the key: " + key + " is incorrect (type: " + dsm.Type + ")")
	}
	fmt.Printf("Trying to delete DSM Object with key: " + key)
	error := stub.DelState(key)
	if error != nil {
		return shim.Error("Error removing DSM with key: " + key)
	}
	return shim.Success(nil)
}

func (d *Device) removeDCM(stub shim.ChaincodeStubInterface, key string) sc.Response {
	if (len(strings.TrimSpace(key))) == 0 {
		return shim.Error("Incorrect number of arguments. Expecting DCM key")
	}
	dcmAsBytes, _ := stub.GetState(key)
	if dcmAsBytes == nil {
		return shim.Error("Error DCM object with key: " + key + " not found")
	}

	var dcm DCM
	err := json.Unmarshal(dcmAsBytes, &dcm)
	if err != nil {
		return shim.Error("Error removing DCM with key: " + key)
	}
	if dcm.Type != "DCM" {
		return shim.Error("Error removing DCM type of the object for the key: " + key + " is incorrect (type: " + dcm.Type + ")")
	}
	fmt.Printf("Trying to delete DCM Object with key: " + key)
	error := stub.DelState(key)
	if error != nil {
		return shim.Error("Error removing DCM with key: " + key)
	}
	return shim.Success(nil)
}

func (d *Device) removeDCD(stub shim.ChaincodeStubInterface, key string) sc.Response {
	if (len(strings.TrimSpace(key))) == 0 {
		return shim.Error("Incorrect number of arguments. Expecting DCD key")
	}
	dcdAsBytes, _ := stub.GetState(key)
	if dcdAsBytes == nil {
		return shim.Error("Error DCD object with key: " + key + " not found")
	}

	var dcd DCD
	err := json.Unmarshal(dcdAsBytes, &dcd)
	if err != nil {
		return shim.Error("Error removing DCD with key: " + key)
	}
	if dcd.Type != "DCD" {
		return shim.Error("Error removing DCD type of the object for the key: " + key + " is incorrect (type: " + dcd.Type + ")")
	}
	fmt.Printf("Trying to delete DCD Object with key: " + key)
	error := stub.DelState(key)
	if error != nil {
		return shim.Error("Error removing DCD with key: " + key)
	}
	return shim.Success(nil)
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

func (d *Device) Invoke(stub shim.ChaincodeStubInterface) sc.Response {
	function, args := stub.GetFunctionAndParameters()
	fmt.Printf("CALL function: " + function)

	switch function {
	// DSM Type
	// Query DSM
	case "qGetAllDSMs":
		return d.queryAllDSMs(stub)
	case "qGetAllDSMsByDSDs":
		return d.queryAllDSMsByDSDs(stub, args)
	case "qGetDSMById":
		return d.querDSMById(stub, args[0])
	case "qGetDSMByMacAdd":
		return d.querDSMByMacAdd(stub, args[0])
	// Edit and Remove DSM
	case "iEditDSM":
		return d.editDSM(stub, args)
	case "iRemoveDSM":
		return d.removeDSM(stub, args[0])
	// DCM Type
	// Query DCM
	case "qGetAllDCMs":
		return d.queryAllDCMs(stub)
	case "qGetDCMById":
		return d.querDCMById(stub, args[0])
	case "qGetDCMByMacAdd":
		return d.querDCMByMacAdd(stub, args[0])
	// Edit and Remove DCM
	case "iEditDCM":
		return d.editDCM(stub, args)
	case "iRemoveDCM":
		return d.removeDCM(stub, args[0])
	// DCD Type
	// Query DCM
	case "qGetAllDCDs":
		return d.queryAllDCDs(stub)
	case "qGetDCDById":
		return d.querDCDById(stub, args[0])
	// Edit and Remove DCD
	case "iEditDCD":
		return d.editDCD(stub, args)
	case "iRemoveDCD":
		return d.removeDCD(stub, args[0])
	}
	return shim.Success(nil)
}

func main() {
	fmt.Printf("Main Function")
	logger.SetLevel(shim.LogInfo)
	logLevel, _ := shim.LogLevel(os.Getenv("SHIM_LOGGING_LEVEL"))
	shim.SetLoggingLevel(logLevel)
	// Edit a new Smart Contract
	err := shim.Start(new(Device))
	fmt.Printf("Edit a new generic Device Type")
	if err != nil {
		fmt.Printf("Error creating new generic Device: %s", err)
	}
}
