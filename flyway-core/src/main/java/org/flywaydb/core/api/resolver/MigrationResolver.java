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
package org.flywaydb.core.api.resolver;

import java.util.Collection;

/**
 * Resolves available migrations. This interface can be implemented to create custom resolvers. A custom resolver
 * can be used to create additional types of migrations not covered by the standard resolvers (jdbc, sql, spring-jdbc).
 * Using the {@link org.flywaydb.core.Flyway#setSkipDefaultResolvers(boolean)}, the built-in resolvers can also be
 * completely replaced.
 *
 * <p>If a resolver also implements the {@link org.flywaydb.core.api.configuration.ConfigurationAware} interface,
 * a {@link org.flywaydb.core.api.configuration.FlywayConfiguration} object will automatically be injected before
 * calling {@link #resolveMigrations()}, giving the resolver access to the core flyway configuration, which provides
 * useful data like resolve locations or placeholder configuration.</p>
 *
 * An abstract implementation is provided in {@link BaseMigrationResolver} which handles the storing of the
 * configuration. It is encouraged to subclass that class instead of implementing this interface directly.
 */
public interface MigrationResolver {
    /**
     * Resolves the available migrations.
     *
     * @return The available migrations.
     */
    Collection<ResolvedMigration> resolveMigrations();
}
