package org.flywaydb.core.internal.callback;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Getter;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Error;
import org.flywaydb.core.api.callback.Statement;
import org.flywaydb.core.api.callback.Warning;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.OperationResult;
import org.flywaydb.core.internal.database.base.Connection;

import java.util.List;

@Getter(onMethod = @__(@Override))
public class SimpleContext implements Context {
    private final Configuration configuration;
    @Getter(AccessLevel.NONE)
    private final Connection connection;
    private final MigrationInfo migrationInfo;
    private final Statement statement;
    private final OperationResult operationResult;

    public SimpleContext(Configuration configuration) {
        this.configuration = configuration;
        this.connection = null;
        this.migrationInfo = null;
        this.statement = null;
        this.operationResult = null;
    }

    public SimpleContext(Configuration configuration, Connection connection, MigrationInfo migrationInfo, OperationResult operationResult) {
        this.configuration = configuration;
        this.connection = connection;
        this.migrationInfo = migrationInfo;
        this.statement = null;
        this.operationResult = operationResult;
    }

    public SimpleContext(Configuration configuration, Connection connection, MigrationInfo migrationInfo,
                         String sql, List<Warning> warnings, List<Error> errors) {
        this.configuration = configuration;
        this.connection = connection;
        this.migrationInfo = migrationInfo;
        this.statement = new SimpleStatement(sql, warnings, errors);
        this.operationResult = null;
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