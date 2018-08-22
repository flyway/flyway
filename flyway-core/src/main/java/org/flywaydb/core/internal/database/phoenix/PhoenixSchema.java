/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.phoenix;

import org.apache.phoenix.jdbc.PhoenixConnection;
import org.apache.phoenix.util.SchemaUtil;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PhoenixSchema extends Schema<PhoenixDatabase> {
    private static final Log LOG = LogFactory.getLog(PhoenixSchema.class);

    /**
     * Creates a new schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param name         The name of the schema.
     */
    PhoenixSchema(JdbcTemplate jdbcTemplate, PhoenixDatabase database, String name) {
        super(jdbcTemplate, database, name);
    }

    static boolean isDefaultSchemaName(String schemaNameOrSearchPath) {
        return schemaNameOrSearchPath == null || schemaNameOrSearchPath.isEmpty() || schemaNameOrSearchPath.equals("DEFAULT");
    }

    private boolean schemaCreationSupported() throws SQLException {
        PhoenixConnection connection = jdbcTemplate.getConnection().unwrap(PhoenixConnection.class);
        return SchemaUtil.isNamespaceMappingEnabled(null, connection.getQueryServices().getProps());
    }

    @Override
    protected boolean doExists() throws SQLException {
        if (!schemaCreationSupported() || isDefaultSchemaName(name)) {
            return true;
        } else {
            Connection c = jdbcTemplate.getConnection();
            try (ResultSet rs = c.getMetaData().getSchemas(null, name)) {
                return rs.next();
            }
        }
    }

    @Override
    protected boolean doEmpty() {
        return allTables().length == 0;
    }

    @Override
    protected void doCreate() throws SQLException {
        if (schemaCreationSupported()) {
            jdbcTemplate.execute("CREATE SCHEMA " + database.quote(name));
        } else {
            LOG.info("Creating schema only supported when phoenix.schema.isNamespaceMappingEnabled=true");
        }
    }

    @Override
    protected void doDrop() throws SQLException {
        if (schemaCreationSupported()) {
            jdbcTemplate.execute("DROP SCHEMA " + database.quote(name));
        } else {
            LOG.info("Dropping schema only supported when phoenix.schema.isNamespaceMappingEnabled=true");
        }
    }

    @Override
    protected void doClean() throws SQLException {
        for (Table table : doAllTables()) {
            table.drop();
        }
    }

    @Override
    protected Table[] doAllTables() throws SQLException {
        List<String> tableNames = getAllTableNames();
        Table[] result = new Table[tableNames.size()];

        for (int i = 0; i < tableNames.size(); i++) {
            result[i] = getTable(tableNames.get(i));
        }
        return result;
    }

    private List<String> getAllTableNames() throws SQLException {
        List<String> tableNames = new ArrayList<>();
        Connection c = jdbcTemplate.getConnection();
        try (ResultSet rs = c.getMetaData().getTables(null, name, null, new String[]{"TABLE"})) {
            while (rs.next()) {
                tableNames.add(rs.getString("TABLE_NAME"));
            }
        }
        return tableNames;
    }

    @Override
    public Table getTable(String tableName) {
        return new PhoenixTable(jdbcTemplate, database, this, tableName);
    }
}
