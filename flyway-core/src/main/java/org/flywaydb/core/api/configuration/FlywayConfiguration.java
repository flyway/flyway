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
package org.flywaydb.core.api.configuration;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.flywaydb.core.api.errorhandler.ErrorHandler;
import org.flywaydb.core.api.resolver.MigrationResolver;

import javax.sql.DataSource;
import java.io.OutputStream;
import java.util.Map;

/**
 * Readonly interface for main flyway configuration. Can be used to provide configuration data to migrations and callbacks.
 */
public interface FlywayConfiguration {
    /**
     * Retrieves the ClassLoader to use for loading migrations, resolvers, etc from the classpath.
     *
     * @return The ClassLoader to use for loading migrations, resolvers, etc from the classpath.
     * (default: Thread.currentThread().getContextClassLoader() )
     */
    ClassLoader getClassLoader();

    /**
     * Retrieves the dataSource to use to access the database. Must have the necessary privileges to execute ddl.
     *
     * @return The dataSource to use to access the database. Must have the necessary privileges to execute ddl.
     */
    DataSource getDataSource();

    /**
     * Retrieves the version to tag an existing schema with when executing baseline.
     *
     * @return The version to tag an existing schema with when executing baseline. (default: 1)
     */
    MigrationVersion getBaselineVersion();

    /**
     * Retrieves the description to tag an existing schema with when executing baseline.
     *
     * @return The description to tag an existing schema with when executing baseline. (default: &lt;&lt; Flyway Baseline &gt;&gt;)
     */
    String getBaselineDescription();

    /**
     * Retrieves the The custom MigrationResolvers to be used in addition to the built-in ones for resolving Migrations to apply.
     *
     * @return The custom MigrationResolvers to be used in addition to the built-in ones for resolving Migrations to apply. An empty array if none.
     * (default: none)
     */
    MigrationResolver[] getResolvers();

    /**
     * Whether Flyway should skip the default resolvers. If true, only custom resolvers are used.
     *
     * @return Whether default built-in resolvers should be skipped. (default: false)
     */
    boolean isSkipDefaultResolvers();

    /**
     * Gets the callbacks for lifecycle notifications.
     *
     * @return The callbacks for lifecycle notifications. An empty array if none. (default: none)
     */
    FlywayCallback[] getCallbacks();

    /**
     * Whether Flyway should skip the default callbacks. If true, only custom callbacks are used.
     *
     * @return Whether default built-in callbacks should be skipped. (default: false)
     */
    boolean isSkipDefaultCallbacks();

    /**
     * The file name prefix for versioned SQL migrations.
     * <p>Versioned SQL migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to V1.1__My_description.sql</p>
     *
     * @return The file name prefix for sql migrations. (default: V)
     */
    String getSqlMigrationPrefix();

    /**
     * The file name prefix for undo SQL migrations.
     * <p>Undo SQL migrations are responsible for undoing the effects of the versioned migration with the same version.</p>
     * <p>They have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to U1.1__My_description.sql</p>
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     *
     * @return The file name prefix for undo sql migrations. (default: U)
     */
    String getUndoSqlMigrationPrefix();

    /**
     * Retrieves the file name prefix for repeatable SQL migrations.
     * <p>Repeatable SQL migrations have the following file name structure: prefixSeparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to R__My_description.sql</p>
     *
     * @return The file name prefix for repeatable sql migrations. (default: R)
     */
    String getRepeatableSqlMigrationPrefix();

    /**
     * Retrieves the file name separator for sql migrations.
     * <p>Sql migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to V1_1__My_description.sql</p>
     *
     * @return The file name separator for sql migrations. (default: __)
     */
    String getSqlMigrationSeparator();

    /**
     * Retrieves the file name suffix for sql migrations.
     * <p>Sql migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to V1_1__My_description.sql</p>
     *
     * @return The file name suffix for sql migrations. (default: .sql)
     * @deprecated Use {@link FlywayConfiguration#getSqlMigrationSuffixes()} instead. Will be removed in Flyway 6.0.0.
     */
    @Deprecated
    String getSqlMigrationSuffix();

    /**
     * The file name suffixes for SQL migrations. (default: .sql)
     * <p>SQL migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to V1_1__My_description.sql</p>
     * <p>Multiple suffixes (like .sql,.pkg,.pkb) can be specified for easier compatibility with other tools such as
     * editors with specific file associations.</p>
     *
     * @return The file name suffixes for SQL migrations.
     */
    String[] getSqlMigrationSuffixes();

    /**
     * Checks whether placeholders should be replaced.
     *
     * @return Whether placeholders should be replaced. (default: true)
     */
    boolean isPlaceholderReplacement();

    /**
     * Retrieves the suffix of every placeholder.
     *
     * @return The suffix of every placeholder. (default: } )
     */
    String getPlaceholderSuffix();

    /**
     * Retrieves the prefix of every placeholder.
     *
     * @return The prefix of every placeholder. (default: ${ )
     */
    String getPlaceholderPrefix();

    /**
     * Retrieves the map of &lt;placeholder, replacementValue&gt; to apply to sql migration scripts.
     *
     * @return The map of &lt;placeholder, replacementValue&gt; to apply to sql migration scripts.
     */
    Map<String, String> getPlaceholders();

    /**
     * Retrieves the target version up to which Flyway should consider migrations.
     * Migrations with a higher version number will be ignored.
     * The special value {@code current} designates the current version of the schema.
     *
     * @return The target version up to which Flyway should consider migrations. (default: the latest version)
     */
    MigrationVersion getTarget();

    /**
     * <p>Retrieves the name of the schema schema history table that will be used by Flyway.</p><p> By default (single-schema
     * mode) the schema history table is placed in the default schema for the connection provided by the datasource. </p> <p>
     * When the <i>flyway.schemas</i> property is set (multi-schema mode), the schema history table is placed in the first
     * schema of the list. </p>
     *
     * @return The name of the schema schema history table that will be used by flyway. (default: flyway_schema_history)
     */
    String getTable();

    /**
     * Retrieves the schemas managed by Flyway.  These schema names are case-sensitive.
     * <p>Consequences:</p>
     * <ul>
     * <li>The first schema in the list will be automatically set as the default one during the migration.</li>
     * <li>The first schema in the list will also be the one containing the schema history table.</li>
     * <li>The schemas will be cleaned in the order of this list.</li>
     * </ul>
     *
     * @return The schemas managed by Flyway. (default: The default schema for the datasource connection)
     */
    String[] getSchemas();

    /**
     * Retrieves the encoding of Sql migrations.
     *
     * @return The encoding of Sql migrations. (default: UTF-8)
     */
    String getEncoding();

    /**
     * Retrieves the locations to scan recursively for migrations.
     * <p>The location type is determined by its prefix.
     * Unprefixed locations or locations starting with {@code classpath:} point to a package on the classpath and may
     * contain both sql and java-based migrations.
     * Locations starting with {@code filesystem:} point to a directory on the filesystem and may only contain sql
     * migrations.</p>
     *
     * @return Locations to scan recursively for migrations. (default: db/migration)
     */
    String[] getLocations();

    /**
     * <p>
     * Whether to automatically call baseline when migrate is executed against a non-empty schema with no schema history table.
     * This schema will then be initialized with the {@code baselineVersion} before executing the migrations.
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
     * @return {@code true} if baseline should be called on migrate for non-empty schemas, {@code false} if not. (default: {@code false})
     */
    boolean isBaselineOnMigrate();

    /**
     * Allows migrations to be run "out of order".
     * <p>If you already have versions 1 and 3 applied, and now a version 2 is found,
     * it will be applied too instead of being ignored.</p>
     *
     * @return {@code true} if outOfOrder migrations should be applied, {@code false} if not. (default: {@code false})
     */
    boolean isOutOfOrder();

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
     * @return {@code true} to continue normally and log a warning, {@code false} to fail fast with an exception.
     * (default: {@code false})
     */
    boolean isIgnoreMissingMigrations();

    /**
     * Ignore future migrations when reading the schema history table. These are migrations that were performed by a
     * newer deployment of the application that are not yet available in this version. For example: we have migrations
     * available on the classpath up to version 3.0. The schema history table indicates that a migration to version 4.0
     * (unknown to us) has already been applied. Instead of bombing out (fail fast) with an exception, a
     * warning is logged and Flyway continues normally. This is useful for situations where one must be able to redeploy
     * an older version of the application after the database has been migrated by a newer one.
     *
     * @return {@code true} to continue normally and log a warning, {@code false} to fail fast with an exception.
     * (default: {@code true})
     */
    boolean isIgnoreFutureMigrations();

    /**
     * Whether to automatically call validate or not when running migrate.
     *
     * @return {@code true} if validate should be called. {@code false} if not. (default: {@code true})
     */
    boolean isValidateOnMigrate();

    /**
     * Whether to automatically call clean or not when a validation error occurs.
     * <p> This is exclusively intended as a convenience for development. Even tough we
     * strongly recommend not to change migration scripts once they have been checked into SCM and run, this provides a
     * way of dealing with this case in a smooth manner. The database will be wiped clean automatically, ensuring that
     * the next migration will bring you back to the state checked into SCM.</p>
     * <p><b>Warning ! Do not enable in production !</b></p>
     *
     * @return {@code true} if clean should be called. {@code false} if not. (default: {@code false})
     */
    boolean isCleanOnValidationError();

    /**
     * Whether to disable clean.
     * <p>This is especially useful for production environments where running clean can be quite a career limiting move.</p>
     *
     * @return {@code true} to disabled clean. {@code false} to leave it enabled. (default: {@code false})
     */
    boolean isCleanDisabled();

    /**
     * Whether to allow mixing transactional and non-transactional statements within the same migration.
     *
     * @return {@code true} if mixed migrations should be allowed. {@code false} if an error should be thrown instead. (default: {@code false})
     */
    boolean isMixed();

    /**
     * Whether to group all pending migrations together in the same transaction when applying them (only recommended for databases with support for DDL transactions).
     *
     * @return {@code true} if migrations should be grouped. {@code false} if they should be applied individually instead. (default: {@code false})
     */
    boolean isGroup();

    /**
     * The username that will be recorded in the schema history table as having applied the migration.
     *
     * @return The username or {@code null} for the current database user of the connection. (default: {@code null}).
     */
    String getInstalledBy();

    /**
     * Handlers for errors and warnings that occur during a migration. This can be used to customize Flyway's behavior by for example
     * throwing another runtime exception, outputting a warning or suppressing the error instead of throwing a FlywayException.
     * ErrorHandlers are invoked in order until one reports to have successfully handled the errors or warnings.
     * If none do, or if none are present, Flyway falls back to its default handling of errors and warnings.
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     *
     * @return The ErrorHandlers or an empty array if the default internal handler should be used instead. (default: none)
     */
    ErrorHandler[] getErrorHandlers();

    /**
     * The stream where to output the SQL statements of a migration dry run. {@code null} if the SQL statements
     * are executed against the database directly.
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     *
     * @return The stream or {@code null} if the SQL statements are executed against the database directly.
     */
    OutputStream getDryRunOutput();
}