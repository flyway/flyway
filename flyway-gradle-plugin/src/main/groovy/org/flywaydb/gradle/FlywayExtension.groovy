/**
 * Copyright 2010-2014 Axel Fontaine and the many contributors.
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
 * @author Ben Manes (ben.manes@gmail.com)
 * @see http://flywaydb.org/documentation/commandline
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

    /** The initial version to put in the database */
    String initVersion

    /** The description of the initial version */
    String initDescription

    /**
     * Locations to scan recursively for migrations. The location type is determined by its prefix.
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

    /** The file name prefix for Sql migrations */
    String sqlMigrationPrefix

    /** The file name suffix for Sql migrations */
    String sqlMigrationSuffix

    /** The encoding of Sql migrations */
    String encoding

    /** Placeholders to replace in Sql migrations */
    Map<String, String> placeholders

    /** The prefix of every placeholder */
    String placeholderPrefix

    /** The suffix of every placeholder */
    String placeholderSuffix

    /**
     * The target version up to which Flyway should run migrations. Migrations with a higher version
     * number will not be applied.
     */
    String target

    /** An array of fully qualified FlywayCallback class implementations */
    String[] callbacks

    /** Allows migrations to be run "out of order" */
    Boolean outOfOrder

    /** Whether to automatically call validate or not when running migrate */
    Boolean validateOnMigrate

    /** Whether to automatically call clean or not when a validation error occurs */
    Boolean cleanOnValidationError

    /**
     * Whether to automatically call init when migrate is executed against a non-empty schema
     * with no metadata table.
     */
    Boolean initOnMigrate
}
