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
package org.flywaydb.core.internal.dbsupport.saphana;

import junit.framework.AssertionFailedError;
import org.flywaydb.core.internal.util.StringUtils;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Test for PostgreSQLSqlStatementBuilder.
 */
public class SapHanaSqlStatementBuilderSmallTest {
    /**
     * Class under test.
     */
    private SapHanaSqlStatementBuilder statementBuilder = new SapHanaSqlStatementBuilder();

    @Test
    public void unicodeString() {
        String sqlScriptSource = "SELECT 'Bria n' \"character string 1\", '100' \"character string 2\", N'abc' \"unicode string\" FROM DUMMY;\n";

        String[] lines = StringUtils.tokenizeToStringArray(sqlScriptSource, "\n");
        for (String line : lines) {
            statementBuilder.addLine(line);
        }

        assertTrue(statementBuilder.isTerminated());
    }

    @Test
    public void binaryString() {
        String sqlScriptSource = "SELECT X'00abcd' \"binary string 1\", x'dcba00' \"binary string 2\" FROM DUMMY;";

        String[] lines = StringUtils.tokenizeToStringArray(sqlScriptSource, "\n");
        for (String line : lines) {
            statementBuilder.addLine(line);
        }

        assertTrue(statementBuilder.isTerminated());
    }

    @Test
    public void binaryStringNot() {
        String sqlScriptSource = "SELECT '00abcd X' \"not a binary string 1\" FROM DUMMY;";

        String[] lines = StringUtils.tokenizeToStringArray(sqlScriptSource, "\n");
        for (String line : lines) {
            statementBuilder.addLine(line);
        }

        assertTrue(statementBuilder.isTerminated());
    }

    @Test
    public void temporalString() {
        String sqlScriptSource = "SELECT date'2010-01-01' \"date\", time'11:00:00.001' \"time\", timestamp'2011-12-31 23:59:59' \"timestamp\" FROM DUMMY;";

        String[] lines = StringUtils.tokenizeToStringArray(sqlScriptSource, "\n");
        for (String line : lines) {
            statementBuilder.addLine(line);
        }

        assertTrue(statementBuilder.isTerminated());
    }

    @Test
    public void parseProcedureWithEmbeddedSemicola() {
        final String sqlScriptSource = readResource(PROCEDURE_TEST_SCRIPT_LOCATION);
        SapHanaSqlStatementBuilder statementBuilder = new SapHanaSqlStatementBuilder();
        String[] lines = StringUtils.tokenizeToStringArray(sqlScriptSource, "\n");
        final List<String> statements = new ArrayList<String>();
        for (String line : lines) {
            statementBuilder.addLine(line);
            if (statementBuilder.isTerminated()) {
                statements.add(statementBuilder.getSqlStatement().getSql());
                statementBuilder = new SapHanaSqlStatementBuilder();
            }
        }
        assertEquals("Should recognize exactly three statements", 3, statements.size());
    }

    private static final String PROCEDURE_TEST_SCRIPT_LOCATION = "/org/flywaydb/core/internal/dbsupport/saphana/procedures/procedure.sql";

    private static String readResource(String name) {
        InputStream resource = null;
        BufferedReader reader = null;
        StringWriter content = null;
        try {
            resource = SapHanaSqlStatementBuilderSmallTest.class.getResourceAsStream(name);
            if (null == resource) {
                throw new AssertionFailedError("Could not read test data file '" + name + '"');
            }
            reader = new BufferedReader(new InputStreamReader(resource));
            content = new StringWriter();
            final BufferedWriter writer = new BufferedWriter(content);
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }
            writer.close();
        } catch (IOException iox) {
            throw new RuntimeException("Problem reading test data file '" + name + "'", iox);
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException iox) {
                    throw new RuntimeException("Problem closing test data file '" + name + "'", iox);
                }
            }
        }
        return content.toString();
    }
}