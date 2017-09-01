/*
 * Copyright 2017 ScuteraTech Unip.LDA
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
package org.flywaydb.core.internal.dbsupport.neo4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.FlywaySqlException;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlScript;
import org.flywaydb.core.internal.dbsupport.Table;
import org.flywaydb.core.internal.metadatatable.AppliedMigration;
import org.flywaydb.core.internal.metadatatable.MetaDataTable;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.jdbc.RowMapper;
import org.flywaydb.core.internal.util.jdbc.TransactionTemplate;
import org.flywaydb.core.internal.util.scanner.classpath.ClassPathResource;

public class Neo4JMetaDataTable implements MetaDataTable {

	private static final Log LOG = LogFactory.getLog(Neo4JMetaDataTable.class);
	
	
	private final DbSupport dbSupport;
	private final Table table;
	private final JdbcTemplate jdbcTemplate;
	
	private final LinkedList<AppliedMigration> cache = new LinkedList<AppliedMigration>();
	
	private String installedBy;

	public Neo4JMetaDataTable(DbSupport dbSupport, Table table, String installedBy) {
	        this.jdbcTemplate = dbSupport.getJdbcTemplate();
	        this.dbSupport = dbSupport;
	        this.table = table;
	        if (installedBy == null) {
	            this.installedBy = dbSupport.getCurrentUserFunction();
	        } else {
	            this.installedBy = "'" + installedBy + "'";
	        }
	    }

	public Neo4JMetaDataTable(DbSupport dbSupport, Table table) {
		this(dbSupport,table, null);
    }
	
	@Override
	public <T> T lock(Callable<T> callable) {
		return dbSupport.lock(table, callable);
	}

	
	@Override
	public void addAppliedMigration(AppliedMigration appliedMigration) {
		dbSupport.changeCurrentSchemaTo(table.getSchema());

        MigrationVersion version = appliedMigration.getVersion();

        try {
            String versionStr = version == null ? "null" : version.toString();
            
            jdbcTemplate.execute("MERGE (schemaVersion :schema_version);");
            String resourceName = "org/flywaydb/core/internal/dbsupport/" + dbSupport.getDbName() + "/insertIntoMetaDataTable.sql";
            ClassPathResource classPathResource = new ClassPathResource(resourceName, getClass().getClassLoader());
            int installedRank = calculateInstalledRank();
            if (classPathResource.exists()) {
                String source = classPathResource.loadAsString("UTF-8");
                Map<String, String> placeholders = new HashMap<String, String>();

                // Placeholders for schema and table
                placeholders.put("schema", table.getSchema().getName());
                placeholders.put("table", table.getName());

                // Placeholders for column values
                placeholders.put("installed_rank", String.valueOf(installedRank));
                placeholders.put("version_val", dbSupport.quote(versionStr));
                placeholders.put("description_val",  dbSupport.quote(appliedMigration.getDescription()));
                placeholders.put("type", dbSupport.quote(appliedMigration.getType().name()));
                placeholders.put("script", dbSupport.quote(appliedMigration.getScript()));
                placeholders.put("checksum", String.valueOf(appliedMigration.getChecksum()));           
                placeholders.put("installed_by", dbSupport.quote(installedBy));
                placeholders.put("installed_on", dbSupport.quote(Timestamp.valueOf(LocalDateTime.now()).toString()));
                placeholders.put("execution_time", String.valueOf(appliedMigration.getExecutionTime() * 1000L));
                placeholders.put("success", String.valueOf(appliedMigration.isSuccess()));

                String sourceNoPlaceholders = new PlaceholderReplacer(placeholders, "${", "}").replacePlaceholders(source);

                SqlScript sqlScript = new SqlScript(sourceNoPlaceholders, dbSupport);

                sqlScript.execute(jdbcTemplate);
            } else {
            	throw new SQLException("Table creation sql file is missing");
            }

            LOG.debug("MetaData table " + table + " successfully updated to reflect changes");
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to insert row for version '" + version + "' in metadata table " + table, e);
        }

	}

	
	@Override
	public boolean exists() {
		return table.exists();
	}

	
	@Override
	public boolean hasAppliedMigrations() {
		  if (!table.exists()) {
	            return false;
	        }
	        try {
	            int count = jdbcTemplate.queryForInt("MATCH (n : Migration) WHERE NOT n." + "type" + "  IN ['SCHEMA', 'INIT', 'BASELINE'] RETURN COUNT(n)");
	            return count > 0;
	        } catch (SQLException e) {
	            throw new FlywaySqlException("Unable to check whether the metadata table " + table + " has applied migrations", e);
	        }
	}

	
	@Override
	public List<AppliedMigration> allAppliedMigrations() {
		return findAppliedMigrations();
	}

	
	@Override
	public void addBaselineMarker(MigrationVersion initVersion, String initDescription) {
	      addAppliedMigration(new AppliedMigration(initVersion, initDescription, MigrationType.BASELINE, initDescription, null,
	                0, true));

	}

	
	@Override
	public boolean hasBaselineMarker() {
		if (!table.exists()) {
	            return false;
        }
        try {
            int count = jdbcTemplate.queryForInt("MATCH (n : Migration) WHERE n." + "type" + "  IN ['INIT', 'BASELINE']  RETURN COUNT(n)");
            return count > 0;
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to check whether the metadata table " + table + " has an baseline marker migration", e);
        }
	}

	
	@Override
	public AppliedMigration getBaselineMarker() {
		 List<AppliedMigration> appliedMigrations = findAppliedMigrations(MigrationType.BASELINE);
	        return appliedMigrations.isEmpty() ? null : appliedMigrations.get(0);
	}

	
	@Override
	public void removeFailedMigrations() {
		if (!table.exists()) {
            LOG.info("Repair of failed migration in metadata table " + table + " not necessary. No failed migration detected.");
            return;
        }


        try {
            int failedCount = jdbcTemplate.queryForInt("MATCH (n : Migration)"
                    + " WHERE n." + "success" + "=" + dbSupport.getBooleanFalse() + " RETURN COUNT(n)");
            if (failedCount == 0) {
                LOG.info("Repair of failed migration in metadata table " + table + " not necessary. No failed migration detected.");
                return;
            }
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to check the metadata table " + table + " for failed migrations", e);
        }

        try {
            jdbcTemplate.execute("MATCH (n : Migration)<-[r]-() "
                    + " WHERE n." + "success" + " = " + dbSupport.getBooleanFalse() + " DELETE n , r");
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to repair metadata table " + table, e);
        }
	}

	
	@Override
	public void addSchemasMarker(Schema[] schemas) {

	        // Lock again for databases with no DDL transaction to prevent implicit commits from triggering deadlocks
	        // in highly concurrent environments
	        table.lock();

	        addAppliedMigration(new AppliedMigration(null, "<< Flyway Schema Creation >>",
	                MigrationType.SCHEMA, StringUtils.arrayToCommaDelimitedString(schemas), null, 0, true));
	}

	
	@Override
	public boolean hasSchemasMarker() {
		 if (!table.exists()) {
	            return false;
	        }


	        try {
	            int count = jdbcTemplate.queryForInt(
	                    "MATCH (n : Migration) WHERE n.type='SCHEMA' RETURN COUNT(n)" );
	            return count > 0;
	        } catch (SQLException e) {
	            throw new FlywaySqlException("Unable to check whether the metadata table " + table + " has a schema marker migration", e);
	        }
	}

	
	@Override
	public void update(MigrationVersion version, String description, Integer checksum) {
		// TODO Auto-generated method stub
		clearCache();

        LOG.info("Repairing metadata for version " + version + " (Description: " + description + ", Checksum: " + checksum + ")  ...");

        // Try load an update.sql file if it exists
        String resourceName = "org/flywaydb/core/internal/dbsupport/" + dbSupport.getDbName() + "/createMetaDataTable.sql";
        ClassPathResource resource = new ClassPathResource(resourceName, getClass().getClassLoader());
        if (resource.exists()) {
            String source = resource.loadAsString("UTF-8");
            Map<String, String> placeholders = new HashMap<String, String>();

            // Placeholders for column names
            placeholders.put("schema", table.getSchema().getName());
            placeholders.put("table", table.getName());

            // Placeholders for column values
            placeholders.put("version_val", version.toString());
            placeholders.put("description_val", description);
            placeholders.put("checksum_val", String.valueOf(checksum));

            String sourceNoPlaceholders = new PlaceholderReplacer(placeholders, "${", "}").replacePlaceholders(source);

            new SqlScript(sourceNoPlaceholders, dbSupport).execute(jdbcTemplate);
        } else {
            try {
                jdbcTemplate.update("Match (n :Migration)"
                		+ " WHERE n." + "version" + "='" + version + "'"
                        + " SET n.description=" + dbSupport.quote(description) + " , n."
                        + "checksum" + "=" + checksum
                        +" RETURN n");
            } catch (SQLException e) {
                throw new FlywaySqlException("Unable to repair metadata table " + table
                        + " for version " + version, e);
            }
        }

	}

	
	@Override
	public boolean upgradeIfNecessary() {
		 if (table.exists() && table.hasColumn("version_rank")) {
	            new TransactionTemplate(jdbcTemplate.getConnection()).execute(new Callable<Object>() {
	                @Override
	                public Void call() {
	                    lock(new Callable<Object>() {
	                        @Override
	                        public Object call() throws Exception {
	                            LOG.info("Upgrading metadata table " + table + " to the Flyway 4.0 format ...");
	                            //TODO upgradeMetaDataTable
	                            String resourceName = "org/flywaydb/core/internal/dbsupport/" + dbSupport.getDbName() + "/createMetaDataTable.sql";
	                            String source = new ClassPathResource(resourceName, getClass().getClassLoader()).loadAsString("UTF-8");

	                            Map<String, String> placeholders = new HashMap<String, String>();
	                            placeholders.put("schema", table.getSchema().getName());
	                            placeholders.put("table", table.getName());
	                            String sourceNoPlaceholders = new PlaceholderReplacer(placeholders, "${", "}").replacePlaceholders(source);

	                            SqlScript sqlScript = new SqlScript(sourceNoPlaceholders, dbSupport);
	                            sqlScript.execute(jdbcTemplate);
	                            return null;
	                        }
	                    });
	                    return null;
	                }
	            });
	            return true;
	        }
	        return false;
	}

	@Override
	public void clearCache() {
		cache.clear();
	}


    private int calculateInstalledRank() throws SQLException {
        int currentMax = jdbcTemplate.queryForInt("MATCH (n:Migration) Return coalesce(Max(n.installed_rank), 0 )");
        return currentMax + 1;
    }
    
    
    private List<AppliedMigration> findAppliedMigrations(MigrationType... migrationTypes) {
        if (!table.exists()) {
            return new ArrayList<AppliedMigration>();
        }

        int minInstalledRank = cache.isEmpty() ? -1 : cache.getLast().getInstalledRank();

        String query = "MATCH (m:Migration)"
        		+ " WHERE " + "m.installed_rank" + " > " + minInstalledRank
                + " RETURN" + " m.installed_rank "
                + " ,m.version "
                + ",m." + "description"
                + ",m." + "type"
                + ",m." + "script"
                + ",m." + "checksum"
                + ",m." + "installed_on"
                + ",m." + "installed_by"
                + ",m." + "execution_time"
                + ",m." + "success"
                + " ORDER BY " + "m.installed_rank";

        try {
            cache.addAll(jdbcTemplate.query(query, new RowMapper<AppliedMigration>() {
                public AppliedMigration mapRow(final ResultSet rs) throws SQLException {
                    Integer checksum = rs.getInt("m.checksum");
                    if (rs.wasNull()) {
                        checksum = null;
                    }
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
                    Date parsedDate;
					try {
						parsedDate = dateFormat.parse(rs.getString("m.installed_on"));
					} catch (ParseException e) {
						parsedDate = Date.from(Instant.now());
					}
                    Timestamp installedOnTimestamp = new java.sql.Timestamp(parsedDate.getTime());
                    
                    return new AppliedMigration(
                            rs.getInt("m.installed_rank"),
                            rs.getString("m.version") != null ? MigrationVersion.fromVersion(rs.getString("m.version")) : null,
                            rs.getString("m.description"),
                            MigrationType.valueOf(rs.getString("m.type")),
                            rs.getString("m.script"),
                            checksum,
                            installedOnTimestamp,
                            rs.getString("m.installed_by"),
                            rs.getInt("m.execution_time"),
                            rs.getBoolean("m.success")
                    );
                }
            }));
            return cache;
        } catch (SQLException e) {
            throw new FlywaySqlException("Error while retrieving the list of applied migrations from metadata table "
                    + table, e);
        }
    }

}
