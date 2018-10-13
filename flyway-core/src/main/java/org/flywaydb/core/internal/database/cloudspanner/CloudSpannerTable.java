/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.cloudspanner;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.util.StringUtils;

/**
 * Google Cloud Spanner-specific table.
 */
public class CloudSpannerTable extends Table {
    private static final Log LOG = LogFactory.getLog(CloudSpannerTable.class);

    /**
     * Creates a new Google Cloud Spanner table.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param schema       The schema this table lives in.
     * @param name         The name of the table.
     */
    public CloudSpannerTable(JdbcTemplate jdbcTemplate, Database database, Schema schema, String name) {
        super(jdbcTemplate, database, schema, name);
    }

    @Override
    protected void doDrop() throws SQLException {
    	List<String> dropStatements = getDropStatements();
    	Statement statement = jdbcTemplate.getConnection().createStatement();
    	for(String sql : dropStatements) {
    		statement.addBatch(sql);
    	}
    	statement.executeBatch();
    }
    
    protected List<String> getDropStatements() throws SQLException {
    	List<String> res = new ArrayList<>();
    	try(ResultSet rs = jdbcTemplate.getConnection().getMetaData().getIndexInfo("", "", name, false, false)) {
    		while(rs.next()) {
    			if(!rs.getString("INDEX_NAME").equalsIgnoreCase("PRIMARY_KEY")) {
    				String sql = "DROP INDEX " + database.quote(rs.getString("INDEX_NAME"));
    				if(!res.contains(sql))
    					res.add(sql);
    			}
    		}
    	}
        res.add("DROP TABLE " + database.quote(name));
        return res;
    }
    
    /**
     * Checks whether this table is interleaved in the other table (this is a child of the other table). This check does not do a recursive check, i.e. if this table is a child of a child of the other table, then the method will return false.
     * @param other The table to be checked for being a direct parent of this table
     * @return true if this is a direct child of other
     * @throws SQLException 
     */
    public boolean isInterleavedIn(CloudSpannerTable other) throws SQLException {
    	try(ResultSet rs = jdbcTemplate.getConnection().getMetaData().getImportedKeys("", "", this.name)) {
    		while(rs.next()) {
    			String parent = rs.getString("PKTABLE_NAME");
    			if(parent.equalsIgnoreCase(other.name))
    				return true;
    		}
    	}
    	return false;
    }

    @Override
    protected boolean doExists() throws SQLException {
    	try(ResultSet rs = jdbcTemplate.getConnection().getMetaData().getTables("", "", name, null)) {
    		return rs.next();
    	}
    }

    @Override
    protected void doLock() throws SQLException {
    	jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + this);
    }
    
    @Override
    public String toString() {
    	if(StringUtils.hasLength(schema.getName()))
    		return database.quote(schema.getName(), name);
    	return database.quote(name);
    }
}