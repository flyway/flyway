/*-
 * ========================LICENSE_START=================================
 * flyway-verb-migrate
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
package org.flywaydb.verb.migrate.migrators;

import org.flywaydb.core.internal.nc.NativeConnectorsDatabase;
import org.flywaydb.nc.NativeConnectorsHybrid;

public class MigratorFactory {
    public static <DB extends NativeConnectorsDatabase> Migrator getMigrator(final DB database) {
        if (database instanceof NativeConnectorsHybrid<?, ?, ?>) {
            return new HybridMigrator();
        }

        return switch (database.getDatabaseMetaData().connectionType()) {
            case API -> new ApiMigrator();
            case JDBC -> new JdbcMigrator();
            case EXECUTABLE -> new ExecutableMigrator();
        };
    }
}
