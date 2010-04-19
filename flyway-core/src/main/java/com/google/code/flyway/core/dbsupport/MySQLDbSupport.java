package com.google.code.flyway.core.dbsupport;

/**
 * Mysql-specific support.
 */
public class MySQLDbSupport implements DbSupport {
    /**
     * The mysql database product name as reported through the jdbc connection metadata.
     */
    public static final String DATABASE_PRODUCT_NAME = "MySQL";

    @Override
    public String[] createSchemaMetaDataTableSql(String tableName) {
        String createTableSql = "CREATE TABLE " + tableName + " (" +
                "    version VARCHAR(20) NOT NULL UNIQUE," +
                "    description VARCHAR(100)," +
                "    script VARCHAR(100) NOT NULL UNIQUE," +
                "    installed_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    execution_time INT," +
                "    state VARCHAR(15) NOT NULL," +
                "    current_version BOOL NOT NULL," +
                "    PRIMARY KEY(version)" +
                ") ENGINE=InnoDB";
        String addIndexSql =
                "ALTER TABLE " + tableName + " ADD INDEX " + tableName + "_current_version_index (current_version)";

        return new String[] {createTableSql, addIndexSql};
    }
}
