/*
 * Copyright 2010-2018 Boxfuse GmbH
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

import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resolver.Context;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.resolver.MigrationInfoHelper;
import org.flywaydb.core.internal.resolver.ResolvedMigrationComparator;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;
import org.flywaydb.core.internal.resource.LoadableResource;
import org.flywaydb.core.internal.resource.ResourceProvider;
import org.flywaydb.core.internal.sqlscript.SqlScript;
import org.flywaydb.core.internal.sqlscript.SqlStatementBuilderFactory;
import org.flywaydb.core.internal.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.zip.CRC32;

/**
 * Migration resolver for SQL files on the classpath. The SQL files must have names like
 * V1__Description.sql, V1_1__Description.sql or R__description.sql.
 */
public class SqlMigrationResolver implements MigrationResolver {
    /**
     * Database-specific support.
     */
    private final Database database;

    /**
     * The resource provider to use.
     */
    private final ResourceProvider resourceProvider;

    private final SqlStatementBuilderFactory sqlStatementBuilderFactory;








    /**
     * The Flyway configuration.
     */
    private final Configuration configuration;

    /**
     * Creates a new instance.
     *
     * @param database                   The database-specific support.
     * @param resourceProvider           The Scanner for loading migrations on the classpath.
     * @param sqlStatementBuilderFactory The SQL statement builder factory.
     * @param configuration              The Flyway configuration.
     */
    public SqlMigrationResolver(Database database, ResourceProvider resourceProvider,
                                SqlStatementBuilderFactory sqlStatementBuilderFactory



            , Configuration configuration) {
        this.database = database;
        this.resourceProvider = resourceProvider;
        this.sqlStatementBuilderFactory = sqlStatementBuilderFactory;



        this.configuration = configuration;
    }

    public List<ResolvedMigration> resolveMigrations(Context context) {
        List<ResolvedMigration> migrations = new ArrayList<>();

        String separator = configuration.getSqlMigrationSeparator();
        String[] suffixes = configuration.getSqlMigrationSuffixes();
        addMigrations(migrations, configuration.getSqlMigrationPrefix(), separator, suffixes,
                false



        );




        addMigrations(migrations, configuration.getRepeatableSqlMigrationPrefix(), separator, suffixes,
                true



        );

        Collections.sort(migrations, new ResolvedMigrationComparator());
        return migrations;
    }

    private void addMigrations(List<ResolvedMigration> migrations, String prefix,
                               String separator, String[] suffixes, boolean repeatable



    ) {
        for (LoadableResource resource : resourceProvider.getResources(prefix, suffixes)) {
            String filename = resource.getFilename();
            if (isSqlCallback(filename, separator, suffixes)) {
                continue;
            }
            Pair<MigrationVersion, String> info =
                    MigrationInfoHelper.extractVersionAndDescription(filename, prefix, separator, suffixes, repeatable);

            ResolvedMigrationImpl migration = new ResolvedMigrationImpl();
            migration.setVersion(info.getLeft());
            migration.setDescription(info.getRight());
            migration.setScript(resource.getRelativePath());

            SqlScript sqlScript = new SqlScript(sqlStatementBuilderFactory, resource, configuration.isMixed());












            int checksum;



                checksum = resource.checksum();











            migration.setChecksum(checksum);
            migration.setType(



                            MigrationType.SQL);
            migration.setPhysicalLocation(resource.getAbsolutePathOnDisk());
            migration.setExecutor(new SqlMigrationExecutor(database, sqlScript



            ));
            migrations.add(migration);
        }
    }


















    /**
     * Checks whether this filename is actually a sql-based callback instead of a regular migration.
     *
     * @param filename  The filename to check.
     * @param separator The separator to use.
     * @param suffixes  The sql migration suffixes.
     * @return {@code true} if it is, {@code false} if it isn't.
     */
    /* private -> testing */
    static boolean isSqlCallback(String filename, String separator, String... suffixes) {
        for (String suffix : suffixes) {
            String baseName = filename.substring(0, filename.length() - suffix.length());
            int index = baseName.indexOf(separator);
            if (index >= 0) {
                baseName = baseName.substring(0, index);
            }
            if (Event.fromId(baseName) != null) {
                return true;
            }
        }
        return false;
    }
}