package org.flywaydb.verb.migrate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.api.output.MigrateResult;
import org.flywaydb.nc.preparation.PreparationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * End-to-end proof, on SQLite through native connectors, that a newly added
 * baseline migration is ignored on a database that already has applied
 * migrations.
 *
 * <p>The baseline script below is a full-state build script: executing it
 * against the migrated database fails (the table already exists), so any
 * attempt to run it proves the bug. Upstream is free to delete this test;
 * it exists only to reproduce the fixed bug.
 */
class BaselineIgnoredOnExistingSqliteTest {

    @TempDir
    Path workDir;

    @Test
    void newBaselineIsIgnoredAndNotExecutedOnSqliteWithExistingMigrations() throws Exception {
        final Path migrations = workDir.resolve("migrations");
        Files.createDirectory(migrations);
        final Path database = workDir.resolve("test.db");

        write(migrations, "V1__create_person.sql",
            "CREATE TABLE person (id INT PRIMARY KEY, name VARCHAR(100));");
        write(migrations, "V2__add_email.sql",
            "ALTER TABLE person ADD COLUMN email VARCHAR(100);");

        final MigrateResult first = migrate(database, migrations);
        assertEquals(2, first.migrationsExecuted, () -> "states after first migrate: " + describe(database, migrations));

        write(migrations, "B2__baseline.sql",
            "CREATE TABLE person (id INT PRIMARY KEY, name VARCHAR(100), email VARCHAR(100));");

        final FluentConfiguration config = ncConfig(database, migrations);
        final PreparationContext context = PreparationContext.get(config, false);
        final MigrationInfo baseline = Arrays.stream(context.getMigrations())
            .filter(m -> MigrationVersion.fromVersion("2").equals(m.getVersion()) && m.getType().isBaseline())
            .findFirst()
            .orElseThrow(() -> new AssertionError("B2 baseline migration was not resolved"));
        assertEquals(MigrationState.BASELINE_IGNORED, baseline.getState());

        final MigrateResult second = (MigrateResult) new MigrateVerbExtension().executeVerb(config);
        assertEquals(0, second.migrationsExecuted);
    }

    private String describe(final Path database, final Path migrations) {
        final PreparationContext context = PreparationContext.get(ncConfig(database, migrations), false);
        return Arrays.stream(context.getMigrations())
            .map(info -> info.getScript() + "=" + info.getState())
            .toList()
            .toString();
    }

    private MigrateResult migrate(final Path database, final Path migrations) {
        return (MigrateResult) new MigrateVerbExtension().executeVerb(ncConfig(database, migrations));
    }

    private FluentConfiguration ncConfig(final Path database, final Path migrations) {
        return new FluentConfiguration().dataSource("jdbc:sqlite:" + database.toAbsolutePath(), "", "")
            .locations("filesystem:" + migrations.toAbsolutePath());
    }

    private static void write(final Path dir, final String filename, final String content) throws Exception {
        Files.write(dir.resolve(filename), content.getBytes(StandardCharsets.UTF_8));
    }
}
