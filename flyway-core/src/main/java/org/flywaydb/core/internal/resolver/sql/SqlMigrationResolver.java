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
package org.flywaydb.core.internal.resolver.sql;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.ConfigurationAware;
import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.callback.SqlScriptFlywayCallback;
import org.flywaydb.core.internal.resolver.MigrationInfoHelper;
import org.flywaydb.core.internal.resolver.ResolvedMigrationComparator;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.Locations;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.scanner.Resource;
import org.flywaydb.core.internal.util.scanner.Scanner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.CRC32;

/**
 * Migration resolver for sql files on the classpath. The sql files must have names like
 * V1__Description.sql or V1_1__Description.sql.
 *
 * <p>This class an be replaced with a custom subclass. Note however that since this class is considered
 * internal API, such a subclass is tied to a specific version and my need to be updated when switching to
 * a new flyway version. In order to use a custom subclass:</p>
 * <ul>
 *     <li>create a subclass of this class</li>
 *     <li>disable the usage of the default resolvers using {@link org.flywaydb.core.Flyway#setSkipDefaultResolvers(boolean)}
 *     or the respective property in the flyway configuration file</li>
 *     <li>include the custom subclass as custom resolver using {@link org.flywaydb.core.Flyway#setResolvers(MigrationResolver...)},
 *     {@link org.flywaydb.core.Flyway#setResolversAsClassNames(String...)} or the respective property in the flyway configuration file</li>
 *     <li><b>if you replace this class with a subclass, and want to use the other default resolvers, you need
 *     to include them as custom resolvers as well!</b></li>
 * </ul>
 */
public class SqlMigrationResolver implements MigrationResolver, ConfigurationAware {

    /**
     * The scanner to use.
     */
    private Scanner scanner;

    /**
     * The base directories on the classpath where the migrations are located.
     */
    private Locations locations;

    /**
     * The Flyway configuration.
     */
    private FlywayConfiguration configuration;

    @Override
    public void setFlywayConfiguration(FlywayConfiguration configuration) {
        this.scanner = Scanner.create(configuration.getClassLoader());
        this.locations = new Locations(configuration.getLocations());
        this.configuration = configuration;
    }

    public List<ResolvedMigration> resolveMigrations() {
        List<ResolvedMigration> migrations = new ArrayList<ResolvedMigration>();

        for (Location location: locations.getLocations()) {
            scanForMigrations(location, migrations, configuration.getSqlMigrationPrefix(), configuration.getSqlMigrationSeparator(), configuration.getSqlMigrationSuffix());
            scanForMigrations(location, migrations, configuration.getRepeatableSqlMigrationPrefix(), configuration.getSqlMigrationSeparator(), configuration.getSqlMigrationSuffix());
        }

        Collections.sort(migrations, new ResolvedMigrationComparator());
        return migrations;
    }

    private void scanForMigrations(Location location, List<ResolvedMigration> migrations, String prefix, String separator, String suffix) {
        for (Resource resource : scanner.scanForResources(location, prefix, suffix)) {
            String filename = resource.getFilename();
            if (isSqlCallback(filename, suffix)) {
                continue;
            }
            Pair<MigrationVersion, String> info =
                    MigrationInfoHelper.extractVersionAndDescription(filename, prefix, separator, suffix);

            ResolvedMigrationImpl migration = new ResolvedMigrationImpl();
            migration.setVersion(info.getLeft());
            migration.setDescription(info.getRight());
            migration.setScript(extractScriptName(resource, location));
            migration.setChecksum(calculateChecksum(resource, resource.loadAsString(configuration.getEncoding())));
            migration.setType(MigrationType.SQL);
            migration.setPhysicalLocation(resource.getLocationOnDisk());
            migration.setExecutor(new SqlMigrationExecutor(resource, configuration));
            migrations.add(migration);
        }
    }

    /**
     * Checks whether this filename is actually a sql-based callback instead of a regular migration.
     *
     * @param filename The filename to check.
     * @param suffix   The sql migration suffix.
     * @return {@code true} if it is, {@code false} if it isn't.
     */
    /* private -> testing */
    static boolean isSqlCallback(String filename, String suffix) {
        String baseName = filename.substring(0, filename.length() - suffix.length());
        return SqlScriptFlywayCallback.ALL_CALLBACKS.contains(baseName);
    }

    /**
     * Extracts the script name from this resource.
     *
     * @param resource The resource to process.
     * @return The script name.
     */
    /* private -> for testing */ String extractScriptName(Resource resource, Location location) {
        if (location.getPath().isEmpty()) {
            return resource.getLocation();
        }

        return resource.getLocation().substring(location.getPath().length() + 1);
    }

    /**
     * Calculates the checksum of this string.
     *
     * @param str The string to calculate the checksum for.
     * @return The crc-32 checksum of the bytes.
     */
    /* private -> for testing */
    static int calculateChecksum(Resource resource, String str) {
        final CRC32 crc32 = new CRC32();

        BufferedReader bufferedReader = new BufferedReader(new StringReader(str));
        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                crc32.update(line.getBytes("UTF-8"));
            }
        } catch (IOException e) {
            String message = "Unable to calculate checksum";
            if (resource != null) {
                message += " for " + resource.getLocation() + " (" + resource.getLocationOnDisk() + ")";
            }
            throw new FlywayException(message, e);
        }

        return (int) crc32.getValue();
    }
}
