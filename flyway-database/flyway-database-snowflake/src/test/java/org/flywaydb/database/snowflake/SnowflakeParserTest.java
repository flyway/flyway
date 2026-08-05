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

class SnowflakeParserTest {

    @Test
    void parsesDropAgentIfExists() {
        List<SqlStatement> statements = parse("DROP AGENT IF EXISTS DATABASE.SCHEMA.AGENT;");

        assertEquals(1, statements.size());
        assertTrue(statements.get(0).getSql().toUpperCase().contains("DROP AGENT IF EXISTS"));
    }

    @Test
    void parsesCreateAgentIfNotExists() {
        List<SqlStatement> statements = parse("CREATE AGENT IF NOT EXISTS my_agent;");

        assertEquals(1, statements.size());
        assertTrue(statements.get(0).getSql().toUpperCase().contains("CREATE AGENT IF NOT EXISTS"));
    }

    @Test
    void parsesDropAgentWithoutIfExists() {
        List<SqlStatement> statements = parse("DROP AGENT DATABASE.SCHEMA.AGENT;");

        assertEquals(1, statements.size());
        assertTrue(statements.get(0).getSql().toUpperCase().contains("DROP AGENT"));
    }

    @Test
    void parsesDropTableIfExistsStillWorks() {
        List<SqlStatement> statements = parse("DROP TABLE IF EXISTS my_table;");

        assertEquals(1, statements.size());
        assertTrue(statements.get(0).getSql().toUpperCase().contains("DROP TABLE IF EXISTS"));
    }

    private static List<SqlStatement> parse(final String sql) {
        final FluentConfiguration configuration = new FluentConfiguration().placeholderReplacement(false);
        final SnowflakeParser parser = new SnowflakeParser(configuration, new ParsingContext());
        final List<SqlStatement> statements = new ArrayList<>();
        try (SqlStatementIterator iterator = parser.parse(new StringResource(sql))) {
            SqlStatement statement;
            while ((statement = iterator.next()) != null) {
                statements.add(statement);
            }
        }
        return statements;
    }
}
