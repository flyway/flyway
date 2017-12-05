/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.resolver.spring;

import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.flywaydb.core.internal.resolver.JavaMigrationResolver;
import org.flywaydb.core.internal.util.Locations;
import org.flywaydb.core.internal.util.scanner.Scanner;

/**
 * Migration resolver for Spring Jdbc migrations. The classes must have a name like V1 or V1_1_3 or V1__Description
 * or V1_1_3__Description.
 */
public class SpringJdbcMigrationResolver extends JavaMigrationResolver<SpringJdbcMigration, SpringJdbcMigrationExecutor> {
    /**
     * Creates a new instance.
     *
     * @param locations     The base packages on the classpath where to migrations are located.
     * @param scanner       The Scanner for loading migrations on the classpath.
     * @param configuration The configuration to inject (if necessary) in the migration classes.
     */
    public SpringJdbcMigrationResolver(Scanner scanner, Locations locations, FlywayConfiguration configuration) {
        super(scanner, locations, configuration);
    }

    @Override
    protected String getMigrationTypeStr() {
        return "Spring JDBC";
    }

    @Override
    protected Class<SpringJdbcMigration> getImplementedInterface() {
        return SpringJdbcMigration.class;
    }

    @Override
    protected SpringJdbcMigrationExecutor createExecutor(SpringJdbcMigration migration) {
        return new SpringJdbcMigrationExecutor(migration);
    }

    @Override
    protected MigrationType getMigrationType(
            // [pro]
            boolean undo
            // [/pro]
    ) {
        return
                // [pro]
                undo ? MigrationType.UNDO_SPRING_JDBC :
                        // [/pro]
                        MigrationType.SPRING_JDBC;
    }
}