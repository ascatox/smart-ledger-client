package eu.faredge.smartledger.client.util;

import eu.faredge.smartledger.client.model.DCM;
import eu.faredge.smartledger.client.model.DSM;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.util.Set;

public class Validator {

    private javax.validation.Validator validator;
    public static final String MAC_ADDRESS_INVALID_MESSAGE = "MAC Address is invalid";
    public static final String URI_INVALID_MESSAGE = "Uri should be a valid URL";
    public static final String URI_CANNOT_BE_EMPTY_MESSAGE = "uri cannot be empty";
    public static final String PHYSICAL_ARTIFACT_CANNOT_BE_EMPTY_MESSAGE = "physicalArtifact cannot be empty";


    public Validator() {

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }


    public void validateBean(DSM dsm) {
        Set<ConstraintViolation<DSM>> violations = validator.validate(dsm);
        for (ConstraintViolation<DSM> violation : violations) {
            Util.fail(violation.getMessage());
        }

    }


    public void validateBean(DCM dcm) {
        Set<ConstraintViolation<DCM>> violations = validator.validate(dcm);
        for (ConstraintViolation<DCM> violation : violations) {
            Util.fail(violation.getMessage());
        }

    }


}
