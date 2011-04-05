package com.googlecode.flyway.core.dbsupport.oracle;

import com.googlecode.flyway.core.exception.FlywayException;
import org.junit.Test;

/**
 * Small Test for OracleDbSupport.
 */
public class OracleDbSupportSmallTest {
    /**
     * Checks that cleaning can not be performed for the SYSTEM schema (Issue 102)
     */
    @Test(expected = FlywayException.class)
    public void createCleanScriptWithSystem() {
        OracleDbSupport oracleDbSupport = new OracleDbSupport(null);
        oracleDbSupport.createCleanScript("SYSTEM");
    }
}
