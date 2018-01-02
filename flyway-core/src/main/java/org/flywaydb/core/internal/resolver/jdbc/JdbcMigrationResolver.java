/*
 * Copyright 2010-2018 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
 */
package org.flywaydb.core.internal.resolver.jdbc;

import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.flywaydb.core.internal.resolver.JavaMigrationResolver;
import org.flywaydb.core.internal.util.Locations;
import org.flywaydb.core.internal.util.scanner.Scanner;

/**
 * Migration resolver for JDBC migrations. The classes must have a name like R__My_description, V1__Description
 * or V1_1_3__Description.
 */
public class JdbcMigrationResolver extends JavaMigrationResolver<JdbcMigration, JdbcMigrationExecutor> {
    /**
     * Creates a new instance.
     *
     * @param locations     The base packages on the classpath where to migrations are located.
     * @param scanner       The Scanner for loading migrations on the classpath.
     * @param configuration The configuration to inject (if necessary) in the migration classes.
     */
    public JdbcMigrationResolver(Scanner scanner, Locations locations, FlywayConfiguration configuration) {
        super(scanner, locations, configuration);
    }

    @Override
    protected String getMigrationTypeStr() {
        return "JDBC";
    }

    @Override
    protected Class<JdbcMigration> getImplementedInterface() {
        return JdbcMigration.class;
    }

    @Override
    protected JdbcMigrationExecutor createExecutor(JdbcMigration migration) {
        return new JdbcMigrationExecutor(migration);
    }

    @Override
    protected MigrationType getMigrationType(
            // [pro]
            boolean undo
            // [/pro]
    ) {
        return
                // [pro]
                undo ? MigrationType.UNDO_JDBC :
                        // [/pro]
                        MigrationType.JDBC;
    }
}
