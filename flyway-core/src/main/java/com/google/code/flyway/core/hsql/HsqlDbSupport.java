package com.google.code.flyway.core.hsql;

import com.google.code.flyway.core.DbSupport;
import com.google.code.flyway.core.SqlScript;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * HsqlDb-specific support
 */
public class HsqlDbSupport implements DbSupport {
    @Override
    public String[] createSchemaMetaDataTableSql(String tableName) {
        String createTableSql = "CREATE TABLE " + tableName + " (" +
                "    version VARCHAR(20) PRIMARY KEY," +
                "    description VARCHAR(100)," +
                "    script VARCHAR(100) NOT NULL," +
                "    installed_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    execution_time INT," +
                "    state VARCHAR(15) NOT NULL," +
                "    current_version BIT NOT NULL," +
                "    CONSTRAINT unique_script UNIQUE (script)" +
                ")";
        String addIndexSql =
                "CREATE INDEX " + tableName + "_current_version_index ON " + tableName + " (current_version)";

        return new String[]{createTableSql, addIndexSql};
    }

    @Override
    public String getCurrentSchema(SimpleJdbcTemplate jdbcTemplate) {
        return jdbcTemplate.getJdbcOperations().execute(new ConnectionCallback<String>() {
            @Override
            public String doInConnection(Connection connection) throws SQLException, DataAccessException {
                ResultSet resultSet = connection.getMetaData().getSchemas();
                while (resultSet.next()) {
                   if (resultSet.getBoolean("IS_DEFAULT")) {
                       return resultSet.getString("TABLE_SCHEM");
                   }
                }
                return null;
            }
        });
    }

    @Override
    public boolean supportsDatabase(String databaseProductName) {
        return "HSQL Database Engine".equals(databaseProductName);
    }

    @Override
    public boolean metaDataTableExists(final SimpleJdbcTemplate jdbcTemplate, final String schemaMetaDataTable) {
        return jdbcTemplate.getJdbcOperations().execute(new ConnectionCallback<Boolean>() {
            @Override
            public Boolean doInConnection(Connection connection) throws SQLException, DataAccessException {
                ResultSet resultSet =
                        connection.getMetaData().getTables(null, getCurrentSchema(jdbcTemplate), schemaMetaDataTable.toUpperCase(), null);
                return resultSet.next();
            }
        });
    }

    @Override
    public boolean supportsDdlTransactions() {
        return false;
    }

    @Override
    public SqlScript createSqlScript(Resource resource, Map<String, String> placeholders) {
        return new SqlScript(resource, placeholders);
    }
}
