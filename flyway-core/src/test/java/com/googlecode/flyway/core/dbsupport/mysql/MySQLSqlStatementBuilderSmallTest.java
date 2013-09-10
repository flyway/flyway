/**
 * Copyright 2010-2013 Axel Fontaine and the many contributors.
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
package com.googlecode.flyway.core.dbsupport.mysql;

import com.googlecode.flyway.core.dbsupport.Delimiter;
import com.googlecode.flyway.core.dbsupport.SqlScript;
import com.googlecode.flyway.core.dbsupport.SqlStatement;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test for MySQLSqlStatementBuilder.
 */
public class MySQLSqlStatementBuilderSmallTest {
    private MySQLSqlStatementBuilder builder = new MySQLSqlStatementBuilder();

    @Test
    public void isCommentDirective() {
        assertFalse(builder.isCommentDirective("SELECT * FROM TABLE;"));
        assertFalse(builder.isCommentDirective("/*SELECT * FROM TABLE*/;"));
        assertTrue(builder.isCommentDirective("/*!12345 SELECT * FROM TABLE*/"));
        assertTrue(builder.isCommentDirective("/*!12345 SELECT * FROM TABLE*/;"));
    }

    @Test
    public void escapedSingleQuotes() {
        builder.setDelimiter(new Delimiter("$$", false));

        builder.addLine("CREATE PROCEDURE test_proc(testDate CHAR(10))");
        builder.addLine("BEGIN");
        builder.addLine("    SET @testSQL = CONCAT(");
        builder.addLine("        'INSERT INTO test_table (test_id, test_date) ',");
        builder.addLine("        ' VALUE (1, DATE(\\'', testDate, '\\'));');");
        builder.addLine("    PREPARE testStmt FROM @testSQL;");
        builder.addLine("    EXECUTE testStmt;");
        builder.addLine("    DEALLOCATE PREPARE testStmt;");
        builder.addLine("END $$");

        assertTrue(builder.isTerminated());
    }

    @Test
    public void stringEndingInX() throws Exception {
        builder.addLine("INSERT INTO Tablename (id) VALUES (' x');");

        assertTrue(builder.isTerminated());
    }

    @Test
    public void stringEndingInB() throws Exception {
        builder.addLine("INSERT INTO Tablename (id) VALUES (' b');");

        assertTrue(builder.isTerminated());
    }

    @Test
    public void hexLiteral() throws Exception {
        builder.addLine("INSERT INTO Tablename (id) VALUES (x'5B1A5933964C4AA59F3013D3B3F3414D');");

        assertTrue(builder.isTerminated());
    }

    @Test
    public void binaryLiteral() throws Exception {
        builder.addLine("INSERT INTO Tablename (id) VALUES (b'10110011');");

        assertTrue(builder.isTerminated());
    }
}
