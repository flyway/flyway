/*
 * Copyright © Red Gate Software Ltd 2010-2021
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

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.callback.NoopCallbackExecutor;
import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.sqlscript.SqlScriptExecutorFactory;
import org.flywaydb.core.internal.sqlscript.SqlScriptFactory;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class SchemaHistoryFactory {
    private static final Log LOG = LogFactory.getLog(SchemaHistoryFactory.class);

    private SchemaHistoryFactory() {
        // Prevent instantiation
    }

    public static SchemaHistory getSchemaHistory(Configuration configuration,
                                                 SqlScriptExecutorFactory sqlScriptExecutorFactory,
                                                 SqlScriptFactory sqlScriptFactory,
                                                 Database database, Schema schema,
                                                 StatementInterceptor statementInterceptor) {
        Table table = schema.getTable(configuration.getTable());
        JdbcTableSchemaHistory jdbcTableSchemaHistory =
                new JdbcTableSchemaHistory(sqlScriptExecutorFactory, sqlScriptFactory, database, table);









        return jdbcTableSchemaHistory;
    }

    public static SchemaHistory getSchemaHistory(Configuration configuration) {
        JdbcConnectionFactory jdbcConnectionFactory = new JdbcConnectionFactory(
                configuration.getDataSource(),
                configuration,
                null);

        final DatabaseType databaseType = jdbcConnectionFactory.getDatabaseType();
        final ParsingContext parsingContext = new ParsingContext();
        final SqlScriptFactory sqlScriptFactory = databaseType.createSqlScriptFactory(configuration, parsingContext);

        final SqlScriptExecutorFactory noCallbackSqlScriptExecutorFactory = databaseType.createSqlScriptExecutorFactory(
                jdbcConnectionFactory,
                NoopCallbackExecutor.INSTANCE,
                null);

        Database database = databaseType.createDatabase(
                configuration,
                true,
                jdbcConnectionFactory,
                null);

        Pair<Schema, List<Schema>> schemas = prepareSchemas(configuration, database);
        Schema defaultSchema = schemas.getLeft();

        SchemaHistory schemaHistory = SchemaHistoryFactory.getSchemaHistory(
                configuration,
                noCallbackSqlScriptExecutorFactory,
                sqlScriptFactory,
                database,
                defaultSchema,
                null);

        return schemaHistory;
    }

    public static Pair<Schema, List<Schema>> prepareSchemas(Configuration configuration, Database database) {
        String defaultSchemaName = configuration.getDefaultSchema();
        String[] schemaNames = configuration.getSchemas();

        if (!isDefaultSchemaValid(defaultSchemaName, schemaNames)) {
            throw new FlywayException("The defaultSchema property is specified but is not a member of the schemas property");
        }

        LOG.debug("Schemas: " + StringUtils.arrayToCommaDelimitedString(schemaNames));
        LOG.debug("Default schema: " + defaultSchemaName);

        List<Schema> schemas = new ArrayList<>();

        if (schemaNames.length == 0) {
            Schema currentSchema = database.getMainConnection().getCurrentSchema();
            if (currentSchema == null) {
                throw new FlywayException("Unable to determine schema for the schema history table." +
                        " Set a default schema for the connection or specify one using the defaultSchema property!");
            }
            schemas.add(currentSchema);
        } else {
            for (String schemaName : schemaNames) {
                schemas.add(database.getMainConnection().getSchema(schemaName));
            }
            if (defaultSchemaName == null) {
                defaultSchemaName = schemaNames[0];
            }
        }

        Schema defaultSchema = (defaultSchemaName != null)
                ? database.getMainConnection().getSchema(defaultSchemaName)
                : database.getMainConnection().getCurrentSchema();

        return Pair.of(defaultSchema, schemas);
    }

    private static boolean isDefaultSchemaValid(String defaultSchema, String[] schemas) {
        // No default schema specified
        if (defaultSchema == null) {
            return true;
        }
        // Default schema is one of those Flyway is managing
        for (String schema : schemas) {
            if (defaultSchema.equals(schema)) {
                return true;
            }
        }
        return false;
    }
}