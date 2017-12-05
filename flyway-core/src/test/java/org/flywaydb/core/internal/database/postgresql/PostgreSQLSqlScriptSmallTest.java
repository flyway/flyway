/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.database.postgresql;

import org.flywaydb.core.internal.database.Delimiter;
import org.flywaydb.core.internal.sqlscript.SqlScript;
import org.flywaydb.core.internal.sqlscript.SqlStatement;
import org.flywaydb.core.internal.database.SqlStatementBuilder;
import org.flywaydb.core.internal.util.scanner.classpath.ClassPathResource;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test for SqlScript for PostgreSQL.
 */
public class PostgreSQLSqlScriptSmallTest {
    private SqlScript createSqlScript(String source) {
        return new SqlScript(source, null) {
            @Override
            protected SqlStatementBuilder createSqlStatementBuilder() {
                return new PostgreSQLSqlStatementBuilder(Delimiter.SEMICOLON);
            }
        };
    }

    @Test
    public void parseSqlStatementsDo() throws Exception {
        String source = new ClassPathResource(
                "migration/database/postgresql/sql/dollar/V2__Even_more_dollars.sql", Thread.currentThread().getContextClassLoader()).loadAsString("UTF-8");

        SqlScript sqlScript = createSqlScript(source);
        List<SqlStatement> sqlStatements = sqlScript.getSqlStatements();
        assertEquals(3, sqlStatements.size());
        assertEquals(17, sqlStatements.get(0).getLineNumber());
        assertEquals(23, sqlStatements.get(1).getLineNumber());
        assertEquals(28, sqlStatements.get(2).getLineNumber());
    }
}
