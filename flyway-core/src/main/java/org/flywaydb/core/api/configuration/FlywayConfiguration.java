/**
 * Copyright 2010-2016 Boxfuse GmbH
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
import org.flywaydb.core.api.callback.SQLFlywayCallback;
import org.flywaydb.core.api.callback.MongoFlywayCallback;
import org.flywaydb.core.api.resolver.MigrationResolver;

import com.mongodb.MongoClient;

import javax.sql.DataSource;
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
}
