/*
 * Copyright 2010-2018 Boxfuse GmbH
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

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;

/**
 * Factory to obtain a reference to the schema history.
 */
public class SchemaHistoryFactory {
    private SchemaHistoryFactory() {
        // Prevent instantiation
    }

    /**
     * Obtains a reference to the schema history.
     *
     * @param configuration The current Flyway configuration.
     * @param database      The Database object.
     * @param schema        The schema whose history to track.
     * @return The schema history.
     */
    public static SchemaHistory getSchemaHistory(Configuration configuration, Database database, Schema schema



    ) {
        String installedBy = configuration.getInstalledBy() == null
                ? database.getCurrentUser()
                : configuration.getInstalledBy();

        Table table = schema.getTable(configuration.getTable());
        JdbcTableSchemaHistory jdbcTableSchemaHistory = new JdbcTableSchemaHistory(database, table, installedBy);









        return jdbcTableSchemaHistory;
    }
}