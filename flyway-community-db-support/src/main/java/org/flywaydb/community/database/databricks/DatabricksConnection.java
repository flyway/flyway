package org.flywaydb.community.database.databricks;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.SQLException;

public class DatabricksConnection extends Connection<DatabricksDatabase> {
    protected DatabricksConnection(DatabricksDatabase database, java.sql.Connection connection) {
        super(database, connection);
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
        String defaultCatalog = "hive_metastore";
        String currentCatalog = jdbcTemplate.queryForString("SELECT current_catalog();");
        String defaultSchema = "default";
        String currentSchema = jdbcTemplate.queryForString("SELECT current_database();");
        return (currentSchema != null) ? currentSchema : defaultSchema;
    }

    @Override
    public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
        String sql = "USE DATABASE " + database.doQuote(schema) + ";";
        jdbcTemplate.execute(sql);
    }

    @Override
    public Schema doGetCurrentSchema() throws SQLException {
        String currentSchema = getCurrentSchemaNameOrSearchPath();

        if (!StringUtils.hasText(currentSchema)) {
            throw new FlywayException("Unable to determine current schema as currentSchema is empty.");
        }

        return getSchema(currentSchema);
    }

    @Override
    public Schema getSchema(String name) {
        return new DatabricksSchema(jdbcTemplate, database, name);
    }
}
