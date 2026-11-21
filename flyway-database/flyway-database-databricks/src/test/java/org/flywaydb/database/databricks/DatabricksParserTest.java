/*-
 * ========================LICENSE_START=================================
 * flyway-database-databricks
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
package org.flywaydb.database.databricks;

import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.resource.StringResource;
import org.flywaydb.core.internal.sqlscript.SqlStatementIterator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabricksParserTest {
    @Test
    void parsesAnalyzeTableForAllColumns() {
        assertParsesOneStatement("ANALYZE TABLE default.flyway_stats_test COMPUTE STATISTICS FOR ALL COLUMNS;");
    }

    @Test
    void parsesAnalyzeTableForColumnsWithWhitespace() {
        assertParsesOneStatement("ANALYZE TABLE default.flyway_stats_test COMPUTE STATISTICS\n FOR\tCOLUMNS `col1`, `col2`;");
    }

    private void assertParsesOneStatement(final String sql) {
        final DatabricksParser parser = new DatabricksParser(new ClassicConfiguration(), new ParsingContext());
        try (SqlStatementIterator statements = parser.parse(new StringResource(sql))) {
            int statementCount = 0;
            while (statements.hasNext()) {
                statements.next();
                statementCount++;
            }
            assertEquals(1, statementCount);
        }
    }
}
