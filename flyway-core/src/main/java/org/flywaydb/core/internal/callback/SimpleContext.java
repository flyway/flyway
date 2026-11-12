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
package org.flywaydb.core.internal.callback;

import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Error;
import org.flywaydb.core.api.callback.Statement;
import org.flywaydb.core.api.callback.Warning;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.OperationResult;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.nc.NativeConnectorsDatabase;
import org.flywaydb.core.internal.reports.ReportDetails;

@CustomLog
@Getter(onMethod = @__(@Override))
public class SimpleContext implements Context {
    private final Configuration configuration;
    @Getter(AccessLevel.NONE)
    private final Connection connection;
    private final MigrationInfo migrationInfo;
    private final Statement statement;
    private final OperationResult operationResult;
    @Getter
    private final Database database;
    @Getter
    private final NativeConnectorsDatabase ncDatabase;
    private ReportDetails reportDetails;

    public SimpleContext(final Configuration configuration, final ReportDetails reportDetails) {
        this.configuration = configuration;
        this.connection = null;
        this.migrationInfo = null;
        this.statement = null;
        this.operationResult = null;
        this.database = null;
        this.ncDatabase = null;
        this.reportDetails = reportDetails;
    }

    public SimpleContext(final Configuration configuration) {
        this.configuration = configuration;
        this.connection = null;
        this.migrationInfo = null;
        this.statement = null;
        this.operationResult = null;
        this.database = null;
        this.ncDatabase = null;
    }

    public SimpleContext(final Configuration configuration,
        final Connection connection,
        final MigrationInfo migrationInfo,
        final OperationResult operationResult) {
        this.configuration = configuration;
        this.connection = connection;
        this.migrationInfo = migrationInfo;
        this.statement = null;
        this.operationResult = operationResult;
        this.database = null;
        this.ncDatabase = null;
    }

    public SimpleContext(final Configuration configuration,
        final Connection connection,
        final MigrationInfo migrationInfo,
        final OperationResult operationResult,
        final Database database) {
        this.configuration = configuration;
        this.connection = connection;
        this.migrationInfo = migrationInfo;
        this.statement = null;
        this.operationResult = operationResult;
        this.database = database;
        this.ncDatabase = null;
    }

    public SimpleContext(final Configuration configuration,
        final Connection connection,
        final MigrationInfo migrationInfo,
        final String sql,
        final List<Warning> warnings,
        final List<Error> errors) {
        this.configuration = configuration;
        this.connection = connection;
        this.migrationInfo = migrationInfo;
        this.statement = new SimpleStatement(sql, warnings, errors);
        this.operationResult = null;
        this.database = null;
        this.ncDatabase = null;
    }

    public SimpleContext(final Configuration configuration,
        final Connection connection,
        final MigrationInfo migrationInfo,
        final OperationResult operationResult,
        final NativeConnectorsDatabase database) {
        this.configuration = configuration;
        this.connection = connection;
        this.migrationInfo = migrationInfo;
        this.statement = null;
        this.operationResult = operationResult;
        this.ncDatabase = database;
        this.database = null;
    }

    public String getDatabaseName() {
        if (ncDatabase != null) {
            return ncDatabase.getDatabaseMetaData().databaseName();
        }
        try {
            return Optional.ofNullable(database).map(Database::getCatalog).orElse("");
        } catch (final Exception e) {
            LOG.debug("Unable to determine the database name for the deploy event: " + e.getMessage());
            return "";
        }
    }

    public String getDatabaseType() {
        if (ncDatabase != null) {
            return ncDatabase.getDatabaseType();
        }
        return Optional.ofNullable(database).map(d -> d.getDatabaseType().getName()).orElse("");
    }

    @Override
    public java.sql.Connection getConnection() {
        return connection == null ? null : connection.getJdbcConnection();
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter(onMethod = @__(@Override))
    private static class SimpleStatement implements Statement {
        private final String sql;
        private final List<Warning> warnings;
        private final List<Error> errors;
    }
}
