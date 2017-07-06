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
    public void extractLabel() throws Exception {
        assertNull(DB2SqlStatementBuilder.extractLabel("BEGIN"));
        assertEquals("LABEL", DB2SqlStatementBuilder.extractLabel("LABEL:BEGIN"));
        assertEquals("LABEL", DB2SqlStatementBuilder.extractLabel("LABEL: BEGIN"));
        assertEquals("LABEL", DB2SqlStatementBuilder.extractLabel("LABEL :BEGIN"));
        assertEquals("LABEL", DB2SqlStatementBuilder.extractLabel("LABEL : BEGIN"));
    }

    @Test
    public void isTerminated() {
        assertTerminated("CREATE OR REPLACE PROCEDURE IP_ROLLUP(\n" +
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
                "@", "@");
    }

    @Test
    public void isTerminatedLabel() {
        assertTerminated("CREATE OR REPLACE PROCEDURE CUST_INSERT_IP_GL(\n" +
                "  IN iORDER_INTERLINER_ID INTEGER,\n" +
                "  IN iACCT_CODE VARCHAR(50),\n" +
                "  IN iACCT_TYPE CHAR(3),\n" +
                "  IN iAMOUNT DOUBLE,\n" +
                "  IN iSOURCE_TYPE VARCHAR(20),\n" +
                "  OUT oLEAVE_MAIN VARCHAR(5)\n" +
                "  )\n" +
                "  LANGUAGE SQL\n" +
                "SPECIFIC SP_CUST_INSERT_IP_GL\n" +
                "MAIN : BEGIN\n" +
                "  SET oLEAVE_MAIN = 'False';\n" +
                "END MAIN\n" +
                "@", "@");
    }

    @Test
    public void isTerminatedNested() {
        assertTerminated("CREATE PROCEDURE dummy_proc ()\n" +
                "LANGUAGE SQL\n" +
                "BEGIN\n" +
                "    declare var1 TIMESTAMP;\n" +
                "    \n" +
                "    SELECT CURRENT_TIMESTAMP INTO var1 FROM SYSIBM.DUAL;\n" +
                "    \n" +
                "    BEGIN\n" +
                "        declare var2 DATE;\n" +
                "        SELECT CURRENT_DATE INTO var2 FROM SYSIBM.DUAL;\n" +
                "    END;\n" +
                "END;", ";");
    }

    private void assertTerminated(String sqlScriptSource, String delimiter) {
        DB2SqlStatementBuilder builder = new DB2SqlStatementBuilder();
        builder.setDelimiter(new Delimiter(delimiter, false));
        String[] lines = StringUtils.tokenizeToStringArray(sqlScriptSource, "\n");
        for (String line : lines) {
            assertFalse(builder.isTerminated());
            builder.addLine(line);
        }

        assertTrue(builder.isTerminated());
    }
}