/*-
 * ========================LICENSE_START=================================
 * flyway-database-snowflake
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.database.snowflake;

import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.resource.StringResource;
import org.flywaydb.core.internal.sqlscript.SqlStatement;
import org.flywaydb.core.internal.sqlscript.SqlStatementIterator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression tests for https://github.com/flyway/flyway/issues/4273.
 *
 * A nested plain BEGIN...EXCEPTION...END; sub-block inside a CREATE TASK body
 * caused the block-depth counter to close the outer BEGIN one END; too early,
 * truncating the statement and dropping whatever followed it.
 */
class SnowflakeParserNestedBeginTest {

    @Test
    void nestedBeginExceptionEndInsideCreateTask() {
        String sql = """
            CREATE OR REPLACE TASK my_schema.my_task
            WAREHOUSE = 'MY_WH'
            SCHEDULE = '5 MINUTE'
            AS
            BEGIN
                LET flag BOOLEAN := FALSE;
                BEGIN
                    SELECT TRUE INTO :flag
                    FROM my_schema.some_control_table
                    WHERE some_condition = TRUE
                    LIMIT 1;
                EXCEPTION
                    WHEN OTHER THEN
                        LET flag := FALSE;
                END;

                IF (:flag) THEN
                    CALL SYSTEM$SET_RETURN_VALUE('a');
                ELSE
                    IF ((SELECT COUNT(*) FROM my_schema.some_table) = 0) THEN
                        CALL SYSTEM$SET_RETURN_VALUE('a');
                    ELSE
                        CALL SYSTEM$SET_RETURN_VALUE('b');
                    END IF;
                END IF;
            END;

            SELECT SYSTEM$TASK_DEPENDENTS_ENABLE('my_schema.my_task');
            """;

        List<String> statements = parseStatements(sql);

        assertEquals(2, statements.size(),
            "Expected 2 statements (CREATE TASK block + trailing SELECT), got " + statements.size());
        assertTrue(statements.get(0).trim().endsWith("END"),
            "First statement should retain the outer block's closing END");
        assertTrue(statements.get(1).trim().startsWith("SELECT SYSTEM$TASK_DEPENDENTS_ENABLE"),
            "Second statement should be the trailing SELECT, not dropped");
    }

    @Test
    void regressionNestedIfFromIssue4179() {
        String sql = """
            DECLARE
              var_one BOOLEAN;
              var_two BOOLEAN;
            BEGIN
              SELECT sysdate() > '1900-01-01' INTO var_one;
              IF (var_one) THEN
                SELECT sysdate() < '1900-01-01' INTO var_two;
                SELECT 'Handle edge cases';
                IF (var_two) THEN
                  SELECT 'Edge case 1' as truth;
                ELSE
                  SELECT 'Edge case 2' as truth;
                END IF;
                SELECT 'Finish the work';
              END IF;
            END;
            SELECT 'Do the normal thing';
            """;

        List<String> statements = parseStatements(sql);

        assertEquals(2, statements.size(),
            "DECLARE block with nested IF should still be parsed as 2 statements, got " + statements.size());
    }

    private List<String> parseStatements(String sql) {
        List<String> statements = new ArrayList<>();

        FluentConfiguration config = new FluentConfiguration();
        ParsingContext parsingContext = new ParsingContext();

        SnowflakeParser parser = new SnowflakeParser(config, parsingContext);

        StringResource resource = new StringResource(sql);
        SqlStatementIterator iterator = parser.parse(resource);

        while (iterator.hasNext()) {
            SqlStatement stmt = iterator.next();
            statements.add(stmt.getSql());
        }

        return statements;
    }
}