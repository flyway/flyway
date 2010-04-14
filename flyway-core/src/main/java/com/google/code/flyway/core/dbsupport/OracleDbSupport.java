package com.google.code.flyway.core.dbsupport;

import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * Oracle-specific support.
 */
public class OracleDbSupport extends BaseDbSupport {
    /**
     * Initializes the DbSupport class with this simpleJdbcTemplate for this schema.
     *
     * @param simpleJdbcTemplate The simpleJdbcTemplate to use for accessing the database.
     * @param schemaName         The name of the schema to operate on.
     */
    public OracleDbSupport(SimpleJdbcTemplate simpleJdbcTemplate, String schemaName) {
        super(simpleJdbcTemplate, schemaName);
    }

    @Override
    public boolean tableExists(String tableName) {
        int count = simpleJdbcTemplate.queryForInt(
                "SELECT count(*) FROM user_tables WHERE table_name = ?", tableName);
        return count > 0;
    }

    @Override
    public void createSchemaMetaDataTable(String tableName) {
        //Not implemented yet
    }
}
