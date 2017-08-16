package org.flywaydb.core.internal.dbsupport.neo4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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

	public Neo4JMetaDataTable(DbSupport dbSupport, Table table) {
	        this.jdbcTemplate = dbSupport.getJdbcTemplate();
	        this.dbSupport = dbSupport;
	        this.table = table;
	        if (installedBy == null) {
	            this.installedBy = dbSupport.getCurrentUserFunction();
	        } else {
	            this.installedBy = "'" + installedBy + "'";
	        }
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
            String versionStr = version == null ? null : version.toString();

            // Try load an updateMetaDataTable.sql file if it exists
            String resourceName = "org/flywaydb/core/internal/dbsupport/" + dbSupport.getDbName() + "/createMetaDataTable.sql";
            ClassPathResource classPathResource = new ClassPathResource(resourceName, getClass().getClassLoader());
            int installedRank = appliedMigration.getType() == MigrationType.SCHEMA ? 0 : calculateInstalledRank();
            if (classPathResource.exists()) {
                String source = classPathResource.loadAsString("UTF-8");
                Map<String, String> placeholders = new HashMap<String, String>();

                // Placeholders for schema and table
                placeholders.put("schema", table.getSchema().getName());
                placeholders.put("table", table.getName());

                // Placeholders for column values
                placeholders.put("installed_rank_val", String.valueOf(installedRank));
                placeholders.put("version_val", versionStr);
                placeholders.put("description_val", appliedMigration.getDescription());
                placeholders.put("type_val", appliedMigration.getType().name());
                placeholders.put("script_val", appliedMigration.getScript());
                placeholders.put("checksum_val", String.valueOf(appliedMigration.getChecksum()));
                placeholders.put("installed_by_val", installedBy);
                placeholders.put("execution_time_val", String.valueOf(appliedMigration.getExecutionTime() * 1000L));
                placeholders.put("success_val", String.valueOf(appliedMigration.isSuccess()));

                String sourceNoPlaceholders = new PlaceholderReplacer(placeholders, "${", "}").replacePlaceholders(source);

                SqlScript sqlScript = new SqlScript(sourceNoPlaceholders, dbSupport);

                sqlScript.execute(jdbcTemplate);
            } else {
                // Fall back to hard-coded statements
                jdbcTemplate.update("INSERT INTO " + table
                                + " (" + dbSupport.quote("installed_rank")
                                + "," + dbSupport.quote("version")
                                + "," + dbSupport.quote("description")
                                + "," + dbSupport.quote("type")
                                + "," + dbSupport.quote("script")
                                + "," + dbSupport.quote("checksum")
                                + "," + dbSupport.quote("installed_by")
                                + "," + dbSupport.quote("execution_time")
                                + "," + dbSupport.quote("success")
                                + ")"
                                + " VALUES (?, ?, ?, ?, ?, ?, " + installedBy + ", ?, ?)",
                        installedRank,
                        versionStr,
                        appliedMigration.getDescription(),
                        appliedMigration.getType().name(),
                        appliedMigration.getScript(),
                        appliedMigration.getChecksum(),
                        appliedMigration.getExecutionTime(),
                        appliedMigration.isSuccess()
                );
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
	            int count = jdbcTemplate.queryForInt("MATCH (n : Migration) WHERE NOT n." + dbSupport.quote("type") + "  IN ['SCHEMA', 'INIT', 'BASELINE'] RETURN COUNT(n)");
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
	            int count = jdbcTemplate.queryForInt("MATCH (n : Migration) WHERE n." + dbSupport.quote("type") + "  IN ['INIT', 'BASELINE']  RETURN COUNT(n)");
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
                    + " WHERE n." + dbSupport.quote("success") + "=" + dbSupport.getBooleanFalse() + "COUNT(n)");
            if (failedCount == 0) {
                LOG.info("Repair of failed migration in metadata table " + table + " not necessary. No failed migration detected.");
                return;
            }
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to check the metadata table " + table + " for failed migrations", e);
        }

        try {
            jdbcTemplate.execute("MATCH (n : Migration)<-[r]-() "
                    + " WHERE n." + dbSupport.quote("success") + " = " + dbSupport.getBooleanFalse() + "DELETE n , r");
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
	                    "MATCH (n : Migration) WHERE n." + dbSupport.quote("type") + "='SCHEMA' COUNT(n)" );
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
        String resourceName = "org/flywaydb/core/internal/dbsupport/" + dbSupport.getDbName() + "/update.sql";
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
                        + " SET n." + dbSupport.quote("description") + "='" + description + "' , n."
                        + dbSupport.quote("checksum") + "=" + checksum
                        + " WHERE n." + dbSupport.quote("version") + "='" + version + "'");
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
        int currentMax = jdbcTemplate.queryForInt("MATCH (n:Migration) RETURN MAX(n." + dbSupport.quote("installed_rank") + ")");
        return currentMax + 1;
    }
    
    
    private List<AppliedMigration> findAppliedMigrations(MigrationType... migrationTypes) {
        if (!table.exists()) {
            return new ArrayList<AppliedMigration>();
        }

        int minInstalledRank = cache.isEmpty() ? -1 : cache.getLast().getInstalledRank();

        String query = "SELECT " + dbSupport.quote("installed_rank")
                + "," + dbSupport.quote("version")
                + "," + dbSupport.quote("description")
                + "," + dbSupport.quote("type")
                + "," + dbSupport.quote("script")
                + "," + dbSupport.quote("checksum")
                + "," + dbSupport.quote("installed_on")
                + "," + dbSupport.quote("installed_by")
                + "," + dbSupport.quote("execution_time")
                + "," + dbSupport.quote("success")
                + " FROM " + table
                + " WHERE " + dbSupport.quote("installed_rank") + " > " + minInstalledRank;

        if (migrationTypes.length > 0) {
            query += " AND " + dbSupport.quote("type") + " IN (";
            for (int i = 0; i < migrationTypes.length; i++) {
                if (i > 0) {
                    query += ",";
                }
                query += "'" + migrationTypes[i] + "'";
            }
            query += ")";
        }

        query += " ORDER BY " + dbSupport.quote("installed_rank");

        try {
            cache.addAll(jdbcTemplate.query(query, new RowMapper<AppliedMigration>() {
                public AppliedMigration mapRow(final ResultSet rs) throws SQLException {
                    Integer checksum = rs.getInt("checksum");
                    if (rs.wasNull()) {
                        checksum = null;
                    }

                    return new AppliedMigration(
                            rs.getInt("installed_rank"),
                            rs.getString("version") != null ? MigrationVersion.fromVersion(rs.getString("version")) : null,
                            rs.getString("description"),
                            MigrationType.valueOf(rs.getString("type")),
                            rs.getString("script"),
                            checksum,
                            rs.getTimestamp("installed_on"),
                            rs.getString("installed_by"),
                            rs.getInt("execution_time"),
                            rs.getBoolean("success")
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
