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

package com.google.code.flyway.core.dbsupport.oracle;

import com.google.code.flyway.core.SqlScript;
import com.google.code.flyway.core.SqlStatement;
import com.google.code.flyway.core.dbsupport.DbSupport;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Oracle-specific support.
 */
public class OracleDbSupport implements DbSupport {
    @Override
    public SqlScript createCreateMetaDataTableScript(String tableName) {
    	Resource resource = new ClassPathResource("com/google/code/flyway/core/dbsupport/oracle/createMetaDataTable.sql");
    	
    	Map<String, String> placeholders = new HashMap<String, String>();
    	placeholders.put("tableName", tableName);

        return new SqlScript(resource, placeholders);
    }

    @Override
    public String getCurrentSchema(JdbcTemplate jdbcTemplate) {
        return (String) jdbcTemplate.execute(new ConnectionCallback() {
            @Override
            public String doInConnection(Connection connection) throws SQLException, DataAccessException {
                return connection.getMetaData().getUserName();
            }
        });
    }

    @Override
    public boolean supportsDatabase(String databaseProductName) {
        return "Oracle".equals(databaseProductName);
    }

    @Override
    public boolean metaDataTableExists(JdbcTemplate jdbcTemplate, String schemaMetaDataTable) {
        int count = jdbcTemplate.queryForInt("SELECT count(*) FROM user_tables WHERE table_name = ?",
                new Object[] {schemaMetaDataTable.toUpperCase()});
        return count > 0;
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
        return new OracleSqlScript(resource, placeholders);
    }

    @Override
    public SqlScript createCleanScript(JdbcTemplate jdbcTemplate) {
        String query = "SELECT 'DROP ' ||  object_type ||' ' || object_name || ' ' || DECODE(OBJECT_TYPE,'TABLE','CASCADE CONSTRAINTS PURGE')" +
                " FROM user_objects WHERE object_type IN ('FUNCTION','MATERIALIZED VIEW','PACKAGE','PROCEDURE','SEQUENCE','SYNONYM','TABLE','TYPE','VIEW') " +
                "order by object_type desc";
        final List<Map<String, Object>> resultSet = jdbcTemplate.queryForList(query);
        int count = 0;
        List<SqlStatement> sqlStatements = new ArrayList<SqlStatement>();
        for (Map<String, Object> row : resultSet) {
            final String dropStatement = (String) row.values().iterator().next();
            count++;
            sqlStatements.add(new SqlStatement(count, dropStatement));
        }
        return new SqlScript(sqlStatements, "oracle drop all objects script");
    }

}
