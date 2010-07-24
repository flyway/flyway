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

package com.googlecode.flyway.core;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.dbsupport.DbSupportFactory;
import com.googlecode.flyway.core.migration.Migration;
import com.googlecode.flyway.core.migration.MigrationResolver;
import com.googlecode.flyway.core.migration.SchemaVersion;
import com.googlecode.flyway.core.migration.java.JavaMigrationResolver;
import com.googlecode.flyway.core.migration.sql.PlaceholderReplacer;
import com.googlecode.flyway.core.migration.sql.SqlMigrationResolver;
import com.googlecode.flyway.core.runtime.DbCleaner;
import com.googlecode.flyway.core.runtime.DbMigrator;
import com.googlecode.flyway.core.runtime.MetaDataTable;
import com.googlecode.flyway.core.util.StringUtils;
import com.googlecode.flyway.core.util.TimeFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central service locator.
 */
public class Flyway {
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(Flyway.class);

    /**
     * The base package where the Java migrations are located. (default:
     * db.migration)
     */
    private String basePackage = "db.migration";

    /**
     * The base directory on the classpath where the Sql migrations are located.
     * (default: sql/location)
     */
    private String baseDir = "db/migration";

    /**
     * The encoding of Sql migrations.
     * (default: UTF-8)
     */
    private String encoding = "UTF-8";

    /**
     * The name of the schema metadata table that will be used by flyway.
     * (default: schema_version)
     */
    private String schemaMetaDataTable = "schema_version";

    /**
     * A map of <placeholder, replacementValue> to apply to sql migration
     * scripts.
     */
    private Map<String, String> placeholders = new HashMap<String, String>();

    /**
     * The prefix of every placeholder. (default: ${ )
     */
    private String placeholderPrefix = "${";

    /**
     * The suffix of every placeholder. (default: } )
     */
    private String placeholderSuffix = "}";

    /**
     * @param basePackage The base package where the migrations are located. (default:
     *                    db.migration)
     */
    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    /**
     * @param baseDir The base directory on the classpath where the Sql migrations
     *                are located. (default: sql/location)
     */
    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    /**
     * @param encoding The encoding of Sql migrations.
     *                 (default: UTF-8)
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * @param schemaMetaDataTable The name of the schema metadata table that will be used by
     *                            flyway. (default: schema_maintenance_history)
     */
    public void setSchemaMetaDataTable(String schemaMetaDataTable) {
        this.schemaMetaDataTable = schemaMetaDataTable;
    }

    /**
     * @param placeholders A map of <placeholder, replacementValue> to apply to sql
     *                     migration scripts.
     */
    public void setPlaceholders(Map<String, String> placeholders) {
        this.placeholders = placeholders;
    }

    /**
     * @param placeholderPrefix The prefix of every placeholder. (default: ${ )
     */
    public void setPlaceholderPrefix(String placeholderPrefix) {
        this.placeholderPrefix = placeholderPrefix;
    }

    /**
     * @param placeholderSuffix The suffix of every placeholder. (default: } )
     */
    public void setPlaceholderSuffix(String placeholderSuffix) {
        this.placeholderSuffix = placeholderSuffix;
    }

    /**
     * JdbcTemplate with ddl manipulation access to the database.
     */
    private JdbcTemplate jdbcTemplate;

    /**
     * The transaction template to use.
     */
    private TransactionTemplate transactionTemplate;

    /**
     * Database-specific functionality.
     */
    private DbSupport dbSupport;

    /**
     * Supports reading and writing to the metadata table.
     */
    private MetaDataTable metaDataTable;

    /**
     * @return Supports reading and writing to the metadata table.
     */
    public MetaDataTable getMetaDataTable() {
        return metaDataTable;
    }

    /**
     * prefix for sql migrations (default: V)
     */
    private String sqlMigrationPrefix = "V";

    /**
     * suffix for sql migrations (default: .sql)
     */
    private String sqlMigrationSuffix = ".sql";


    /**
     * @param sqlMigrationPrefix prefix for sql migrations (default: V)
     */
    public void setSqlMigrationPrefix(String sqlMigrationPrefix) {
        this.sqlMigrationPrefix = sqlMigrationPrefix;
    }

    /**
     * @param sqlMigrationSuffix suffix for sql migrations (default: .sql)
     */
    public void setSqlMigrationSuffix(String sqlMigrationSuffix) {
        this.sqlMigrationSuffix = sqlMigrationSuffix;
    }

    /**
     * @param dataSource The datasource to use. Must have the necessary privileges to
     *                   execute ddl.
     */
    public void setDataSource(DataSource dataSource) {
        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        transactionTemplate = new TransactionTemplate(transactionManager);
        jdbcTemplate = new JdbcTemplate(dataSource);

        dbSupport = DbSupportFactory.createDbSupport(jdbcTemplate);
        LOG.debug("Schema: " + dbSupport.getCurrentSchema(jdbcTemplate));

        metaDataTable = new MetaDataTable(transactionTemplate, jdbcTemplate, dbSupport, schemaMetaDataTable);
    }

    /**
     * Starts the database migration.
     *
     * @return The number of successfully applied migrations.
     * @throws Exception Thrown when the migration failed.
     */
    public int migrate() throws Exception {
        metaDataTable.createIfNotExists();

        PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer(placeholders, placeholderPrefix, placeholderSuffix);

        Collection<MigrationResolver> migrationResolvers = new ArrayList<MigrationResolver>();
        migrationResolvers.add(new SqlMigrationResolver(baseDir, placeholderReplacer, encoding, sqlMigrationPrefix, sqlMigrationSuffix));
        migrationResolvers.add(new JavaMigrationResolver(basePackage));

        DbMigrator dbMigrator = new DbMigrator(transactionTemplate, jdbcTemplate, dbSupport, migrationResolvers,
                metaDataTable);
        return dbMigrator.migrate();
    }

    /**
     * Drops all object in the schema.
     */
    public void clean() {
        DbCleaner dbCleaner = new DbCleaner(transactionTemplate, jdbcTemplate, dbSupport);
        dbCleaner.clean();
    }

    /**
     * Returns the status (current version) of the database.
     *
     * @return The latest applied migration, or {@code null} if no migration has been applied yet.
     */
    public Migration status() {
        return getMetaDataTable().latestAppliedMigration();
    }

    /**
     * Returns the history (all applied migrations) of the database.
     *
     * @return All migrations applied to the database, sorted, oldest first. An empty list if none.
     */
    public List<Migration> history() {
        return getMetaDataTable().allAppliedMigrations();
    }

    /**
     * Creates and initializes the Flyway metadata table.
     *
     * @param initialVersion (Optional) The initial version to put in the metadata table. Only migrations with a version number
     *                       higher than this one will be considered for this database.
     */
    public void init(SchemaVersion initialVersion) {
        metaDataTable.createIfNotExists();
        metaDataTable.init(initialVersion);
    }
}
