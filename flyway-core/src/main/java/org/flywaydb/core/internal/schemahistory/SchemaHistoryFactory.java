/*-
 * ========================LICENSE_START=================================
 * flyway-core
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
package org.flywaydb.core.internal.schemahistory;

import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.NoArgsConstructor;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.sqlscript.SqlScriptExecutorFactory;
import org.flywaydb.core.internal.sqlscript.SqlScriptFactory;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@CustomLog
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SchemaHistoryFactory {
    public static SchemaHistory getSchemaHistory(final Configuration configuration,
        final SqlScriptExecutorFactory sqlScriptExecutorFactory,
        final SqlScriptFactory sqlScriptFactory,
        final Database database,
        final Schema schema,
        final StatementInterceptor statementInterceptor) {
        final Table table = schema.getTable(configuration.getTable());
        final JdbcTableSchemaHistory jdbcTableSchemaHistory = new JdbcTableSchemaHistory(sqlScriptExecutorFactory,
            sqlScriptFactory,
            database,
            table,
            configuration);

        if (statementInterceptor != null) {
            return statementInterceptor.getSchemaHistory(configuration, jdbcTableSchemaHistory);
        }
        return jdbcTableSchemaHistory;
    }

    public static Pair<Schema, List<Schema>> prepareSchemas(final Configuration configuration,
        final Database database) {
        final String[] schemaNames = configuration.getSchemas();
        String defaultSchemaName = configuration.getDefaultSchema();

        LOG.debug("Schemas: " + StringUtils.arrayToCommaDelimitedString(schemaNames));
        LOG.debug("Default schema: " + defaultSchemaName);

        final List<Schema> schemas = new ArrayList<>();
        for (final String schemaName : schemaNames) {
            schemas.add(database.getMainConnection().getSchema(schemaName));
        }

        if (defaultSchemaName == null) {
            if (schemaNames.length == 0) {
                final Schema currentSchema = database.getMainConnection().getCurrentSchema();
                if (currentSchema == null || currentSchema.getName() == null) {
                    throw new FlywayException(
                        "Unable to determine schema for the schema history table. Set a default schema for the connection or specify one using the 'defaultSchema' property");
                }
                defaultSchemaName = currentSchema.getName();
            } else {
                defaultSchemaName = schemaNames[0];
            }
        }

        final Schema defaultSchema = database.getMainConnection().getSchema(defaultSchemaName);
        if (!schemas.contains(defaultSchema)) {
            schemas.add(0, defaultSchema);
        }

        return Pair.of(defaultSchema, schemas);
    }
}
