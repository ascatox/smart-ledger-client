package eu.faredge.smartledger.client;

import eu.faredge.smartledger.client.model.DSM;
import eu.faredge.smartledger.client.util.Validator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class ValidatorTest {

    Validator validator;

    @Before
    public void setup() {
        validator = new Validator();
    }

    @After
    public void tearDown() {
        validator = null;
    }

    @Test
    public void testValidateDSMWhenUriIsWrong() {
        DSM dsm = new DSM();
        dsm.setPhysicalArtifact("DEVICE00");
        dsm.setUri("ddddd");
        dsm.setMacAddress("b8:e8:56:41:43:06");
        try {
            validator.validateBean(dsm);
            assertTrue(true);
        } catch (Exception e) {
            assertThat(e.getMessage(), is(Validator.URI_INVALID_MESSAGE));
        }
    }

    @Test
    public void testValidateDSMWhenUriIsCorrect() {
        DSM dsm = new DSM();
        dsm.setPhysicalArtifact("DEVICE00");
        dsm.setUri("http://www.google.it");
        dsm.setMacAddress("b8:e8:56:41:43:06");
        try {
            validator.validateBean(dsm);
            assertTrue(true);
        } catch (Exception e) {
            assertFalse(e.getMessage(),true);
        }
    }

    @Test
    public void testValidateDSMWhenMacAddressIsWrong() {
        DSM dsm = new DSM();
        dsm.setPhysicalArtifact("DEVICE01");
        dsm.setUri("https://www.antonioscatoloni.it");
        dsm.setMacAddress("ciaomare");
        try {
            validator.validateBean(dsm);
            assertFalse(true);
        } catch (Exception e) {
            assertThat(e.getMessage(), is(Validator.MAC_ADDRESS_INVALID_MESSAGE));
        }
    }

    @Test
    public void testValidateDSMWhenMacAddressIsCorrect() {
        DSM dsm = new DSM();
        dsm.setPhysicalArtifact("DEVICE01");
        dsm.setUri("https://www.antonioscatoloni.it");
        dsm.setMacAddress("b8:e8:56:41:43:06");
        try {
            validator.validateBean(dsm);
            assertTrue(true);
        } catch (Exception e) {
            assertFalse(e.getMessage(),true);
        }

    }


}
