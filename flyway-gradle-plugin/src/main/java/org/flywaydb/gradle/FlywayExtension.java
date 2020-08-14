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
package org.flywaydb.gradle;

import java.util.Map;

/**
 * The flyway's configuration properties.
 * <p>More info: <a href="https://flywaydb.org/documentation/gradle">https://flywaydb.org/documentation/gradle</a></p>
 */
public class FlywayExtension {
    /**
     * The fully qualified classname of the jdbc driver to use to connect to the database
     */
    public String driver;

    /**
     * The jdbc url to use to connect to the database
     */
    public String url;

    /**
     * The user to use to connect to the database
     */
    public String user;

    /**
     * The password to use to connect to the database
     */
    public String password;

    /**
     * The maximum number of retries when attempting to connect to the database. After each failed attempt, Flyway will
     * wait 1 second before attempting to connect again, up to the maximum number of times specified by connectRetries.
     * (default: 0)
     * <p>Also configurable with Gradle or System Property: ${flyway.connectRetries}</p>
     */
    public int connectRetries;

    /**
     * The SQL statements to run to initialize a new database connection immediately after opening it.
     * (default: {@code null})
     * <p>Also configurable with Gradle or System Property: ${flyway.initSql}</p>
     */
    public String initSql;

    /**
     * <p>The name of the schema history table that will be used by Flyway. (default: flyway_schema_history)</p><p> By default
     * (single-schema mode) the schema history table is placed in the default schema for the connection provided by the
     * datasource. </p> <p> When the <i>flyway.schemas</i> property is set (multi-schema mode), the schema history table is
     * placed in the first schema of the list. </p>
     * <p>Also configurable with Gradle or System Property: ${flyway.table}</p>
     */
    public String table;

    /**
     * <p>The tablespace where to create the schema history table that will be used by Flyway.</p>
     * <p>If not specified, Flyway uses the default tablespace for the database connection.
     * This setting is only relevant for databases that do support the notion of tablespaces. Its value is simply
     * ignored for all others.</p>
     * <p>Also configurable with Gradle or System Property: ${flyway.tablespace}</p>
     */
    public String tablespace;

    /**
     * The default schema managed by Flyway. This schema name is case-sensitive. If not specified, but
     * <i>schemas</i> is, Flyway uses the first schema in that list. If that is also not specified, Flyway uses the
     * default schema for the database connection.
     * <p>Consequences:</p>
     * <ul>
     * <li>This schema will be the one containing the schema history table.</li>
     * <li>This schema will be the default for the database connection (provided the database supports this concept).</li>
     * </ul>
     * <p>Also configurable with Maven or System Property: ${flyway.defaultSchema}</p>
     */
    public String defaultSchema;

    /**
     * The schemas managed by Flyway. These schema names are case-sensitive. If not specified, Flyway uses
     * the default schema for the database connection. If <i>defaultSchema</i> is not specified, then the first of
     * this list also acts as default schema.
     * <p>Consequences:</p>
     * <ul>
     * <li>Flyway will automatically attempt to create all these schemas, unless they already exist.</li>
     * <li>The schemas will be cleaned in the order of this list.</li>
     * <li>If Flyway created them, the schemas themselves will be dropped when cleaning.</li>
     * </ul>
     * <p>Also configurable with Maven or System Property: ${flyway.schemas} (comma-separated list)</p>
     */
    public String[] schemas;

    /**
     * The version to tag an existing schema with when executing baseline. (default: 1)
     */
    public String baselineVersion;

    /**
     * The description to tag an existing schema with when executing baseline. (default: &lt;&lt; Flyway Baseline &gt;&gt;)
     */
    public String baselineDescription;

    /**
     * Locations to scan recursively for migrations.
     * <p>The location type is determined by its prefix.
     * Unprefixed locations or locations starting with {@code classpath:} point to a package on the classpath and may
     * contain both SQL and Java-based migrations.
     * Locations starting with {@code filesystem:} point to a directory on the filesystem, may only
     * contain SQL migrations and are only scanned recursively down non-hidden directories.</p>
     * (default: filesystem:src/main/resources/db/migration)
     */
    public String[] locations;

    /**
     * The fully qualified class names of the custom MigrationResolvers to be used in addition (default)
     * or as a replacement (using skipDefaultResolvers) to the built-in ones for resolving Migrations to
     * apply.
     * <p>(default: none)</p>
     */
    public String[] resolvers;

    /**
     * If set to true, default built-in resolvers will be skipped, only custom migration resolvers will be used.
     * <p>(default: false)</p>
     */
    public Boolean skipDefaultResolvers;

    /**
     * The file name prefix for versioned SQL migrations. (default: V)
     * <p>Versioned SQL migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to V1_1__My_description.sql</p>
     * <p>Also configurable with Gradle or System Property: ${flyway.sqlMigrationPrefix}</p>
     */
    public String sqlMigrationPrefix;

    /**
     * The file name prefix for undo SQL migrations. (default: U)
     * <p>Undo SQL migrations are responsible for undoing the effects of the versioned migration with the same version.</p>
     * <p>They have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to U1.1__My_description.sql</p>
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     * <p>Also configurable with Gradle or System Property: ${flyway.undoSqlMigrationPrefix}</p>
     */
    public String undoSqlMigrationPrefix;

    /**
     * The file name prefix for repeatable SQL migrations (default: R).
     * <p>Repeatable SQL migrations have the following file name structure: prefixSeparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to R__My_description.sql</p>
     * <p>Also configurable with Gradle or System Property: ${flyway.repeatableSqlMigrationPrefix}</p>
     */
    public String repeatableSqlMigrationPrefix;

    /**
     * The file name prefix for Sql migrations
     * <p>Sql migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to V1_1__My_description.sql</p>
     */
    public String sqlMigrationSeparator;

    /**
     * The file name suffixes for SQL migrations. (default: .sql)
     * <p>SQL migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to V1_1__My_description.sql</p>
     * <p>Multiple suffixes (like .sql,.pkg,.pkb) can be specified for easier compatibility with other tools such as
     * editors with specific file associations.</p>
     * <p>Also configurable with Gradle or System Property: ${flyway.sqlMigrationSuffixes}</p>
     */
    public String[] sqlMigrationSuffixes;

    /**
     * The encoding of Sql migrations
     */
    public String encoding;

    /**
     * Placeholders to replace in Sql migrations
     */
    public Map<Object, Object> placeholders;

    /**
     * Whether placeholders should be replaced.
     */
    public Boolean placeholderReplacement;

    /**
     * The prefix of every placeholder
     */
    public String placeholderPrefix;

    /**
     * The suffix of every placeholder
     */
    public String placeholderSuffix;

    /**
     * The target version up to which Flyway should consider migrations.
     * Migrations with a higher version number will be ignored. 
     * Special values:
     * <ul>
     * <li>{@code current}: designates the current version of the schema</li>
     * <li>{@code latest}: the latest version of the schema, as defined by the migration with the highest version</li>
     * </ul>
     * Defaults to {@code latest}.
     */
    public String target;

    /**
     * An array of fully qualified FlywayCallback class implementations
     */
    public String[] callbacks;

    /**
     * If set to true, default built-in callbacks will be skipped, only custom migration callbacks will be used.
     * <p>(default: false)</p>
     */
    public Boolean skipDefaultCallbacks;

    /**
     * Allows migrations to be run "out of order"
     */
    public Boolean outOfOrder;

    /**
     * Whether Flyway should output a table with the results of queries when executing migrations (default: true).
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     * <p>Also configurable with Gradle or System Property: ${flyway.outputQueryResults}</p>
     */
    public Boolean outputQueryResults;

    /**
     * Whether to automatically call validate or not when running migrate. (default: true)
     */
    public Boolean validateOnMigrate;

    /**
     * Whether to automatically call clean or not when a validation error occurs. (default: {@code false})<br>
     * <p> This is exclusively intended as a convenience for development. even though we
     * strongly recommend not to change migration scripts once they have been checked into SCM and run, this provides a
     * way of dealing with this case in a smooth manner. The database will be wiped clean automatically, ensuring that
     * the next migration will bring you back to the state checked into SCM.</p>
     * <p><b>Warning ! Do not enable in production !</b></p><br>
     * <p>Also configurable with Gradle or System Property: ${flyway.cleanOnValidationError}</p>
     */
    public Boolean cleanOnValidationError;

    /**
     * Ignore missing migrations when reading the schema history table. These are migrations that were performed by an
     * older deployment of the application that are no longer available in this version. For example: we have migrations
     * available on the classpath with versions 1.0 and 3.0. The schema history table indicates that a migration with version 2.0
     * (unknown to us) has also been applied. Instead of bombing out (fail fast) with an exception, a
     * warning is logged and Flyway continues normally. This is useful for situations where one must be able to deploy
     * a newer version of the application even though it doesn't contain migrations included with an older one anymore.
     * Note that if the most recently applied migration is removed, Flyway has no way to know it is missing and will
     * mark it as future instead.(default: {@code false})
     * <p>Also configurable with Gradle or System Property: ${flyway.ignoreMissingMigrations}</p>
     */
    public Boolean ignoreMissingMigrations;

    /**
     * Ignore ignored migrations when reading the schema history table. These are migrations that were added in between
     * already migrated migrations in this version. For example: we have migrations available on the classpath with
     * versions from 1.0 to 3.0. The schema history table indicates that version 1 was finished on 1.0.15, and the next
     * one was 2.0.0. But with the next release a new migration was added to version 1: 1.0.16. Such scenario is ignored
     * by migrate command, but by default is rejected by validate. When ignoreIgnoredMigrations is enabled, such case
     * will not be reported by validate command. This is useful for situations where one must be able to deliver
     * complete set of migrations in a delivery package for multiple versions of the product, and allows for further
     * development of older versions.(default: {@code false})
     * <p>Also configurable with Gradle or System Property: ${flyway.ignoreIgnoredMigrations}</p>
     */
    public Boolean ignoreIgnoredMigrations;

    /**
     * Ignore pending migrations when reading the schema history table. These are migrations that are available
     * but have not yet been applied. This can be useful for verifying that in-development migration changes
     * don't contain any validation-breaking changes of migrations that have already been applied to a production
     * environment, e.g. as part of a CI/CD process, without failing because of the existence of new migration versions.
     * (default: {@code false})
     * <p>Also configurable with Gradle or System Property: ${flyway.ignorePendingMigrations}</p>
     */
    public Boolean ignorePendingMigrations;

    /**
     * Ignore future migrations when reading the schema history table. These are migrations that were performed by a
     * newer deployment of the application that are not yet available in this version. For example: we have migrations
     * available on the classpath up to version 3.0. The schema history table indicates that a migration to version 4.0
     * (unknown to us) has already been applied. Instead of bombing out (fail fast) with an exception, a
     * warning is logged and Flyway continues normally. This is useful for situations where one must be able to redeploy
     * an older version of the application after the database has been migrated by a newer one. (default: {@code true})
     * <p>Also configurable with Gradle or System Property: ${flyway.ignoreFutureMigrations}</p>
     */
    public Boolean ignoreFutureMigrations;

    /**
     * Whether to validate migrations and callbacks whose scripts do not obey the correct naming convention. A failure can be
     * useful to check that errors such as case sensitivity in migration prefixes have been corrected.
     *{@code false} to continue normally, {@code true} to fail fast with an exception. (default: {@code false})
     * <p>Also configurable with Gradle or System Property: ${flyway.validateMigrationNaming}</p>
     */
    public Boolean validateMigrationNaming;

    /**
     * Whether to disable clean. (default: {@code false})
     * <p>This is especially useful for production environments where running clean can be quite a career limiting move.</p>
     */
    public Boolean cleanDisabled;

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
     * <p>{@code true} if baseline should be called on migrate for non-empty schemas, {@code false} if not. (default: {@code false})</p>
     */
    public Boolean baselineOnMigrate;

    /**
     * Whether to allow mixing transactional and non-transactional statements within the same migration. Enabling this
     * automatically causes the entire affected migration to be run without a transaction.
     *
     * <p>Note that this is only applicable for PostgreSQL, Aurora PostgreSQL, SQL Server and SQLite which all have
     * statements that do not run at all within a transaction.</p>
     * <p>This is not to be confused with implicit transaction, as they occur in MySQL or Oracle, where even though a
     * DDL statement was run within a transaction, the database will issue an implicit commit before and after
     * its execution.</p>
     * <p>{@code true} if mixed migrations should be allowed. {@code false} if an error should be thrown instead. (default: {@code false})</p>
     */
    public Boolean mixed;

    /**
     * Whether to group all pending migrations together in the same transaction when applying them (only recommended for databases with support for DDL transactions).
     * <p>{@code true} if migrations should be grouped. {@code false} if they should be applied individually instead. (default: {@code false})</p>
     */
    public Boolean group;

    /**
     * The username that will be recorded in the schema history table as having applied the migration.
     * {@code null} for the current database user of the connection. (default: {@code null}).
     */
    public String installedBy;

    /**
     * Gradle configurations that will be added to the classpath for running Flyway tasks.
     * (default: <code>compile</code>, <code>runtime</code>, <code>testCompile</code>, <code>testRuntime</code>)
     * <p>Also configurable with Gradle or System Property: ${flyway.configurations}</p>
     */
    public String[] configurations;

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
     * <p>Also configurable with Gradle or System Property: ${flyway.errorOverrides}</p>
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     */
    public String[] errorOverrides;

    /**
     * The file where to output the SQL statements of a migration dry run. If the file specified is in a non-existent
     * directory, Flyway will create all directories and parent directories as needed.
     * <p>{@code null} to execute the SQL statements directly against the database. (default: {@code null})</p>
     * <p>Also configurable with Gradle or System Property: ${flyway.dryRunOutput}</p>
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     */
    public String dryRunOutput;

    /**
     * Whether to stream SQL migrations when executing them. Streaming doesn't load the entire migration in memory at
     * once. Instead each statement is loaded individually. This is particularly useful for very large SQL migrations
     * composed of multiple MB or even GB of reference data, as this dramatically reduces Flyway's memory consumption.
     * (default: {@code false}
     * <p>Also configurable with Gradle or System Property: ${flyway.stream}</p>
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     */
    public Boolean stream;

    /**
     * Whether to batch SQL statements when executing them. Batching can save up to 99 percent of network roundtrips by
     * sending up to 100 statements at once over the network to the database, instead of sending each statement
     * individually. This is particularly useful for very large SQL migrations composed of multiple MB or even GB of
     * reference data, as this can dramatically reduce the network overhead. This is supported for INSERT, UPDATE,
     * DELETE, MERGE and UPSERT statements. All other statements are automatically executed without batching.
     * (default: {@code false})
     * <p>Also configurable with Gradle or System Property: ${flyway.batch}</p>
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     */
    public Boolean batch;

    /**
     * Whether to Flyway's support for Oracle SQL*Plus commands should be activated.
     * (default: {@code false})
     * <p>Also configurable with Gradle or System Property: ${flyway.oracle.sqlplus}</p>
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     */
    public Boolean oracleSqlplus;

    /**
     * Whether Flyway should issue a warning instead of an error whenever it encounters an Oracle SQL*Plus statement
     * it doesn't yet support. (default: {@code false})
     * <p>Also configurable with Gradle or System Property: ${flyway.oracle.sqlplusWarn}</p>
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     */
    public Boolean oracleSqlplusWarn;

    /**
     * Your Flyway license key (FL01...). Not yet a Flyway Pro or Enterprise Edition customer?
     * Request your <a href="https://flywaydb.org/download/">Flyway trial license key</a>
     * to try out Flyway Pro and Enterprise Edition features free for 30 days.
     * <p>Also configurable with Gradle or System Property: ${flyway.licenseKey}</p>
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     */
    public String licenseKey;

    /**
     * The encoding of the external config files specified with the {@code flyway.configFiles} property. (default: UTF-8).
     * <p>Also configurable with Gradle or System Property: ${flyway.configFileEncoding}</p>
     */
    public String configFileEncoding;

    /**
     * Config files from which to load the Flyway configuration. The names of the individual properties match the ones you would
     * use as Gradle or System properties. The encoding of the files is defined by the
     * flyway.configFileEncoding property, which is UTF-8 by default. Relative paths are relative to the project root.
     * <p>Also configurable with Gradle or System Property: ${flyway.configFiles}</p>
     */
    public String[] configFiles;

    /**
     * The working directory to consider when dealing with relative paths for both config files and locations.
     * (default: basedir, the directory where the POM resides)
     * <p/>
     * <p>Also configurable with Gradle or System Property: ${flyway.workingDirectory}</p>
     */
    public String workingDirectory;

    /**
     * Whether Flyway should attempt to create the schemas specified in the schemas property
     *
     * <p>Also configurable with Gradle or System Property: ${flyway.createSchemas}</p>
     */
    public Boolean createSchemas;
}