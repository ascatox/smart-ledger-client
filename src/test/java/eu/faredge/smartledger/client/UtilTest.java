package eu.faredge.smartledger.client;

import eu.faredge.smartledger.client.util.Util;
import org.junit.Test;

import static junit.framework.TestCase.fail;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class UtilTest {


    @Test
    public void testValidateUriWhenUriIsNotValid() {
        String uri = "pippo";
        boolean b = Util.validateUri(uri);
        assertTrue(!b);
    }

    @Test
    public void testValidateUriWhenUriIsValid() {
        String uri = "http://www.google.it";
        boolean b = Util.validateUri(uri);
        assertTrue(b);

    }

    @Test
    public void testValidateUriWhenUriIsEmpty() {
        try {
            Util.validateUri("");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("empty uri"));
        }
    }

    @Test
    public void testValidateMacAddressWhenUriIsNotValid() {
        String macAddress = "ciaomare";
        boolean b = Util.validateMacAddress(macAddress);
        assertTrue(!b);

    }

    @Test
    public void testValidateMacAddressWhenUriIsValid() {
        String macAddress = "b8:e8:56:41:43:06";
        boolean b = Util.validateMacAddress(macAddress);
        assertTrue(b);
    }

    @Test
    public void testValidateMacAddressWhenUriIsEmpty() {
        try {
            Util.validateMacAddress("");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("empty macAddress"));
        }
    }


}
