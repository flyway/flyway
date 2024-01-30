package org.flywaydb.core.internal.jdbc;

import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.extensibility.AppliedMigration;
import org.flywaydb.core.extensibility.Plugin;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.sqlscript.SqlStatement;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

public interface StatementInterceptor extends Plugin {
    void init(Configuration configuration, Database database, Table table);

    boolean isConfigured(Configuration configuration);

    List<Callback> getCallbacks();

    Connection createConnectionProxy(Connection connection);

    SchemaHistory getSchemaHistory(Configuration configuration, SchemaHistory jdbcTableSchemaHistory);

    void schemaHistoryTableCreate(boolean baseline);

    void schemaHistoryTableInsert(AppliedMigration appliedMigration);

    void close();

    void sqlScript(LoadableResource resource);

    void scriptMigration(LoadableResource resource);

    void javaMigration(JavaMigration javaMigration);

    void sqlStatement(SqlStatement statement);

    void interceptCommand(String command);

    void interceptStatement(String sql);

    void interceptPreparedStatement(String sql, Map<Integer, Object> params);

    void interceptCallableStatement(String sql);

    void schemaHistoryTableDeleteFailed(Table table, AppliedMigration appliedMigration);
}