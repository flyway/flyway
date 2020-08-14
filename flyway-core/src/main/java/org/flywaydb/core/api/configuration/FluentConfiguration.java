/*
 * Copyright 2010-2020 Redgate Software Ltd
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
package org.flywaydb.core.api.configuration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.ClassProvider;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.internal.util.ClassUtils;

import javax.sql.DataSource;
import java.io.File;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Properties;

/**
 * Fluent configuration for Flyway. This is the preferred means of configuring the Flyway API.
 * <p>
 * This configuration can be passed to Flyway using the <code>new Flyway(Configuration)</code> constructor.
 * </p>
 */
public class FluentConfiguration implements Configuration {
    private final ClassicConfiguration config;

    /**
     * Creates a new default configuration.
     */
    public FluentConfiguration() {
        config = new ClassicConfiguration();
    }

    /**
     * Creates a new default configuration with this class loader.
     *
     * @param classLoader The ClassLoader to use for loading migrations, resolvers, etc from the classpath. (default: Thread.currentThread().getContextClassLoader() )
     */
    public FluentConfiguration(ClassLoader classLoader) {
        config = new ClassicConfiguration(classLoader);
    }

    /**
     * Loads this configuration into a new Flyway instance.
     *
     * @return The new fully-configured Flyway instance.
     */
    public Flyway load() {
        return new Flyway(this);
    }

    /**
     * Configure with the same values as this existing configuration.
     *
     * @param configuration The configuration to use.
     */
    public FluentConfiguration configuration(Configuration configuration) {
        config.configure(configuration);
        return this;
    }

    @Override
    public Location[] getLocations() {
        return config.getLocations();
    }

    @Override
    public Charset getEncoding() {
        return config.getEncoding();
    }

    @Override
    public String getDefaultSchema() { return config.getDefaultSchema(); }

    @Override
    public String[] getSchemas() { return config.getSchemas(); }

    @Override
    public String getTable() {
        return config.getTable();
    }

    @Override
    public String getTablespace() {
        return config.getTablespace();
    }

    @Override
    public MigrationVersion getTarget() {
        return config.getTarget();
    }

    @Override
    public boolean isPlaceholderReplacement() {
        return config.isPlaceholderReplacement();
    }

    @Override
    public Map<String, String> getPlaceholders() {
        return config.getPlaceholders();
    }

    @Override
    public String getPlaceholderPrefix() {
        return config.getPlaceholderPrefix();
    }

    @Override
    public String getPlaceholderSuffix() {
        return config.getPlaceholderSuffix();
    }

    @Override
    public String getSqlMigrationPrefix() {
        return config.getSqlMigrationPrefix();
    }

    @Override
    public String getRepeatableSqlMigrationPrefix() {
        return config.getRepeatableSqlMigrationPrefix();
    }

    @Override
    public String getSqlMigrationSeparator() {
        return config.getSqlMigrationSeparator();
    }

    @Override
    public String[] getSqlMigrationSuffixes() {
        return config.getSqlMigrationSuffixes();
    }

    @Override
    public JavaMigration[] getJavaMigrations() {
        return config.getJavaMigrations();
    }

    @Override
    public boolean isIgnoreMissingMigrations() {
        return config.isIgnoreMissingMigrations();
    }

    @Override
    public boolean isIgnoreIgnoredMigrations() {
        return config.isIgnoreIgnoredMigrations();
    }

    @Override
    public boolean isIgnorePendingMigrations() {
        return config.isIgnorePendingMigrations();
    }

    @Override
    public boolean isIgnoreFutureMigrations() {
        return config.isIgnoreFutureMigrations();
    }

    @Override
    public boolean isValidateMigrationNaming() { return config.isValidateMigrationNaming(); }

    @Override
    public boolean isValidateOnMigrate() {
        return config.isValidateOnMigrate();
    }

    @Override
    public boolean isCleanOnValidationError() {
        return config.isCleanOnValidationError();
    }

    @Override
    public boolean isCleanDisabled() {
        return config.isCleanDisabled();
    }

    @Override
    public MigrationVersion getBaselineVersion() {
        return config.getBaselineVersion();
    }

    @Override
    public String getBaselineDescription() {
        return config.getBaselineDescription();
    }

    @Override
    public boolean isBaselineOnMigrate() {
        return config.isBaselineOnMigrate();
    }

    @Override
    public boolean isOutOfOrder() {
        return config.isOutOfOrder();
    }

    @Override
    public MigrationResolver[] getResolvers() {
        return config.getResolvers();
    }

    @Override
    public boolean isSkipDefaultResolvers() {
        return config.isSkipDefaultResolvers();
    }

    @Override
    public DataSource getDataSource() {
        return config.getDataSource();
    }

    @Override
    public int getConnectRetries() {
        return config.getConnectRetries();
    }

    @Override
    public String getInitSql() {
        return config.getInitSql();
    }

    @Override
    public ClassLoader getClassLoader() {
        return config.getClassLoader();
    }

    @Override
    public boolean isMixed() {
        return config.isMixed();
    }

    @Override
    public String getInstalledBy() {
        return config.getInstalledBy();
    }

    @Override
    public boolean isGroup() {
        return config.isGroup();
    }

    @Override
    public String[] getErrorOverrides() {
        return config.getErrorOverrides();
    }

    @Override
    public OutputStream getDryRunOutput() {
        return config.getDryRunOutput();
    }

    @Override
    public boolean isStream() {
        return config.isStream();
    }

    @Override
    public boolean isBatch() {
        return config.isBatch();
    }

    @Override
    public boolean isOracleSqlplus() {
        return config.isOracleSqlplus();
    }

    @Override
    public boolean isOracleSqlplusWarn() {
        return config.isOracleSqlplusWarn();
    }

    @Override
    public String getLicenseKey() {
        return config.getLicenseKey();
    }

    @Override
    public ResourceProvider getResourceProvider() {
        return config.getResourceProvider();
    }

    @Override
    public ClassProvider<JavaMigration> getJavaMigrationClassProvider() {
        return config.getJavaMigrationClassProvider();
    }

    @Override
    public boolean outputQueryResults() {
        return config.outputQueryResults();
    }

    @Override
    public boolean getCreateSchemas() {
        return config.getCreateSchemas();
    }

    /**
     * Sets the stream where to output the SQL statements of a migration dry run. {@code null} to execute the SQL statements
     * directly against the database. The stream when be closing when Flyway finishes writing the output.
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     *
     * @param dryRunOutput The output file or {@code null} to execute the SQL statements directly against the database.
     */
    public FluentConfiguration dryRunOutput(OutputStream dryRunOutput) {
        config.setDryRunOutput(dryRunOutput);
        return this;
    }

    /**
     * Sets the file where to output the SQL statements of a migration dry run. {@code null} to execute the SQL statements
     * directly against the database. If the file specified is in a non-existent directory, Flyway will create all
     * directories and parent directories as needed.
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     *
     * @param dryRunOutput The output file or {@code null} to execute the SQL statements directly against the database.
     */
    public FluentConfiguration dryRunOutput(File dryRunOutput) {
        config.setDryRunOutputAsFile(dryRunOutput);
        return this;
    }

    /**
     * Sets the file where to output the SQL statements of a migration dry run. {@code null} to execute the SQL statements
     * directly against the database. If the file specified is in a non-existent directory, Flyway will create all
     * directories and parent directories as needed.
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     *
     * @param dryRunOutputFileName The name of the output file or {@code null} to execute the SQL statements directly
     *                             against the database.
     */
    public FluentConfiguration dryRunOutput(String dryRunOutputFileName) {
        config.setDryRunOutputAsFileName(dryRunOutputFileName);
        return this;
    }

    /**
     * Rules for the built-in error handler that let you override specific SQL states and errors codes in order to force
     * specific errors or warnings to be treated as debug messages, info messages, warnings or errors.
     * <p>Each error override has the following format: {@code STATE:12345:W}.
     * It is a 5 character SQL state (or * to match all SQL states), a colon,
     * the SQL error code (or * to match all SQL error codes), a colon and finally
     * the desired behavior that should override the initial one.</p>
     * <p>The following behaviors are accepted:</p>
     * <ul>
     * <li>{@code D} to force a debug message</li>
     * <li>{@code D-} to force a debug message, but do not show the original sql state and error code</li>
     * <li>{@code I} to force an info message</li>
     * <li>{@code I-} to force an info message, but do not show the original sql state and error code</li>
     * <li>{@code W} to force a warning</li>
     * <li>{@code W-} to force a warning, but do not show the original sql state and error code</li>
     * <li>{@code E} to force an error</li>
     * <li>{@code E-} to force an error, but do not show the original sql state and error code</li>
     * </ul>
     * <p>Example 1: to force Oracle stored procedure compilation issues to produce
     * errors instead of warnings, the following errorOverride can be used: {@code 99999:17110:E}</p>
     * <p>Example 2: to force SQL Server PRINT messages to be displayed as info messages (without SQL state and error
     * code details) instead of warnings, the following errorOverride can be used: {@code S0001:0:I-}</p>
     * <p>Example 3: to force all errors with SQL error code 123 to be treated as warnings instead,
     * the following errorOverride can be used: {@code *:123:W}</p>
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     *
     * @param errorOverrides The ErrorOverrides or an empty array if none are defined. (default: none)
     */
    public FluentConfiguration errorOverrides(String... errorOverrides) {
        config.setErrorOverrides(errorOverrides);
        return this;
    }

    /**
     * Whether to group all pending migrations together in the same transaction when applying them (only recommended for databases with support for DDL transactions).
     *
     * @param group {@code true} if migrations should be grouped. {@code false} if they should be applied individually instead. (default: {@code false})
     */
    public FluentConfiguration group(boolean group) {
        config.setGroup(group);
        return this;
    }

    /**
     * The username that will be recorded in the schema history table as having applied the migration.
     *
     * @param installedBy The username or {@code null} for the current database user of the connection. (default: {@code null}).
     */
    public FluentConfiguration installedBy(String installedBy) {
        config.setInstalledBy(installedBy);
        return this;
    }

    /**
     * Whether to allow mixing transactional and non-transactional statements within the same migration. Enabling this
     * automatically causes the entire affected migration to be run without a transaction.
     *
     * <p>Note that this is only applicable for PostgreSQL, Aurora PostgreSQL, SQL Server and SQLite which all have
     * statements that do not run at all within a transaction.</p>
     * <p>This is not to be confused with implicit transaction, as they occur in MySQL or Oracle, where even though a
     * DDL statement was run within a transaction, the database will issue an implicit commit before and after
     * its execution.</p>
     *
     * @param mixed {@code true} if mixed migrations should be allowed. {@code false} if an error should be thrown instead. (default: {@code false})
     */
    public FluentConfiguration mixed(boolean mixed) {
        config.setMixed(mixed);
        return this;
    }

    /**
     * Ignore missing migrations when reading the schema history table. These are migrations that were performed by an
     * older deployment of the application that are no longer available in this version. For example: we have migrations
     * available on the classpath with versions 1.0 and 3.0. The schema history table indicates that a migration with version 2.0
     * (unknown to us) has also been applied. Instead of bombing out (fail fast) with an exception, a
     * warning is logged and Flyway continues normally. This is useful for situations where one must be able to deploy
     * a newer version of the application even though it doesn't contain migrations included with an older one anymore.
     * Note that if the most recently applied migration is removed, Flyway has no way to know it is missing and will
     * mark it as future instead.
     *
     * @param ignoreMissingMigrations {@code true} to continue normally and log a warning, {@code false} to fail fast with an exception.
     *                                (default: {@code false})
     */
    public FluentConfiguration ignoreMissingMigrations(boolean ignoreMissingMigrations) {
        config.setIgnoreMissingMigrations(ignoreMissingMigrations);
        return this;
    }

    /**
     * Ignore ignored migrations when reading the schema history table. These are migrations that were added in between
     * already migrated migrations in this version. For example: we have migrations available on the classpath with
     * versions from 1.0 to 3.0. The schema history table indicates that version 1 was finished on 1.0.15, and the next
     * one was 2.0.0. But with the next release a new migration was added to version 1: 1.0.16. Such scenario is ignored
     * by migrate command, but by default is rejected by validate. When ignoreIgnoredMigrations is enabled, such case
     * will not be reported by validate command. This is useful for situations where one must be able to deliver
     * complete set of migrations in a delivery package for multiple versions of the product, and allows for further
     * development of older versions.
     *
     * @param ignoreIgnoredMigrations {@code true} to continue normally, {@code false} to fail fast with an exception.
     *                                (default: {@code false})
     */
    public FluentConfiguration ignoreIgnoredMigrations(boolean ignoreIgnoredMigrations) {
        config.setIgnoreIgnoredMigrations(ignoreIgnoredMigrations);
        return this;
    }

    /**
     * Ignore pending migrations when reading the schema history table. These are migrations that are available
     * but have not yet been applied. This can be useful for verifying that in-development migration changes
     * don't contain any validation-breaking changes of migrations that have already been applied to a production
     * environment, e.g. as part of a CI/CD process, without failing because of the existence of new migration versions.
     *
     * @param ignorePendingMigrations {@code true} to continue normally, {@code false} to fail fast with an exception.
     *                                (default: {@code false})
     */
    public FluentConfiguration ignorePendingMigrations(boolean ignorePendingMigrations) {
        config.setIgnorePendingMigrations(ignorePendingMigrations);
        return this;
    }

    /**
     * Whether to ignore future migrations when reading the schema history table. These are migrations that were performed by a
     * newer deployment of the application that are not yet available in this version. For example: we have migrations
     * available on the classpath up to version 3.0. The schema history table indicates that a migration to version 4.0
     * (unknown to us) has already been applied. Instead of bombing out (fail fast) with an exception, a
     * warning is logged and Flyway continues normally. This is useful for situations where one must be able to redeploy
     * an older version of the application after the database has been migrated by a newer one.
     *
     * @param ignoreFutureMigrations {@code true} to continue normally and log a warning, {@code false} to fail
     *                               fast with an exception. (default: {@code true})
     */
    public FluentConfiguration ignoreFutureMigrations(boolean ignoreFutureMigrations) {
        config.setIgnoreFutureMigrations(ignoreFutureMigrations);
        return this;
    }

    /**
     * Whether to validate migrations and callbacks whose scripts do not obey the correct naming convention. A failure can be
     * useful to check that errors such as case sensitivity in migration prefixes have been corrected.
     *
     * @param validateMigrationNaming {@code false} to continue normally, {@code true} to fail
     *                                                fast with an exception. (default: {@code false})
     */
    public FluentConfiguration validateMigrationNaming(boolean validateMigrationNaming){
        config.setValidateMigrationNaming(validateMigrationNaming);
        return this;
    }

    /**
     * Whether to automatically call validate or not when running migrate.
     *
     * @param validateOnMigrate {@code true} if validate should be called. {@code false} if not. (default: {@code true})
     */
    public FluentConfiguration validateOnMigrate(boolean validateOnMigrate) {
        config.setValidateOnMigrate(validateOnMigrate);
        return this;
    }

    /**
     * Whether to automatically call clean or not when a validation error occurs.
     * <p> This is exclusively intended as a convenience for development. even though we
     * strongly recommend not to change migration scripts once they have been checked into SCM and run, this provides a
     * way of dealing with this case in a smooth manner. The database will be wiped clean automatically, ensuring that
     * the next migration will bring you back to the state checked into SCM.</p>
     * <p><b>Warning ! Do not enable in production !</b></p>
     *
     * @param cleanOnValidationError {@code true} if clean should be called. {@code false} if not. (default: {@code false})
     */
    public FluentConfiguration cleanOnValidationError(boolean cleanOnValidationError) {
        config.setCleanOnValidationError(cleanOnValidationError);
        return this;
    }

    /**
     * Whether to disable clean.
     * <p>This is especially useful for production environments where running clean can be quite a career limiting move.</p>
     *
     * @param cleanDisabled {@code true} to disable clean. {@code false} to leave it enabled.  (default: {@code false})
     */
    public FluentConfiguration cleanDisabled(boolean cleanDisabled) {
        config.setCleanDisabled(cleanDisabled);
        return this;
    }

    /**
     * Sets the locations to scan recursively for migrations.
     * <p>The location type is determined by its prefix.
     * Unprefixed locations or locations starting with {@code classpath:} point to a package on the classpath and may
     * contain both SQL and Java-based migrations.
     * Locations starting with {@code filesystem:} point to a directory on the filesystem, may only
     * contain SQL migrations and are only scanned recursively down non-hidden directories.</p>
     *
     * @param locations Locations to scan recursively for migrations. (default: db/migration)
     */
    public FluentConfiguration locations(String... locations) {
        config.setLocationsAsStrings(locations);
        return this;
    }

    /**
     * Sets the locations to scan recursively for migrations.
     * <p>The location type is determined by its prefix.
     * Unprefixed locations or locations starting with {@code classpath:} point to a package on the classpath and may
     * contain both SQL and Java-based migrations.
     * Locations starting with {@code filesystem:} point to a directory on the filesystem, may only
     * contain SQL migrations and are only scanned recursively down non-hidden directories.</p>
     *
     * @param locations Locations to scan recursively for migrations. (default: db/migration)
     */
    public FluentConfiguration locations(Location... locations) {
        config.setLocations(locations);
        return this;
    }

    /**
     * Sets the encoding of Sql migrations.
     *
     * @param encoding The encoding of Sql migrations. (default: UTF-8)
     */
    public FluentConfiguration encoding(String encoding) {
        config.setEncodingAsString(encoding);
        return this;
    }

    /**
     * Sets the encoding of Sql migrations.
     *
     * @param encoding The encoding of Sql migrations. (default: UTF-8)
     */
    public FluentConfiguration encoding(Charset encoding) {
        config.setEncoding(encoding);
        return this;
    }

    /**
     * Sets the default schema managed by Flyway. This schema name is case-sensitive. If not specified, but
     * <i>schemas</i> is, Flyway uses the first schema in that list. If that is also not specified, Flyway uses the default
     * schema for the database connection.
     * <p>Consequences:</p>
     * <ul>
     * <li>This schema will be the one containing the schema history table.</li>
     * <li>This schema will be the default for the database connection (provided the database supports this concept).</li>
     * </ul>
     *
     * @param schema The default schema managed by Flyway.
     */
    public FluentConfiguration defaultSchema(String schema) {
        config.setDefaultSchema(schema);
        return this;
    }

    /**
     * Sets the schemas managed by Flyway. These schema names are case-sensitive. If not specified, Flyway uses
     * the default schema for the database connection. If <i>defaultSchemaName</i> is not specified, then the first of
     * this list also acts as default schema.
     * <p>Consequences:</p>
     * <ul>
     * <li>Flyway will automatically attempt to create all these schemas, unless they already exist.</li>
     * <li>The schemas will be cleaned in the order of this list.</li>
     * <li>If Flyway created them, the schemas themselves will be dropped when cleaning.</li>
     * </ul>
     *
     * @param schemas The schemas managed by Flyway. May not be {@code null}. Must contain at least one element.
     */
    public FluentConfiguration schemas(String... schemas) {
        config.setSchemas(schemas);
        return this;
    }

    /**
     * <p>Sets the name of the schema history table that will be used by Flyway.</p><p> By default (single-schema mode)
     * the schema history table is placed in the default schema for the connection provided by the datasource. </p> <p> When
     * the <i>flyway.schemas</i> property is set (multi-schema mode), the schema history table is placed in the first schema
     * of the list. </p>
     *
     * @param table The name of the schema history table that will be used by Flyway. (default: flyway_schema_history)
     */
    public FluentConfiguration table(String table) {
        config.setTable(table);
        return this;
    }

    /**
     * <p>Sets the tablespace where to create the schema history table that will be used by Flyway.</p>
     * <p>If not specified, Flyway uses the default tablespace for the database connection.
     * This setting is only relevant for databases that do support the notion of tablespaces. Its value is simply
     * ignored for all others.</p>
     *
     * @param tablespace The tablespace where to create the schema history table that will be used by Flyway. 
     */
    public FluentConfiguration tablespace(String tablespace) {
        config.setTablespace(tablespace);
        return this;
    }

    /**
     * Sets the target version up to which Flyway should consider migrations.
     * Migrations with a higher version number will be ignored. 
     * Special values:
     * <ul>
     * <li>{@code current}: designates the current version of the schema</li>
     * <li>{@code latest}: the latest version of the schema, as defined by the migration with the highest version</li>
     * </ul>
     * Defaults to {@code latest}.
     */
    public FluentConfiguration target(MigrationVersion target) {
        config.setTarget(target);
        return this;
    }

    /**
     * Sets the target version up to which Flyway should consider migrations.
     * Migrations with a higher version number will be ignored. 
     * Special values:
     * <ul>
     * <li>{@code current}: designates the current version of the schema</li>
     * <li>{@code latest}: the latest version of the schema, as defined by the migration with the highest version</li>
     * </ul>
     * Defaults to {@code latest}.
     */
    public FluentConfiguration target(String target) {
        config.setTargetAsString(target);
        return this;
    }

    /**
     * Sets whether placeholders should be replaced.
     *
     * @param placeholderReplacement Whether placeholders should be replaced. (default: true)
     */
    public FluentConfiguration placeholderReplacement(boolean placeholderReplacement) {
        config.setPlaceholderReplacement(placeholderReplacement);
        return this;
    }

    /**
     * Sets the placeholders to replace in sql migration scripts.
     *
     * @param placeholders The map of &lt;placeholder, replacementValue&gt; to apply to sql migration scripts.
     */
    public FluentConfiguration placeholders(Map<String, String> placeholders) {
        config.setPlaceholders(placeholders);
        return this;
    }

    /**
     * Sets the prefix of every placeholder.
     *
     * @param placeholderPrefix The prefix of every placeholder. (default: ${ )
     */
    public FluentConfiguration placeholderPrefix(String placeholderPrefix) {
        config.setPlaceholderPrefix(placeholderPrefix);
        return this;
    }

    /**
     * Sets the suffix of every placeholder.
     *
     * @param placeholderSuffix The suffix of every placeholder. (default: } )
     */
    public FluentConfiguration placeholderSuffix(String placeholderSuffix) {
        config.setPlaceholderSuffix(placeholderSuffix);
        return this;
    }

    /**
     * Sets the file name prefix for sql migrations.
     * <p>Sql migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to V1_1__My_description.sql</p>
     *
     * @param sqlMigrationPrefix The file name prefix for sql migrations (default: V)
     */
    public FluentConfiguration sqlMigrationPrefix(String sqlMigrationPrefix) {
        config.setSqlMigrationPrefix(sqlMigrationPrefix);
        return this;
    }

    @Override
    public String getUndoSqlMigrationPrefix() {
        return config.getUndoSqlMigrationPrefix();
    }

    /**
     * Sets the file name prefix for undo SQL migrations. (default: U)
     * <p>Undo SQL migrations are responsible for undoing the effects of the versioned migration with the same version.</p>
     * <p>They have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to U1.1__My_description.sql</p>
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     *
     * @param undoSqlMigrationPrefix The file name prefix for undo SQL migrations. (default: U)
     */
    public FluentConfiguration undoSqlMigrationPrefix(String undoSqlMigrationPrefix) {
        config.setUndoSqlMigrationPrefix(undoSqlMigrationPrefix);
        return this;
    }

    /**
     * Sets the file name prefix for repeatable sql migrations.
     * <p>Repeatable sql migrations have the following file name structure: prefixSeparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to R__My_description.sql</p>
     *
     * @param repeatableSqlMigrationPrefix The file name prefix for repeatable sql migrations (default: R)
     */
    public FluentConfiguration repeatableSqlMigrationPrefix(String repeatableSqlMigrationPrefix) {
        config.setRepeatableSqlMigrationPrefix(repeatableSqlMigrationPrefix);
        return this;
    }

    /**
     * Sets the file name separator for sql migrations.
     * <p>Sql migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to V1_1__My_description.sql</p>
     *
     * @param sqlMigrationSeparator The file name separator for sql migrations (default: __)
     */
    public FluentConfiguration sqlMigrationSeparator(String sqlMigrationSeparator) {
        config.setSqlMigrationSeparator(sqlMigrationSeparator);
        return this;
    }

    /**
     * The file name suffixes for SQL migrations. (default: .sql)
     * <p>SQL migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to V1_1__My_description.sql</p>
     * <p>Multiple suffixes (like .sql,.pkg,.pkb) can be specified for easier compatibility with other tools such as
     * editors with specific file associations.</p>
     *
     * @param sqlMigrationSuffixes The file name suffixes for SQL migrations.
     */
    public FluentConfiguration sqlMigrationSuffixes(String... sqlMigrationSuffixes) {
        config.setSqlMigrationSuffixes(sqlMigrationSuffixes);
        return this;
    }

    /**
     * The manually added Java-based migrations. These are not Java-based migrations discovered through classpath
     * scanning and instantiated by Flyway. Instead these are manually added instances of JavaMigration.
     * This is particularly useful when working with a dependency injection container, where you may want the DI
     * container to instantiate the class and wire up its dependencies for you.
     *
     * @param javaMigrations The manually added Java-based migrations. An empty array if none. (default: none)
     */
    public FluentConfiguration javaMigrations(JavaMigration... javaMigrations) {
        config.setJavaMigrations(javaMigrations);
        return this;
    }

    /**
     * Sets the datasource to use. Must have the necessary privileges to execute ddl.
     *
     * @param dataSource The datasource to use. Must have the necessary privileges to execute ddl.
     */
    public FluentConfiguration dataSource(DataSource dataSource) {
        config.setDataSource(dataSource);
        return this;
    }

    /**
     * Sets the datasource to use. Must have the necessary privileges to execute ddl.
     *
     * @param url      The JDBC URL of the database.
     * @param user     The user of the database.
     * @param password The password of the database.
     */
    public FluentConfiguration dataSource(String url, String user, String password) {
        config.setDataSource(url, user, password);
        return this;
    }

    /**
     * The maximum number of retries when attempting to connect to the database. After each failed attempt, Flyway will
     * wait 1 second before attempting to connect again, up to the maximum number of times specified by connectRetries.
     *
     * @param connectRetries The maximum number of retries (default: 0).
     */
    public FluentConfiguration connectRetries(int connectRetries) {
        config.setConnectRetries(connectRetries);
        return this;
    }

    /**
     * The SQL statements to run to initialize a new database connection immediately after opening it.
     *
     * @param initSql The SQL statements. (default: {@code null})
     */
    public FluentConfiguration initSql(String initSql) {
        config.setInitSql(initSql);
        return this;
    }

    /**
     * Sets the version to tag an existing schema with when executing baseline.
     *
     * @param baselineVersion The version to tag an existing schema with when executing baseline. (default: 1)
     */
    public FluentConfiguration baselineVersion(MigrationVersion baselineVersion) {
        config.setBaselineVersion(baselineVersion);
        return this;
    }

    /**
     * Sets the version to tag an existing schema with when executing baseline.
     *
     * @param baselineVersion The version to tag an existing schema with when executing baseline. (default: 1)
     */
    public FluentConfiguration baselineVersion(String baselineVersion) {
        config.setBaselineVersion(MigrationVersion.fromVersion(baselineVersion));
        return this;
    }

    /**
     * Sets the description to tag an existing schema with when executing baseline.
     *
     * @param baselineDescription The description to tag an existing schema with when executing baseline. (default: &lt;&lt; Flyway Baseline &gt;&gt;)
     */
    public FluentConfiguration baselineDescription(String baselineDescription) {
        config.setBaselineDescription(baselineDescription);
        return this;
    }

    /**
     * <p>
     * Whether to automatically call baseline when migrate is executed against a non-empty schema with no schema history table.
     * This schema will then be baselined with the {@code baselineVersion} before executing the migrations.
     * Only migrations above {@code baselineVersion} will then be applied.
     * </p>
     * <p>
     * This is useful for initial Flyway production deployments on projects with an existing DB.
     * </p>
     * <p>
     * Be careful when enabling this as it removes the safety net that ensures
     * Flyway does not migrate the wrong database in case of a configuration mistake!
     * </p>
     *
     * @param baselineOnMigrate {@code true} if baseline should be called on migrate for non-empty schemas, {@code false} if not. (default: {@code false})
     */
    public FluentConfiguration baselineOnMigrate(boolean baselineOnMigrate) {
        config.setBaselineOnMigrate(baselineOnMigrate);
        return this;
    }

    /**
     * Allows migrations to be run "out of order".
     * <p>If you already have versions 1 and 3 applied, and now a version 2 is found,
     * it will be applied too instead of being ignored.</p>
     *
     * @param outOfOrder {@code true} if outOfOrder migrations should be applied, {@code false} if not. (default: {@code false})
     */
    public FluentConfiguration outOfOrder(boolean outOfOrder) {
        config.setOutOfOrder(outOfOrder);
        return this;
    }

    /**
     * Gets the callbacks for lifecycle notifications.
     *
     * @return The callbacks for lifecycle notifications. An empty array if none. (default: none)
     */
    @Override
    public Callback[] getCallbacks() {
        return config.getCallbacks();
    }

    @Override
    public boolean isSkipDefaultCallbacks() {
        return config.isSkipDefaultCallbacks();
    }

    /**
     * Set the callbacks for lifecycle notifications.
     *
     * @param callbacks The callbacks for lifecycle notifications. (default: none)
     */
    public FluentConfiguration callbacks(Callback... callbacks) {
        config.setCallbacks(callbacks);
        return this;
    }

    /**
     * Set the callbacks for lifecycle notifications.
     *
     * @param callbacks The fully qualified class names of the callbacks for lifecycle notifications. (default: none)
     */
    public FluentConfiguration callbacks(String... callbacks) {
        config.setCallbacksAsClassNames(callbacks);
        return this;
    }

    /**
     * Whether Flyway should skip the default callbacks. If true, only custom callbacks are used.
     *
     * @param skipDefaultCallbacks Whether default built-in callbacks should be skipped. <p>(default: false)</p>
     */
    public FluentConfiguration skipDefaultCallbacks(boolean skipDefaultCallbacks) {
        config.setSkipDefaultCallbacks(skipDefaultCallbacks);
        return this;
    }

    /**
     * Sets custom MigrationResolvers to be used in addition to the built-in ones for resolving Migrations to apply.
     *
     * @param resolvers The custom MigrationResolvers to be used in addition to the built-in ones for resolving Migrations to apply. (default: empty list)
     */
    public FluentConfiguration resolvers(MigrationResolver... resolvers) {
        config.setResolvers(resolvers);
        return this;
    }

    /**
     * Sets custom MigrationResolvers to be used in addition to the built-in ones for resolving Migrations to apply.
     *
     * @param resolvers The fully qualified class names of the custom MigrationResolvers to be used in addition to the built-in ones for resolving Migrations to apply. (default: empty list)
     */
    public FluentConfiguration resolvers(String... resolvers) {
        config.setResolversAsClassNames(resolvers);
        return this;
    }

    /**
     * Whether Flyway should skip the default resolvers. If true, only custom resolvers are used.
     *
     * @param skipDefaultResolvers Whether default built-in resolvers should be skipped. <p>(default: false)</p>
     */
    public FluentConfiguration skipDefaultResolvers(boolean skipDefaultResolvers) {
        config.setSkipDefaultResolvers(skipDefaultResolvers);
        return this;
    }

    /**
     * Whether to stream SQL migrations when executing them. Streaming doesn't load the entire migration in memory at
     * once. Instead each statement is loaded individually. This is particularly useful for very large SQL migrations
     * composed of multiple MB or even GB of reference data, as this dramatically reduces Flyway's memory consumption.
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     *
     * @param stream {@code true} to stream SQL migrations. {@code false} to fully loaded them in memory instead. (default: {@code false})
     */
    public FluentConfiguration stream(boolean stream) {
        config.setStream(stream);
        return this;
    }

    /**
     * Whether to batch SQL statements when executing them. Batching can save up to 99 percent of network roundtrips by
     * sending up to 100 statements at once over the network to the database, instead of sending each statement
     * individually. This is particularly useful for very large SQL migrations composed of multiple MB or even GB of
     * reference data, as this can dramatically reduce the network overhead. This is supported for INSERT, UPDATE,
     * DELETE, MERGE and UPSERT statements. All other statements are automatically executed without batching.
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     *
     * @param batch {@code true} to batch SQL statements. {@code false} to execute them individually instead. (default: {@code false})
     */
    public FluentConfiguration batch(boolean batch) {
        config.setBatch(batch);
        return this;
    }

    /**
     * Whether to Flyway's support for Oracle SQL*Plus commands should be activated.
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     *
     * @param oracleSqlplus {@code true} to active SQL*Plus support. {@code false} to fail fast instead. (default: {@code false})
     */
    public FluentConfiguration oracleSqlplus(boolean oracleSqlplus) {
        config.setOracleSqlplus(oracleSqlplus);
        return this;
    }

    /**
     * Whether Flyway should issue a warning instead of an error whenever it encounters an Oracle SQL*Plus statement
     * it doesn't yet support.
     *
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     *
     * @param oracleSqlplusWarn {@code true} to issue a warning. {@code false} to fail fast instead. (default: {@code false})
     */
    public FluentConfiguration oracleSqlplusWarn(boolean oracleSqlplusWarn) {
        config.setOracleSqlplusWarn(oracleSqlplusWarn);
        return this;
    }

    /**
     * Your Flyway license key (FL01...). Not yet a Flyway Pro or Enterprise Edition customer?
     * Request your <a href="https://flywaydb.org/download/">Flyway trial license key</a>
     * to try out Flyway Pro and Enterprise Edition features free for 30 days.
     *
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     *
     * @param licenseKey Your Flyway license key.
     */
    public FluentConfiguration licenseKey(String licenseKey) {
        config.setLicenseKey(licenseKey);
        return this;
    }

    /**
     * Custom ResourceProvider to be used to look up resources. If not set, the default strategy will be used.
     *
     * @param resourceProvider Custom ResourceProvider to be used to look up resources
     */
    public FluentConfiguration resourceProvider(ResourceProvider resourceProvider) {
        config.setResourceProvider(resourceProvider);
        return this;
    }

    /**
     * Custom ClassProvider to be used to look up {@link JavaMigration} classes. If not set, the default strategy will be used.
     *
     * @param javaMigrationClassProvider Custom ClassProvider to be used to look up {@link JavaMigration} classes.
     */
    public FluentConfiguration javaMigrationClassProvider(ClassProvider<JavaMigration> javaMigrationClassProvider) {
        config.setJavaMigrationClassProvider(javaMigrationClassProvider);
        return this;
    }

    /**
     * Configures Flyway with these properties. This overwrites any existing configuration. Property names are
     * documented in the flyway maven plugin.
     * <p>To use a custom ClassLoader, setClassLoader() must be called prior to calling this method.</p>
     *
     * @param properties Properties used for configuration.
     * @throws FlywayException when the configuration failed.
     */
    public FluentConfiguration configuration(Properties properties) {
        config.configure(properties);
        return this;
    }

    /**
     * Configures Flyway with these properties. This overwrites any existing configuration. Property names are
     * documented in the flyway maven plugin.
     * <p>To use a custom ClassLoader, it must be passed to the Flyway constructor prior to calling this method.</p>
     *
     * @param props Properties used for configuration.
     * @throws FlywayException when the configuration failed.
     */
    public FluentConfiguration configuration(Map<String, String> props) {
        config.configure(props);
        return this;
    }

    /**
     * Load configuration files from the default locations:
     * $installationDir$/conf/flyway.conf
     * $user.home$/flyway.conf
     * $workingDirectory$/flyway.conf
     *
     * The configuration files must be encoded with UTF-8.
     *
     * @throws FlywayException when the configuration failed.
     */
    public FluentConfiguration loadDefaultConfigurationFiles() {
        return loadDefaultConfigurationFiles("UTF-8");
    }

    /**
     * Load configuration files from the default locations:
     * $installationDir$/conf/flyway.conf
     * $user.home$/flyway.conf
     * $workingDirectory$/flyway.conf
     *
     * @param encoding the conf file encoding.
     * @throws FlywayException when the configuration failed.
     */
    public FluentConfiguration loadDefaultConfigurationFiles(String encoding) {
        String installationPath = ClassUtils.getLocationOnDisk(FluentConfiguration.class);
        File installationDir = new File(installationPath).getParentFile();

        Map<String, String> configMap = ConfigUtils.loadDefaultConfigurationFiles(installationDir, encoding);

        config.configure(configMap);
        return this;
    }

    /**
     * Whether Flyway should attempt to create the schemas specified in the schemas property
     *
     * @param createSchemas @{code true} to attempt to create the schemas (default: {@code true})
     */
    public FluentConfiguration createSchemas(boolean createSchemas) {
        config.setShouldCreateSchemas(createSchemas);
        return this;
    }

    /**
     * Configures Flyway using FLYWAY_* environment variables.
     *
     * @throws FlywayException when the configuration failed.
     */
    public FluentConfiguration envVars() {
        config.configureUsingEnvVars();
        return this;
    }
}