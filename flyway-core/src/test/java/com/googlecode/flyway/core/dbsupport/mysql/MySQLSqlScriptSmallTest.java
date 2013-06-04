/**
 * Copyright (C) 2010-2013 the original author or authors.
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

import com.googlecode.flyway.core.dbsupport.SqlScript;
import com.googlecode.flyway.core.dbsupport.SqlStatement;
import com.googlecode.flyway.core.dbsupport.oracle.OracleDbSupport;
import com.googlecode.flyway.core.util.ClassPathResource;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test for MySQL SqlScript.
 */
public class MySQLSqlScriptSmallTest {
    @Test
    public void multiLineCommentDirective() throws Exception {
        String source = "/*!50001 CREATE ALGORITHM=UNDEFINED */\n" +
                "/*!50013 DEFINER=`user`@`%` SQL SECURITY DEFINER */\n" +
                "/*!50001 VIEW `viewname` AS select `t`.`id` AS `someId`,`t`.`name` AS `someName` from `someTable` `t` where `t`.`state` = 0 */;\n";

        SqlScript sqlScript = new SqlScript(source, new MySQLDbSupport(null));
        List<SqlStatement> sqlStatements = sqlScript.getSqlStatements();
        assertEquals(1, sqlStatements.size());
        assertEquals(1, sqlStatements.get(0).getLineNumber());
    }
}
