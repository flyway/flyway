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
package org.flywaydb.core.internal.dbsupport.db2;

import org.flywaydb.core.internal.dbsupport.Delimiter;
import org.flywaydb.core.internal.util.StringUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class DB2SqlStatementBuilderSmallTest {
    @Test
    public void isBegin() throws Exception {
        assertTrue(DB2SqlStatementBuilder.isBegin("BEGIN"));
        assertTrue(DB2SqlStatementBuilder.isBegin("LABEL:BEGIN"));
        assertTrue(DB2SqlStatementBuilder.isBegin("LABEL: BEGIN"));
        assertTrue(DB2SqlStatementBuilder.isBegin("LABEL :BEGIN"));
        assertTrue(DB2SqlStatementBuilder.isBegin("LABEL : BEGIN"));
    }

    @Test
    public void isTerminated() {
        DB2SqlStatementBuilder builder = new DB2SqlStatementBuilder();
        String sqlScriptSource = "CREATE OR REPLACE PROCEDURE IP_ROLLUP(\n" +
                "  IN iODLID INTEGER,\n" +
                "  IN iNDLID INTEGER,\n" +
                "  IN iOID INTEGER,\n" +
                "  IN iTYPE VARCHAR(5),\n" +
                "  IN iACTION VARCHAR(6)\n" +
                "  )\n" +
                "  LANGUAGE SQL\n" +
                "SPECIFIC SP_IP_ROLLUP\n" +
                "MAIN : BEGIN\n" +
                "\n" +
                "\n" +
                "END\n" +
                "@";

        builder.setDelimiter(new Delimiter("@", false));
        String[] lines = StringUtils.tokenizeToStringArray(sqlScriptSource, "\n");
        for (String line : lines) {
            builder.addLine(line);
        }

        assertTrue(builder.isTerminated());
    }
}