/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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
    public static SchemaHistory getSchemaHistory(Configuration configuration, SqlScriptExecutorFactory sqlScriptExecutorFactory, SqlScriptFactory sqlScriptFactory,
                                                 Database database, Schema schema, StatementInterceptor statementInterceptor) {
        Table table = schema.getTable(configuration.getTable());
        JdbcTableSchemaHistory jdbcTableSchemaHistory = new JdbcTableSchemaHistory(sqlScriptExecutorFactory, sqlScriptFactory, database, table);








        return jdbcTableSchemaHistory;
    }

    public static Pair<Schema, List<Schema>> prepareSchemas(Configuration configuration, Database database) {
        String[] schemaNames = configuration.getSchemas();
        String defaultSchemaName = configuration.getDefaultSchema();

        LOG.debug("Schemas: " + StringUtils.arrayToCommaDelimitedString(schemaNames));
        LOG.debug("Default schema: " + defaultSchemaName);

        List<Schema> schemas = new ArrayList<>();
        for (String schemaName : schemaNames) {
            schemas.add(database.getMainConnection().getSchema(schemaName));
        }

        if (defaultSchemaName == null) {
            if (schemaNames.length == 0) {
                Schema currentSchema = database.getMainConnection().getCurrentSchema();
                if (currentSchema == null || currentSchema.getName() == null) {
                    throw new FlywayException("Unable to determine schema for the schema history table. Set a default schema for the connection or specify one using the 'defaultSchema' property");
                }
                defaultSchemaName = currentSchema.getName();
            } else {
                defaultSchemaName = schemaNames[0];
            }
        }

        Schema defaultSchema = database.getMainConnection().getSchema(defaultSchemaName);
        if (!schemas.contains(defaultSchema)) {
            schemas.add(0, defaultSchema);
        }

        return Pair.of(defaultSchema, schemas);
    }
}