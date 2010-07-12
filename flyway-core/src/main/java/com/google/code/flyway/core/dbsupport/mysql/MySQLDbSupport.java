/**
 * Copyright (C) 2009-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.code.flyway.core.dbsupport.mysql;

import com.google.code.flyway.core.dbsupport.DbSupport;
import com.google.code.flyway.core.SqlScript;
import com.google.code.flyway.core.SqlStatement;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mysql-specific support.
 */
public class MySQLDbSupport implements DbSupport {
    @Override
    public SqlScript createCreateMetaDataTableScript(String tableName) {
    	Resource resource = new ClassPathResource("com/google/code/flyway/core/dbsupport/mysql/createMetaDataTable.sql");
    	
    	Map<String, String> placeholders = new HashMap<String, String>();
    	placeholders.put("tableName", tableName);

        return new SqlScript(resource, placeholders);
    }

    @Override
    public String getCurrentSchema(JdbcTemplate jdbcTemplate) {
        return (String) jdbcTemplate.execute(new ConnectionCallback() {
            @Override
            public String doInConnection(Connection connection) throws SQLException, DataAccessException {
                return connection.getCatalog();
            }
        });
    }

    @Override
    public boolean supportsDatabase(String databaseProductName) {
        return "MySQL".equals(databaseProductName);
    }

    @Override
    public boolean metaDataTableExists(final JdbcTemplate jdbcTemplate, final String schemaMetaDataTable) {
        return (Boolean) jdbcTemplate.execute(new ConnectionCallback() {
            @Override
            public Boolean doInConnection(Connection connection) throws SQLException, DataAccessException {
                ResultSet resultSet = connection.getMetaData().getTables(getCurrentSchema(jdbcTemplate), null,
                        schemaMetaDataTable, null);
                return resultSet.next();
            }
        });
    }

    @Override
    public boolean supportsDdlTransactions() {
        return false;
    }

    @Override
    public boolean supportsLocking() {
        return true;
    }

    @Override
    public SqlScript createSqlScript(Resource resource, Map<String, String> placeholders) {
        return new SqlScript(resource, placeholders);
    }

    @Override
    public SqlScript createCleanScript(JdbcTemplate jdbcTemplate) {
        List<Map<String,String>> tableNames =
                jdbcTemplate.queryForList(
                        "SELECT table_name FROM information_schema.tables WHERE table_schema=? AND table_type='BASE TABLE'",
                        new Object[] {getCurrentSchema(jdbcTemplate)});
        List<SqlStatement> sqlStatements = new ArrayList<SqlStatement>();
        int lineNumber = 1;
        for (Map<String,String> row : tableNames) {
            String tableName = row.get("table_name");
            sqlStatements.add(new SqlStatement(lineNumber, "DROP TABLE " + tableName));
        }
        return new SqlScript(sqlStatements, "Clean schema " + getCurrentSchema(jdbcTemplate));
    }
}
