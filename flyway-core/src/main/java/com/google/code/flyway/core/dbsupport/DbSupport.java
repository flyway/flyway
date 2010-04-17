package com.google.code.flyway.core.dbsupport;

/**
 * Abstraction for database-specific functionality.
 */
public interface DbSupport {
    /**
     * Generates the sql statements for creating the schema meta-data table.
     *
     * @param tableName The name to give to this table.
     * @return The sql statements.
     */
    String[] createSchemaMetaDataTableSql(String tableName);
}
