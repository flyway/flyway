package com.googlecode.flyway.core.dbsupport.oracle;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test for OracleSqlStatementBuilder.
 */
public class OracleSqlStatementBuilderSmallTest {
    @Test
    public void changeDelimiterRegEx() {
        final OracleSqlStatementBuilder statementBuilder = new OracleSqlStatementBuilder();
        assertNull(statementBuilder.changeDelimiterIfNecessary("BEGIN_DATE", null));
        assertEquals("/", statementBuilder.changeDelimiterIfNecessary("BEGIN DATE", null).getDelimiter());
        assertEquals("/", statementBuilder.changeDelimiterIfNecessary("BEGIN", null).getDelimiter());
    }
}
