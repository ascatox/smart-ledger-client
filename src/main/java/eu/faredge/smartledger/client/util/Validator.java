
package eu.faredge.smartledger.client.util;

import eu.faredge.dm.dcm.DCM;
import eu.faredge.dm.dsm.DSM;
import eu.faredge.smartledger.client.SmartLedgerClient;
import eu.faredge.smartledger.client.exception.SmartLedgerClientException;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.util.Set;

public class Validator {

    private javax.validation.Validator validator;
    public static final String MAC_ADDRESS_INVALID_MESSAGE = "MAC Address is invalid";
    public static final String URI_INVALID_MESSAGE = "Uri should be a valid URL";
    public static final String ID_CANNOT_BE_EMPTY_MESSAGE = "Model and id cannot be empty";
    public static final String PHYSICAL_ARTIFACT_CANNOT_BE_EMPTY_MESSAGE = "physicalArtifact cannot be empty";


    public Validator() {

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }


    public void validateBean(DSM dsm) throws SmartLedgerClientException {
        if (null == dsm || StringUtils.isEmpty(dsm.getId()))
            throw new SmartLedgerClientException(ID_CANNOT_BE_EMPTY_MESSAGE);
    }


    public void validateBean(DCM dcm) throws SmartLedgerClientException {
        if (null == dcm || StringUtils.isEmpty(dcm.getId()))
            throw new SmartLedgerClientException(ID_CANNOT_BE_EMPTY_MESSAGE);
    }


}

