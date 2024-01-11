package org.flywaydb.core.api.migration;

import org.flywaydb.core.api.configuration.Configuration;

import java.sql.Connection;

/**
 * The context relevant to a Java-based migration.
 */
public interface Context {
    /**
     * @return The configuration currently in use.
     */
    Configuration getConfiguration();

    /**
     * @return The JDBC connection being used. Transaction are managed by Flyway.
     * When the context is passed to the migrate method, a transaction will already have
     * been started if required and will be automatically committed or rolled back afterwards, unless the
     * canExecuteInTransaction method has been implemented to return false.
     */
    Connection getConnection();
}