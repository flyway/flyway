/**
 * Copyright 2010-2013 Axel Fontaine and the many contributors.
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
/**
 *
 */
package com.googlecode.flyway.core.dbsupport.nonstop;

import com.googlecode.flyway.core.api.FlywayException;
import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.dbsupport.JdbcTemplate;
import com.googlecode.flyway.core.dbsupport.Schema;
import com.googlecode.flyway.core.dbsupport.Table;
import com.googlecode.flyway.core.util.logging.Log;
import com.googlecode.flyway.core.util.logging.LogFactory;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import java.sql.SQLException;
import java.util.ArrayList;
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
        ResultSet rs = metaData.getSchemas();//(catalog, "NRT");
        String s1, s2;
        if(rs != null){
            while(rs.next()){
                s1 = rs.getString(1);
                s2 = rs.getString(2);
                //System.out.println(s2+""+s1);
                if(s2.equalsIgnoreCase(catalog) && s1.equalsIgnoreCase(name)){
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
        ResultSet rs = metaData.getSchemas();//(catalog, "NRT");
        if(rs != null){
            while(rs.next()){
                if(rs.getString(2).equalsIgnoreCase(catalog)){
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
        //TODO: static sql query for prism86 and NRT.
        String catalog = jdbcTemplate.getConnection().getCatalog();
        //List<String> tableNames = jdbcTemplate.queryForStringList("select OBJECT_NAME from PRISM86.DEFINITION_SCHEMA_VERSION_3100.OBJECTS O where O.SCHEMA_UID=(select SCHEMA_UID from NONSTOP_SQLMX_HPPRISM.SYSTEM_SCHEMA.SCHEMATA S where S.CAT_UID = (select CAT_UID from NONSTOP_SQLMX_HPPRISM.SYSTEM_SCHEMA.CATSYS C where C.CAT_NAME = 'PRISM86') and S.SCHEMA_NAME = 'NRT') and O.OBJECT_TYPE='BT'", name);
        List<String> tableNames = jdbcTemplate.queryForStringList("select OBJECT_NAME from "+catalog+".DEFINITION_SCHEMA_VERSION_3100.OBJECTS O where O.SCHEMA_UID=(select SCHEMA_UID from NONSTOP_SQLMX_HPPRISM.SYSTEM_SCHEMA.SCHEMATA S where S.CAT_UID = (select CAT_UID from NONSTOP_SQLMX_HPPRISM.SYSTEM_SCHEMA.CATSYS C where C.CAT_NAME = '"+catalog+"') and S.SCHEMA_NAME = ? ) and O.OBJECT_TYPE='BT'", name);
        
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
    private String node;

    /*private String getNode() throws SQLException {
        if (node == null) {
            DatabaseMetaData metaData = jdbcTemplate.getMetaData();
            ResultSet rs = metaData.getSchemas();//(catalog, "NRT");
            if (rs != null) {
                while (rs.next()) {
                    if (rs.getString(2).startsWith("NONSTOP_SQLMX_")) {
                        node = rs.getString(2).substring(14);//remove NONSTOP_SQLMX_
                        break;
                    }
                }
                rs.close();
            }
        }
        return node;
    }*/
}
