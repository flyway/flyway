/**
 * Copyright 2010-2014 Axel Fontaine
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
package org.flywaydb.core.internal.dbsupport.monetdb;

import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * MonetDB-specific table.
 */
public class MonetDBTable extends Table {
    /**
     * Creates a new table.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param dbSupport    The database-specific support.
     * @param schema       The schema this table lives in.
     * @param name         The name of the table.
     */
    public MonetDBTable(JdbcTemplate jdbcTemplate, DbSupport dbSupport, Schema schema, String name) {
        super(jdbcTemplate, dbSupport, schema, name);
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP TABLE " + dbSupport.quote(schema.getName(), name) + " CASCADE");
    }

    @Override
    protected boolean doExists() throws SQLException {
    	// MonetDB need such dirty trick because
    	// there are problems with commiting changes to metadata table after migration.
    	// Migration can be succesfull BUT writing record to matadata table is refused by database. 
    	// JDBC Driver throws SQLException with message "COMMIT: failed".
    	// Strange...
    	if(! jdbcTemplate.getConnection().getAutoCommit()) {
    		jdbcTemplate.getConnection().commit();
    	}
        return exists(null, schema, name);
    }

    @Override
    protected void doLock() throws SQLException {
    	// Dirty trick explained above (in doExists metod).
    	jdbcTemplate.getConnection().commit(); 
    	// MonetDB cannot lock tables for updates.
    	
    	// Maybe creating temporary table before mgration and removing it after???
    	// I don't know....
    	// maybe there is no problem because of optimictic lock?
    }
}
