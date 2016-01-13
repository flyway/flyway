/**
 * Copyright 2010-2015 Boxfuse GmbH
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
package org.flywaydb.gradle

/**
 * The flyway's configuration properties.
 *
 * <p>More info: <a href="http://flywaydb.org/documentation/gradle">http://flywaydb.org/documentation/gradle</a></p>
 *
 * @author Ben Manes (ben.manes@gmail.com)
 */
public class FlywayExtension {
    /** The fully qualified classname of the jdbc driver to use to connect to the database */
    String driver

    /** The jdbc url to use to connect to the database */
    String url

    /** The user to use to connect to the database */
    String user

    /** The password to use to connect to the database */
    String password

    /** The name of Flyway's metadata table */
    String table

    /** The case-sensitive list of schemas managed by Flyway */
    String[] schemas

    /**
     * The version to tag an existing schema with when executing baseline. (default: 1)
     */
    String baselineVersion

    /**
     * The description to tag an existing schema with when executing baseline. (default: << Flyway Baseline >>)
     */
    String baselineDescription

    /**
     * Locations to scan recursively for migrations. The location type is determined by its prefix.
     * (default: filesystem:src/main/resources/db/migration)
     *
     * <tt>Unprefixed locations or locations starting with classpath:</tt>
     * point to a package on the classpath and may contain both sql and java-based migrations.
     *
     * <tt>Locations starting with filesystem:</tt>
     * point to a directory on the filesystem and may only contain sql migrations.
     */
    String[] locations

    /**
     * The fully qualified class names of the custom MigrationResolvers to be used in addition to the built-in ones for
     * resolving Migrations to apply.
     * <p>(default: none)</p>
     */
    String[] resolvers

    /**
     * The file name prefix for Sql migrations
     *
     * <p>Sql migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to V1_1__My_description.sql</p>
     */
    String sqlMigrationPrefix

    /**
     * The file name prefix for Sql migrations
     *
     * <p>Sql migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to V1_1__My_description.sql</p>
     */
    String sqlMigrationSeparator

    /**
     * The file name suffix for Sql migrations
     *
     * <p>Sql migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to V1_1__My_description.sql</p>
     */
    String sqlMigrationSuffix

    /** The encoding of Sql migrations */
    String encoding

    /** Placeholders to replace in Sql migrations */
    Map<String, String> placeholders

    /** Whether placeholders should be replaced. */
    Boolean placeholderReplacement

    /** The prefix of every placeholder */
    String placeholderPrefix

    /** The suffix of every placeholder */
    String placeholderSuffix

    /**
     * The target version up to which Flyway should consider migrations.
     * Migrations with a higher version number will be ignored.
     * The special value {@code current} designates the current version of the schema.
     */
    String target

    /** An array of fully qualified FlywayCallback class implementations */
    String[] callbacks

    /** Allows migrations to be run "out of order" */
    Boolean outOfOrder

    /** Whether to automatically call validate or not when running migrate. (default: true) */
    Boolean validateOnMigrate

    /** Whether to automatically call clean or not when a validation error occurs */
    Boolean cleanOnValidationError

    /**
     * Whether to disable clean. (default: {@code false})
     * <p>This is especially useful for production environments where running clean can be quite a career limiting move.</p>
     */
    Boolean cleanDisabled;

    /**
     * <p>
     * Whether to automatically call baseline when migrate is executed against a non-empty schema with no metadata table.
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
    Boolean baselineOnMigrate
}
