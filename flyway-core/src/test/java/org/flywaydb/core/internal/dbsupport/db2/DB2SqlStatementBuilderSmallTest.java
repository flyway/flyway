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
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.*;

public class DB2SqlStatementBuilderSmallTest {
    @Test
    public void isBegin() throws Exception {
        assertTrue(DB2SqlStatementBuilder.isBegin("BEGIN"));
        assertTrue(DB2SqlStatementBuilder.isBegin("LABEL:BEGIN"));
        assertTrue(DB2SqlStatementBuilder.isBegin("LABEL: BEGIN"));
        assertTrue(DB2SqlStatementBuilder.isBegin("LABEL :BEGIN"));
        assertTrue(DB2SqlStatementBuilder.isBegin("LABEL : BEGIN"));
        assertTrue(DB2SqlStatementBuilder.isBegin("REFERENCING NEW AS NEW FOR EACH ROW BEGIN ATOMIC"));
    }

    @Test
    public void isEnd() throws Exception {
        assertTrue(DB2SqlStatementBuilder.isEnd("END", null, new Delimiter(";", false), 1));
        assertTrue(DB2SqlStatementBuilder.isEnd("END;", null, new Delimiter(";", false), 1));
        assertTrue(DB2SqlStatementBuilder.isEnd("END", "LABEL", new Delimiter(";", false), 1));
        assertTrue(DB2SqlStatementBuilder.isEnd("END@", "LABEL", new Delimiter("@", false), 1));
        assertTrue(DB2SqlStatementBuilder.isEnd("END;", "LABEL", new Delimiter("@", false), 2));
        assertTrue(DB2SqlStatementBuilder.isEnd("END LABEL", "LABEL", new Delimiter(";", false), 1));
        assertFalse(DB2SqlStatementBuilder.isEnd("END FOR", null, new Delimiter(";", false), 1));
        assertFalse(DB2SqlStatementBuilder.isEnd("END FOR", "LABEL", new Delimiter(";", false), 1));
        assertFalse(DB2SqlStatementBuilder.isEnd("END IF", null, new Delimiter(";", false), 1));
        assertFalse(DB2SqlStatementBuilder.isEnd("END IF", "LABEL", new Delimiter(";", false), 1));
        assertFalse(DB2SqlStatementBuilder.isEnd("SELECT XXX INTO YYY_END", "LABEL", new Delimiter(";", false), 1));
        assertFalse(DB2SqlStatementBuilder.isEnd("IF (COALESCE(XXX.END_YYY,'') <> '') THEN", "LABEL", new Delimiter(";", false), 1));
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
    public void isTerminated() throws IOException {
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
                "  -- COMMENT WITH END\n" +
                "END\n" +
                "@", "@");
    }

    @Test
    public void isTerminatedLabel() throws IOException {
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
    public void isTerminatedNested() throws IOException {
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

    @Test
    public void isTerminatedNestedDelimiter() throws IOException {
        assertTerminated("CREATE OR REPLACE PROCEDURE NESTED () LANGUAGE SQL\n" +
                "MAIN: BEGIN\n" +
                "  BEGIN\n" +
                "    -- do something\n" +
                "  END;\n" +
                "END MAIN\n" +
                "@", "@");
    }

    @Test
    public void isTerminatedNestedLabel() throws IOException {
        assertTerminated("CREATE OR REPLACE PROCEDURE TEST2\n" +
                "BEGIN\n" +
                " MAIN: BEGIN\n" +
                "  MAIN1: BEGIN\n" +
                "  END MAIN1;\n" +
                " END MAIN;\n" +
                " \n" +
                " TEST: BEGIN\n" +
                "  TEST1: BEGIN\n" +
                "  END TEST1;\n" +
                " END TEST;\n" +
                "END\n" +
                "@", "@");
    }

    @Test
    public void isTerminatedForIf() throws IOException {
        assertTerminated("CREATE OR REPLACE PROCEDURE FORIF(\n" +
                "  IN my_arg INTEGER\n" +
                "  )\n" +
                "  LANGUAGE SQL\n" +
                "SPECIFIC xxx\n" +
                "MAIN:BEGIN\n" +
                "  FOR C1 AS\n" +
                "    SELECT aaa\n" +
                "    FROM ttt\n" +
                "    WHERE ccc = 'vvv'DO\n" +
                "    -- Do something\n" +
                "    IF my_condition THEN\n" +
                "      -- Do something\n" +
                "    ELSE\n" +
                "      -- Do something\n" +
                "    END IF;\n" +
                "  END FOR;\n" +
                "END@", "@");
    }

    @Test
    public void isTerminatedTrigger1() throws IOException {
        assertTerminated("CREATE OR REPLACE TRIGGER I_TRIGGER_I\n" +
                "BEFORE INSERT ON I_TRIGGER\n" +
                "REFERENCING NEW AS NEW FOR EACH ROW\n" +
                "BEGIN ATOMIC\n" +
                "IF NEW.ID IS NULL\n" +
                "THEN SET NEW.ID = NEXTVAL FOR SEQ_I_TRIGGER;\n" +
                "END IF;\n" +
                "END\n" +
                "/", "/");
    }

    @Test
    public void isTerminatedTrigger2() throws IOException {
        assertTerminated("CREATE OR REPLACE TRIGGER I_TRIGGER_I\n" +
                "BEFORE INSERT ON I_TRIGGER\n" +
                "REFERENCING NEW AS NEW FOR EACH ROW BEGIN ATOMIC\n" +
                "IF NEW.ID IS NULL\n" +
                "THEN SET NEW.ID = NEXTVAL FOR SEQ_I_TRIGGER;\n" +
                "END IF;\n" +
                "END\n" +
                "/", "/");
    }

    @Test
    public void isTerminatedCase() throws IOException {
        assertTerminated("SELECT EMPNO, LASTNAME,\n" +
                "       CASE SUBSTR(WORKDEPT,1,1)\n" +
                "       WHEN 'A' THEN 'Administration'\n" +
                "       WHEN 'B' THEN 'Human Resources'\n" +
                "       WHEN 'C' THEN 'Design'\n" +
                "       WHEN 'D' THEN 'Operations'\n" +
                "       END\n" +
                "   FROM EMPLOYEE;", ";");
    }

    private void assertTerminated(String sqlScriptSource, String delimiter) throws IOException {
        DB2SqlStatementBuilder builder = new DB2SqlStatementBuilder(new Delimiter(";", false));
        builder.setDelimiter(new Delimiter(delimiter, false));

        BufferedReader bufferedReader = new BufferedReader(new StringReader(sqlScriptSource));
        int num = 0;
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            num++;
            assertFalse("Line " + num + " should not terminate: " + line, builder.isTerminated());
            builder.addLine(line);
        }

        assertTrue(builder.isTerminated());
    }
}