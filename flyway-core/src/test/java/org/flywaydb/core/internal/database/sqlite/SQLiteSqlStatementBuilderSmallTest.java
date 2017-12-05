/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
 */
package org.flywaydb.core.internal.database.sqlite;

import org.flywaydb.core.internal.database.Delimiter;
import org.flywaydb.core.internal.util.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertTrue;


/**
 * Test for PostgreSQLSqlStatementBuilder.
 */
public class SQLiteSqlStatementBuilderSmallTest {
    /**
     * Class under test.
     */
    private SQLiteSqlStatementBuilder statementBuilder = new SQLiteSqlStatementBuilder(new Delimiter(";", false));

    @Test
    public void beginEnd() {
        String sqlScriptSource = "CREATE TABLE my_patch_level\n" +
                "  (\n" +
                "    db_id                     INTEGER PRIMARY KEY autoincrement,\n" +
                "    db_level                  NUMBER (3) NOT NULL ,\n" +
                "    db_svn                    VARCHAR2 (35) NOT NULL ,\n" +
                "    db_zp_beginn              DATE NOT NULL ,\n" +
                "    db_zp_ende                DATE\n" +
                "   ) ;";

        String[] lines = StringUtils.tokenizeToStringArray(sqlScriptSource, "\n");
        for (String line : lines) {
            statementBuilder.addLine(line);
        }

        assertTrue(statementBuilder.isTerminated());
    }

    @Test
    public void beginEndTrigger() {
        String sqlScriptSource = "CREATE TRIGGER cust_addr_chng\n" +
                "INSTEAD OF UPDATE OF cust_addr ON customer_address\n" +
                "BEGIN\n" +
                "  UPDATE customer SET cust_addr=NEW.cust_addr\n" +
                "   WHERE cust_id=NEW.cust_id;\n" +
                "END;";

        String[] lines = StringUtils.tokenizeToStringArray(sqlScriptSource, "\n");
        for (String line : lines) {
            statementBuilder.addLine(line);
        }

        assertTrue(statementBuilder.isTerminated());
    }

    @Test
    public void blobLiteral() throws Exception {
        statementBuilder.addLine("INSERT INTO test_table (id, bin) VALUES(1, x'01');");
        assertTrue(statementBuilder.isTerminated());
    }

}
