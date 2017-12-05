/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.database.mysql;

import org.flywaydb.core.internal.database.Delimiter;
import org.flywaydb.core.internal.sqlscript.SqlScript;
import org.flywaydb.core.internal.sqlscript.SqlStatement;
import org.flywaydb.core.internal.database.SqlStatementBuilder;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test for MySQL SqlScript.
 */
public class MySQLSqlScriptSmallTest {
    private SqlScript createSqlScript(String source) {
        return new SqlScript(source, null) {
            @Override
            protected SqlStatementBuilder createSqlStatementBuilder() {
                return new MySQLSqlStatementBuilder(Delimiter.SEMICOLON);
            }
        };
    }

    @Test
    public void multiLineCommentDirective() throws Exception {
        String source = "/*!50001 CREATE ALGORITHM=UNDEFINED */\n" +
                "/*!50013 DEFINER=`user`@`%` SQL SECURITY DEFINER */\n" +
                "/*!50001 VIEW `viewname` AS select `t`.`id` AS `someId`,`t`.`name` AS `someName` from `someTable` `t` where `t`.`state` = 0 */;\n";

        SqlScript sqlScript = createSqlScript(source);
        List<SqlStatement> sqlStatements = sqlScript.getSqlStatements();
        assertEquals(1, sqlStatements.size());
        assertEquals(1, sqlStatements.get(0).getLineNumber());
    }

    @Test
    public void unquotedMultiLineCommentDirective() throws Exception {
        String source = "INSERT INTO tablename VALUES ('a','b');\n" +
                "/*!50001 CREATE TABLE `viewname` (\n" +
                "`id` int(10) unsigned,\n" +
                "`name` varchar(10)\n" +
                ") ENGINE=MyISAM */;\n" +
                "INSERT INTO tablename VALUES ('a','b');";
        SqlScript sqlScript = createSqlScript(source);
        List<SqlStatement> sqlStatements = sqlScript.getSqlStatements();
        assertEquals(3, sqlStatements.size());
        assertEquals(1, sqlStatements.get(0).getLineNumber());
        assertEquals(2, sqlStatements.get(1).getLineNumber());
        assertEquals(6, sqlStatements.get(2).getLineNumber());
    }
}
