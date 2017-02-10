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
import org.flywaydb.core.api.resolver.MigrationResolver;

import java.util.Map;

/**
 * Readonly interface for main flyway configuration. Can be used to provide configuration data to migrations and callbacks.
 */
public interface FlywayConfiguration {
    /**
     * Retrieves the ClassLoader to use for resolving migrations on the classpath.
     *
     * @return The ClassLoader to use for resolving migrations on the classpath.
     * (default: Thread.currentThread().getContextClassLoader() )
     */
    ClassLoader getClassLoader();
	
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
     * Whether Flyway should skip the default callbacks. If true, only custom callbacks are used.
     *
     * @return Whether default built-in callbacks should be skipped. (default: false)
     */
    boolean isSkipDefaultCallbacks();

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
     * <p>Retrieves the name of the schema metadata table that will be used by Flyway.</p><p> By default (single-schema
     * mode) the metadata table is placed in the default schema for the connection provided by the datasource. </p> <p>
     * When the <i>flyway.schemas</i> property is set (multi-schema mode), the metadata table is placed in the first
     * schema of the list. </p>
     *
     * @return The name of the schema metadata table that will be used by flyway. (default: schema_version)
     */
    String getTable();

    /**
     * Retrieves the encoding of migrations.
     *
     * @return The encoding of migrations. (default: UTF-8)
     */
    String getEncoding();

    /**
     * Retrieves the locations to scan recursively for migrations.
     * <p/>
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
     * Whether to automatically call baseline when migrate is executed against a non-empty schema with no metadata table.
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
     * Ignore missing migrations when reading the metadata table. These are migrations that were performed by an
     * older deployment of the application that are no longer available in this version. For example: we have migrations
     * available on the classpath with versions 1.0 and 3.0. The metadata table indicates that a migration with version 2.0
     * (unknown to us) has also been applied. Instead of bombing out (fail fast) with an exception, a
     * warning is logged and Flyway continues normally. This is useful for situations where one must be able to deploy
     * a newer version of the application even though it doesn't contain migrations included with an older one anymore.
     *
     * @return {@code true} to continue normally and log a warning, {@code false} to fail fast with an exception.
     * (default: {@code false})
     */
    boolean isIgnoreMissingMigrations();

    /**
     * Ignore future migrations when reading the metadata table. These are migrations that were performed by a
     * newer deployment of the application that are not yet available in this version. For example: we have migrations
     * available on the classpath up to version 3.0. The metadata table indicates that a migration to version 4.0
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
    boolean isAllowMixedMigrations();

    /**
     * The username that will be recorded in the metadata table as having applied the migration.
     *
     * @return The username or {@code null} for the current database user of the connection. (default: {@code null}).
     */
    String getInstalledBy();
}
