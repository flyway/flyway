package com.google.code.flyway.core.dbsupport;

/**
 * Oracle-specific support.
 */
public class OracleDbSupport implements DbSupport {
    @Override
    public String[] createSchemaMetaDataTableSql(String tableName) {
        //Not implemented yet
        return new String[0];
    }
}
