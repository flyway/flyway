package com.googlecode.flyway.core.dbsupport.postgresql;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PostgreSQLDbSupportSmallTest {
    @Test
    public void doQuote() {
        PostgreSQLDbSupport dbSupport = new PostgreSQLDbSupport(null);
        assertEquals("\"abc\"", dbSupport.doQuote("abc"));
        assertEquals("\"a\"\"b\"\"c\"", dbSupport.doQuote("a\"b\"c"));
    }
}
