package com.google.code.flyway.core.dbsupport;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Mysql-specific support.
 */
public class MySQLDbSupport implements DbSupport {
    @Override
    public String[] createSchemaMetaDataTableSql(String tableName) {
        String createTableSql = "CREATE TABLE " + tableName + " (" +
                "    version VARCHAR(20) NOT NULL UNIQUE," +
                "    description VARCHAR(100)," +
                "    script VARCHAR(100) NOT NULL UNIQUE," +
                "    installed_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    execution_time INT," +
                "    state VARCHAR(15) NOT NULL," +
                "    current_version BOOL NOT NULL," +
                "    PRIMARY KEY(version)" +
                ") ENGINE=InnoDB";
        String addIndexSql =
                "ALTER TABLE " + tableName + " ADD INDEX " + tableName + "_current_version_index (current_version)";

        return new String[] {createTableSql, addIndexSql};
    }

    @Override
    public String getCurrentSchema(Connection connection) throws SQLException {
        return connection.getCatalog();
    }

    @Override
    public boolean supportsDatabase(String databaseProductName) {
        return "MySQL".equals(databaseProductName);
    }

    @Override
    public boolean metaDataTableExists(SimpleJdbcTemplate jdbcTemplate, final String schema, final String schemaMetaDataTable) throws SQLException {
        ResultSet resultSet = (ResultSet) jdbcTemplate.getJdbcOperations().execute(new ConnectionCallback() {
            @Override
            public Object doInConnection(Connection connection) throws SQLException, DataAccessException {
                return connection.getMetaData().getTables(schema, null, schemaMetaDataTable, null);
            }
        });
        return resultSet.next();
    }

    @Override
    public boolean supportsDdlTransactions() {
        return false;
    }
}
