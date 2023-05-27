package org.flywaydb.community.database.databricks;

import org.flywaydb.core.internal.database.InsertRowLock;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class DatabricksTable extends Table<DatabricksDatabase, DatabricksSchema> {
    private final InsertRowLock insertRowLock;

    /**
     * @param jdbcTemplate The JDBC template for communicating with the DB.
     * @param database     The database-specific support.
     * @param schema       The schema this table lives in.
     * @param name         The name of the table.
     */
    public DatabricksTable(JdbcTemplate jdbcTemplate, DatabricksDatabase database, DatabricksSchema schema, String name) {
        super(jdbcTemplate, database, schema, name);
        this.insertRowLock = new InsertRowLock(jdbcTemplate, 10);
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP TABLE " + database.quote(schema.getName(), name) + ";");
    }

    @Override
    protected boolean doExists() throws SQLException {
        if (!schema.exists()) {
            return false;
        }
        List<Map<String, String>> tables = jdbcTemplate.queryForList(
                "show tables in " + database.quote(schema.getName()) + " like ?",
                name
        );
        return tables.stream().anyMatch(table -> table.get("tableName").equals(name));
    }

    @Override
    protected void doLock() throws SQLException {
        String updateLockStatement = "UPDATE " + this + " SET installed_on = CURRENT_TIMESTAMP() WHERE version = '?' AND DESCRIPTION = 'flyway-lock';";
        String deleteExpiredLockStatement =
                " DELETE FROM " + this +
                        " WHERE DESCRIPTION = 'flyway-lock'" +
                        " AND installed_on < TIMESTAMP '?';";

        if (lockDepth == 0) {
            insertRowLock.doLock(database.getInsertStatement(this), updateLockStatement, deleteExpiredLockStatement, database.getBooleanTrue());
        }
    }

    @Override
    protected void doUnlock() throws SQLException {
        if (lockDepth == 1) {
            insertRowLock.doUnlock(getDeleteLockTemplate());
        }
    }

    private String getDeleteLockTemplate() {
        return "DELETE FROM " + this + " WHERE version = '?' AND DESCRIPTION = 'flyway-lock';";
    }
}
