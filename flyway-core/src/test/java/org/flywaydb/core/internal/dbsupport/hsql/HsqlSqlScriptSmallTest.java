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
package org.flywaydb.core.internal.dbsupport.hsql;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Small test for HsqlSqlScript.
 */
public class HsqlSqlScriptSmallTest {
    @Test
    public void parseBeginAtomic() {
        HsqlSqlStatementBuilder statementBuilder = new HsqlSqlStatementBuilder();
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
