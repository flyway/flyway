package org.flywaydb.community.database.databricks;

import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;

public class DatabricksTable extends Table<DatabricksDatabase, DatabricksSchema> {
    /**
     * @param jdbcTemplate The JDBC template for communicating with the DB.
     * @param database     The database-specific support.
     * @param schema       The schema this table lives in.
     * @param name         The name of the table.
     */
    public DatabricksTable(JdbcTemplate jdbcTemplate, DatabricksDatabase database, DatabricksSchema schema, String name) {
        super(jdbcTemplate, database, schema, name);
    }

    @Override
    protected void doDrop() throws SQLException {

    }

    @Override
    protected boolean doExists() throws SQLException {
        if (!schema.exists()) {
            return false;
        }
        return jdbcTemplate
                .queryForInt("select count(table_name) from information_schema.tables where table_schema = ? and table_name = ?;", schema.getName(), name) > 0;
    }

    @Override
    protected void doLock() throws SQLException {

    }
}
