package org.flywaydb.community.database.databricks;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.SQLException;

public class DatabricksConnection extends Connection<DatabricksDatabase> {
    protected DatabricksConnection(DatabricksDatabase database, java.sql.Connection connection) {
        super(database, connection);
        try {
            connection.setCatalog("hive_metastore");
            connection.setSchema("main");
        } catch (SQLException e) {}
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
        String defaultCatalog = "hive_metastore";
        String currentCatalog = jdbcTemplate.queryForString("SELECT current_catalog()");
        return (currentCatalog != null) ? currentCatalog : defaultCatalog;
    }

    @Override
    public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
        String sql = "USE CATALOG " + database.doQuote(schema);
        jdbcTemplate.execute(sql);
    }

    @Override
    public Schema doGetCurrentSchema() throws SQLException {
        String currentCatalog = getCurrentSchemaNameOrSearchPath();

        if (!StringUtils.hasText(currentCatalog)) {
            throw new FlywayException("Unable to determine current catalog as search_path is empty.");
        }

        return getSchema(currentCatalog);
    }

    @Override
    public Schema getSchema(String name) {
        return new DatabricksSchema(jdbcTemplate, database, name);
    }
}
