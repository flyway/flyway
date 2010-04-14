package com.google.code.flyway.core.dbsupport;

import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * Mysql-specific support.
 */
public class MySqlDbSupport extends BaseDbSupport {
    /**
     * Initializes the DbSupport class with this simpleJdbcTemplate for this schema.
     *
     * @param simpleJdbcTemplate The simpleJdbcTemplate to use for accessing the database.
     * @param schemaName         The name of the schema to operate on.
     */
    public MySqlDbSupport(SimpleJdbcTemplate simpleJdbcTemplate, String schemaName) {
        super(simpleJdbcTemplate, schemaName);
    }

    @Override
    public boolean tableExists(String tableName) {
        int count = simpleJdbcTemplate.queryForInt(
                "SELECT count(*) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?", schemaName, tableName);
        return count > 0;
    }

    @Override
    public void createSchemaMetaDataTable(String tableName) {
        simpleJdbcTemplate.update("CREATE TABLE " + tableName + " (" +
                "    version VARCHAR(20) NOT NULL UNIQUE," +
                "    description VARCHAR(100)," +
                "\tscript VARCHAR(100) NOT NULL UNIQUE," +
                "    installed_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    execution_time INT," +
                "    state VARCHAR(15) NOT NULL," +
                "    current_version BOOL NOT NULL" +
                "    PRIMARY KEY(version)" +
                ") ENGINE=InnoDB");
        simpleJdbcTemplate.update(
                "ALTER TABLE " + tableName + " ADD INDEX " + tableName + "_current_version_index (current_version)");
    }
}
