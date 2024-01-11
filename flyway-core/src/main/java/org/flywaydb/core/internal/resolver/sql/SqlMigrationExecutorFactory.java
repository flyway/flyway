package org.flywaydb.core.internal.resolver.sql;

public interface SqlMigrationExecutorFactory {
    SqlMigrationExecutor createSqlMigrationExecutor();
}