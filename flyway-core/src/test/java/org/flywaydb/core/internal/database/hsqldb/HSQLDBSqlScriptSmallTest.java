/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.hsqldb;

import org.flywaydb.core.internal.database.Delimiter;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Small test for HsqlSqlScript.
 */
public class HSQLDBSqlScriptSmallTest {
    @Test
    public void parseBeginAtomic() {
        HSQLDBSqlStatementBuilder statementBuilder = new HSQLDBSqlStatementBuilder(new Delimiter(";", false));
        String sqlScriptSource = "CREATE TRIGGER uniqueidx_trigger BEFORE INSERT ON usertable \n" +
                "\tREFERENCING NEW ROW AS newrow\n" +
                "    FOR EACH ROW WHEN (newrow.name is not null)\n" +
                "\tBEGIN ATOMIC\n" +
                "      IF EXISTS (SELECT * FROM usertable WHERE usertable.name = newrow.name) THEN\n" +
                "        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'duplicate name';\n" +
                "      END IF;\n" +
                "    END";

        String[] lines = (sqlScriptSource + ";   ").split("[\n]");
        for (String line : lines) {
            statementBuilder.addLine(line);
        }

        assertEquals(sqlScriptSource, statementBuilder.getSqlStatement().getSql());
    }
}
