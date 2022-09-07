package org.flywaydb.community.database.databricks;

import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;

public class DatabricksSchema extends Schema<DatabricksDatabase, DatabricksTable> {
    /**
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param name         The name of the schema.
     */
    public DatabricksSchema(JdbcTemplate jdbcTemplate, DatabricksDatabase database, String name) {
        super(jdbcTemplate, database, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate
                .queryForInt("select count(table_name) from information_schema.tables where table_schema = ?;", name) > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        return jdbcTemplate
                .queryForInt("select count(table_name) from information_schema.tables where table_schema = ?;", name) == 0;
    }

    @Override
    protected void doCreate() throws SQLException {

    }

    @Override
    protected void doDrop() throws SQLException {

    }

    @Override
    protected void doClean() throws SQLException {

    }

    @Override
    protected DatabricksTable[] doAllTables() throws SQLException {
        return new DatabricksTable[0];
    }

    @Override
    public Table getTable(String tableName) {
        return new DatabricksTable(jdbcTemplate, database, this, tableName);
    }
}
