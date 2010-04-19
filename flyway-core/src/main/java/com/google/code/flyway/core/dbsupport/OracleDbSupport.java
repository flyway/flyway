package com.google.code.flyway.core.dbsupport;

/**
 * Oracle-specific support.
 */
public class OracleDbSupport implements DbSupport {
    /**
     * The oracle database product name as reported through the jdbc connection metadata.
     */
    public static final String DATABASE_PRODUCT_NAME = "Oracle";

    @Override
    public String[] createSchemaMetaDataTableSql(String tableName) {
        String createTableSql = "CREATE TABLE " + tableName + " (" +
                "    version VARCHAR2(20) NOT NULL PRIMARY KEY," +
                "    description VARCHAR2(100)," +
                "    script VARCHAR2(100) NOT NULL UNIQUE," +
                "    installed_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    execution_time INT," +
                "    state VARCHAR2(15) NOT NULL," +
                "    current_version NUMBER(1) NOT NULL" +
                ")";
        String addIndexSql =
                "ALTER TABLE " + tableName + " ADD INDEX " + tableName + "_current_version_index (current_version)";

        return new String[] {createTableSql, addIndexSql};
    }
}
