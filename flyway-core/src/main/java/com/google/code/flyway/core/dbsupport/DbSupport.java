package com.google.code.flyway.core.dbsupport;

/**
 * Abstraction for database-specific functionality.
 */
public interface DbSupport {
    /**
     * Checks whether the table with this name already exists in the database or not.
     *
     * @param tableName The name of the table to check.
     *
     * @return {@code true} if the table exists, {@code false} if it doesn't.
     */
    boolean tableExists(String tableName);

    /**
     * Creates the schema meta-data table.
     *
     * @param tableName The name to give to this table.
     */
    void createSchemaMetaDataTable(String tableName);
}
