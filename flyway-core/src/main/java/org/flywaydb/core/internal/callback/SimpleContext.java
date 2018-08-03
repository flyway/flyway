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
package org.flywaydb.core.internal.callback;

import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Error;
import org.flywaydb.core.api.callback.Statement;
import org.flywaydb.core.api.callback.Warning;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Connection;

import java.util.List;

public class SimpleContext implements Context {
    private final Configuration configuration;
    private final Connection connection;
    private final MigrationInfo migrationInfo;
    private final Statement statement;

    SimpleContext(Configuration configuration, Connection connection, MigrationInfo migrationInfo) {
        this.configuration = configuration;
        this.connection = connection;
        this.migrationInfo = migrationInfo;
        this.statement = null;
    }

    public SimpleContext(Configuration configuration, Connection connection, MigrationInfo migrationInfo,
                         String sql, List<Warning> warnings, List<Error> errors) {
        this.configuration = configuration;
        this.connection = connection;
        this.migrationInfo = migrationInfo;
        this.statement = new SimpleStatement(sql, warnings, errors);
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public java.sql.Connection getConnection() {
        return connection.getJdbcConnection();
    }

    @Override
    public MigrationInfo getMigrationInfo() {
        return migrationInfo;
    }

    @Override
    public Statement getStatement() {
        return statement;
    }


    private static class SimpleStatement implements Statement {
        private final String sql;
        private final List<Warning> warnings;
        private final List<Error> errors;
        private boolean suppressErrors;

        private SimpleStatement(String sql, List<Warning> warnings, List<Error> errors) {
            this.sql = sql;
            this.warnings = warnings;
            this.errors = errors;
        }

        @Override
        public String getSql() {
            return sql;
        }

        @Override
        public List<Warning> getWarnings() {
            return warnings;
        }

        @Override
        public List<Error> getErrors() {
            return errors;
        }

        @Override
        public boolean isSuppressErrors() {
            return suppressErrors;
        }

        @Override
        public void setSuppressErrors(boolean suppressErrors) {
            this.suppressErrors = suppressErrors;
        }
    }
}