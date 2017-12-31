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

import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.database.Table;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Google Cloud Spanner implementation of Schema.
 */
public class CloudSpannerSchema extends Schema<CloudSpannerDatabase> {
    private static final Log LOG = LogFactory.getLog(CloudSpannerSchema.class);

    /**
     * Creates a new Google Cloud Spanner schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param name         The name of the schema.
     */
    CloudSpannerSchema(JdbcTemplate jdbcTemplate, CloudSpannerDatabase database, String name) {
        super(jdbcTemplate, database, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
    	return "".equals(name) || "INFORMATION_SCHEMA".equalsIgnoreCase(name);
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        Table[] tables = allTables();
        return tables.length == 0;
    }

    @Override
    protected void doCreate() throws SQLException {
        LOG.info("Google Cloud Spanner does not support creating schemas. Schema not created: " + name);
    }

    @Override
    protected void doDrop() throws SQLException {
        LOG.info("Google Cloud Spanner does not support dropping schemas. Schema not dropped: " + name);
    }

    @Override
    protected void doClean() throws SQLException {
    	List<CloudSpannerTable> tables = new ArrayList<>();
        for (CloudSpannerTable table : doAllTables()) {
        	tables.add(table);
        }
        // Sort the tables so that tables that are interleaved in others come first
        try {
        	tables.sort(new InterleavedTableComparator());
        }
        catch(RuntimeException e) {
        	// see InterleavedTableComparator#compare
        	if(e.getCause() != null && e.getCause() instanceof SQLException)
        		throw (SQLException) e.getCause();
        	throw e;
        }
    	List<String> dropStatements = new ArrayList<>();
    	for(CloudSpannerTable table : tables) {
    		dropStatements.addAll(table.getDropStatements());
    	}
    	Statement statement = jdbcTemplate.getConnection().createStatement();
    	for(String sql : dropStatements) {
    		statement.addBatch(sql);
    	}
        statement.executeBatch();
    }

    @Override
    protected CloudSpannerTable[] doAllTables() throws SQLException {
    	List<CloudSpannerTable> tables = new ArrayList<>();
        try(ResultSet rs = jdbcTemplate.getConnection().getMetaData().getTables("", "", null, null)) {
	        while(rs.next())
	        {
	        	tables.add(new CloudSpannerTable(jdbcTemplate, database, this, rs.getString("TABLE_NAME")));
	        }
        }
        return tables.toArray(new CloudSpannerTable[tables.size()]);
    }

    @Override
    public CloudSpannerTable getTable(String tableName) {
        return new CloudSpannerTable(jdbcTemplate, database, this, tableName);
    }
}