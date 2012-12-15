package com.googlecode.flyway.core.dbsupport;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test for SqlStatementBuilder.
 */
public class SqlStatementBuilderSmallTest {
    @Test
    public void stripDelimiter() {
        assertEquals("SELECT * FROM t WHERE a = 'Straßenpaß'",
                SqlStatementBuilder.stripDelimiter("SELECT * FROM t WHERE a = 'Straßenpaß';", new Delimiter(";", false)));
    }
}
