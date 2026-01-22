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
import org.flywaydb.core.internal.sqlscript.SqlStatement;
import org.flywaydb.core.internal.sqlscript.SqlStatementIterator;
import org.flywaydb.core.internal.resource.StringResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SnowflakeParser to verify correct parsing of nested IF statements.
 *
 * These tests address GitHub Issue #4179: Flyway generates invalid SQL when running
 * multi-statement Snowflake migrations with nested IF statements.
 *
 * @see <a href="https://github.com/flyway/flyway/issues/4179">Issue #4179</a>
 */
class SnowflakeParserNestedIfTest {

    /**
     * Tests that nested IF statements within a DECLARE block are parsed correctly.
     *
     * Before the fix, the parser incorrectly terminated the block at the first "END IF",
     * treating it as the block terminator instead of the IF statement terminator.
     *
     * Expected: 2 statements (DECLARE block + SELECT statement)
     * Bug behavior: 3 statements (incomplete DECLARE block + "END" + SELECT statement)
     */
    @Test
    @DisplayName("Issue #4179: Nested IF statements should be parsed as a single DECLARE block")
    void testNestedIfStatementsParsing() {
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
            "DECLARE block with nested IF should be parsed as 2 statements, but got " + statements.size());

        String firstStatement = statements.get(0);
        assertTrue(firstStatement.trim().toUpperCase().startsWith("DECLARE"),
            "First statement should start with DECLARE");
        assertTrue(firstStatement.contains("END IF;"),
            "First statement should contain 'END IF;'");
        assertTrue(firstStatement.trim().toUpperCase().endsWith("END"),
            "First statement should end with 'END'");

        String secondStatement = statements.get(1);
        assertTrue(secondStatement.trim().toUpperCase().startsWith("SELECT"),
            "Second statement should start with SELECT");
        assertTrue(secondStatement.contains("Do the normal thing"),
            "Second statement should contain 'Do the normal thing'");
    }

    /**
     * Tests that sequential (non-nested) IF statements are parsed correctly.
     * This is a regression test based on the issue comment example.
     */
    @Test
    @DisplayName("Issue #4179: Sequential IF statements should be parsed as a single DECLARE block")
    void testSequentialIfStatementsParsing() {
        String sql = """
            DECLARE
              var_one BOOLEAN;
              var_two BOOLEAN;
            BEGIN
              SELECT sysdate() > '1900-01-01' INTO var_one;
              IF (var_one) THEN
                SELECT sysdate() < '1900-01-01' INTO var_two;
              END IF;
              IF (var_one and var_two) THEN
                SELECT 'Case 1' as truth;
              ELSE
                SELECT 'Case 2' as truth;
              END IF;
              IF (var_one) THEN
                SELECT 'Finish';
              END IF;
            END;
            """;

        List<String> statements = parseStatements(sql);

        assertEquals(1, statements.size(),
            "DECLARE block with sequential IF statements should be parsed as 1 statement, but got " + statements.size());

        String statement = statements.get(0);
        int endIfCount = countOccurrences(statement.toUpperCase(), "END IF");
        assertEquals(3, endIfCount, "Statement should contain 3 END IF clauses, but found " + endIfCount);
    }

    /**
     * Tests that a simple IF statement (without nesting) is parsed correctly.
     * This ensures the fix doesn't break basic IF statement parsing.
     */
    @Test
    @DisplayName("Simple IF statement should be parsed correctly")
    void testSimpleIfStatementParsing() {
        String sql = """
            DECLARE
              var_one BOOLEAN;
            BEGIN
              SELECT true INTO var_one;
              IF (var_one) THEN
                SELECT 'yes';
              END IF;
            END;
            """;

        List<String> statements = parseStatements(sql);

        assertEquals(1, statements.size(),
            "DECLARE block with simple IF should be parsed as 1 statement, but got " + statements.size());
    }

    /**
     * Tests deeply nested IF statements (3 levels deep).
     */
    @Test
    @DisplayName("Deeply nested IF statements (3 levels) should be parsed correctly")
    void testDeeplyNestedIfStatementsParsing() {
        String sql = """
            DECLARE
              a BOOLEAN;
              b BOOLEAN;
              c BOOLEAN;
            BEGIN
              SELECT true INTO a;
              SELECT true INTO b;
              SELECT true INTO c;
              IF (a) THEN
                SELECT 'level 1';
                IF (b) THEN
                  SELECT 'level 2';
                  IF (c) THEN
                    SELECT 'level 3';
                  END IF;
                END IF;
              END IF;
            END;
            """;

        List<String> statements = parseStatements(sql);

        assertEquals(1, statements.size(),
            "DECLARE block with 3-level nested IF should be parsed as 1 statement, but got " + statements.size());

        String statement = statements.get(0);
        int endIfCount = countOccurrences(statement.toUpperCase(), "END IF");
        assertEquals(3, endIfCount, "Statement should contain 3 END IF clauses, but found " + endIfCount);
    }

    /**
     * Tests IF-ELSEIF-ELSE structure parsing.
     */
    @Test
    @DisplayName("IF-ELSEIF-ELSE structure should be parsed correctly")
    void testIfElseIfElseParsing() {
        String sql = """
            DECLARE
              val INTEGER;
            BEGIN
              SELECT 2 INTO val;
              IF (val = 1) THEN
                SELECT 'one';
              ELSEIF (val = 2) THEN
                SELECT 'two';
              ELSE
                SELECT 'other';
              END IF;
            END;
            """;

        List<String> statements = parseStatements(sql);

        assertEquals(1, statements.size(),
            "DECLARE block with IF-ELSEIF-ELSE should be parsed as 1 statement, but got " + statements.size());
    }


    /**
     * Parses SQL using SnowflakeParser and returns the list of parsed statements.
     */
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

    /**
     * Counts occurrences of a pattern in a string.
     */
    private int countOccurrences(String str, String pattern) {
        int count = 0;
        int idx = 0;
        while ((idx = str.indexOf(pattern, idx)) != -1) {
            count++;
            idx += pattern.length();
        }
        return count;
    }
}
