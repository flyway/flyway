/**
 * Copyright 2010-2013 Axel Fontaine and the many contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
/**
 *
 */
package com.googlecode.flyway.core.dbsupport.nonstop;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.dbsupport.JdbcTemplate;
import com.googlecode.flyway.core.dbsupport.Schema;
import com.googlecode.flyway.core.dbsupport.Table;
import com.googlecode.flyway.core.util.logging.Log;
import com.googlecode.flyway.core.util.logging.LogFactory;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import java.sql.SQLException;
import java.util.List;

/**
 * NonStop implementation of Schema.
 */
public class NonStopSchema extends Schema {

    private static final Log LOG = LogFactory.getLog(NonStopSchema.class);

    /**
     * Creates a new NonStop schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param dbSupport The database-specific support.
     * @param name The name of the schema.
     */
    public NonStopSchema(JdbcTemplate jdbcTemplate, DbSupport dbSupport, String name) {
        super(jdbcTemplate, dbSupport, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        boolean exists = false;
        String catalog = jdbcTemplate.getConnection().getCatalog();
        DatabaseMetaData metaData = jdbcTemplate.getMetaData();
        ResultSet rs = metaData.getSchemas();
        String schemaName, catalogName;
        if (rs != null) {
            while (rs.next()) {
                schemaName = rs.getString(1);//Schema Name
                catalogName = rs.getString(2);//Catalog name
                if (catalogName.equalsIgnoreCase(catalog) && schemaName.equalsIgnoreCase(name)) {
                    exists = true;
                    break;
                }
            }
            rs.close();
        }
        return exists;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        boolean empty = true;
        String catalog = jdbcTemplate.getConnection().getCatalog();
        DatabaseMetaData metaData = jdbcTemplate.getMetaData();
        ResultSet rs = metaData.getSchemas();
        if (rs != null) {
            while (rs.next()) {
                if (rs.getString(2).equalsIgnoreCase(catalog)) {
                    empty = false;
                    break;
                }
            }
            rs.close();
        }
        return empty;
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.execute("CREATE SCHEMA " + dbSupport.quote(name));
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP SCHEMA " + dbSupport.quote(name));
    }

    @Override
    protected void doClean() throws SQLException {
        doDrop();
        doCreate();
    }

    @Override
    protected Table[] doAllTables() throws SQLException {
        String rootCatalog = "", rootSchema = "";
        String catalog = jdbcTemplate.getConnection().getCatalog();
        DatabaseMetaData metaData = jdbcTemplate.getMetaData();
        ResultSet rs = metaData.getSchemas();
        String schemaName, catalogName;
        if (rs != null) {
            while (rs.next()) {
                if (!rootCatalog.isEmpty() && !rootSchema.isEmpty()) {
                    //Retreived Both Root Catalog and RootSchema. Break from Loop.
                    break;
                }
                schemaName = rs.getString(1);//Schema Name
                catalogName = rs.getString(2);//Catalog name
                if (catalogName.startsWith("NONSTOP_SQLMX_")) {//Catalog Starting with NONSTOP_SQLMX_ is reserved 
                    rootCatalog = catalogName;
                    //break;
                }
                if (schemaName.startsWith("DEFINITION_SCHEMA_VERSION_")) {//Schemas starting with DEFINITION_SCHEMA_VERSION_ are reserved
                    rootSchema = schemaName;
                }

            }
            rs.close();
        }

        List<String> tableNames = jdbcTemplate.queryForStringList("select OBJECT_NAME from " + catalog + "." + rootSchema + ".OBJECTS O where O.SCHEMA_UID=(select SCHEMA_UID from " + rootCatalog + ".SYSTEM_SCHEMA.SCHEMATA S where S.CAT_UID = (select CAT_UID from " + rootCatalog + ".SYSTEM_SCHEMA.CATSYS C where C.CAT_NAME = '" + catalog + "') and S.SCHEMA_NAME = ? ) and O.OBJECT_TYPE='BT'", name);

        Table[] tables = new Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new NonStopTable(jdbcTemplate, dbSupport, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    public Table getTable(String tableName) {
        return new NonStopTable(jdbcTemplate, dbSupport, this, tableName);
    }
}
