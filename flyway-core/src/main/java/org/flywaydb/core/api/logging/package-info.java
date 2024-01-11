/**
 * Interfaces for Flyway's log abstraction. Custom MigrationResolver, MigrationExecutor, FlywayCallback, ErrorHandler and JdbcMigration
 * implementations should use this to obtain a logger that will work with any logging framework across all environments
 * (API, Maven, Gradle, CLI, etc).
 */
package org.flywaydb.core.api.logging;