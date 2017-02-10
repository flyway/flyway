/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.core.internal.dbsupport.sqlite;

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
    private SQLiteSqlStatementBuilder statementBuilder = new SQLiteSqlStatementBuilder();

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
