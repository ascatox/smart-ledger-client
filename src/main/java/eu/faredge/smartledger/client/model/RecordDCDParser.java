
package eu.faredge.smartledger.client.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import eu.faredge.dm.dcd.DCD;

public class RecordDCDParser {


    public static DCD parse(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonObject jobj = jsonObject.get("Record").getAsJsonObject();
        DCD dcd = new DCD();
        String id = jobj.get("id").getAsString();
        String expirationDateTime = jobj.get("expirationDateTime").getAsString();
        String validFrom = jobj.get("validFrom").getAsString();
        String dsmId = jobj.get("dsmId").getAsString();
        String dcmId =  jobj.get("dcmId").getAsString();
        //TODO Date Conversion
        return dcd;
    }


}

