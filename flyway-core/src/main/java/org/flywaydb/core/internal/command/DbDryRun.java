package org.flywaydb.core.internal.command;

import lombok.CustomLog;
import org.flywaydb.core.api.CoreMigrationType;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.DryRunOutput;
import org.flywaydb.core.api.output.DryRunResult;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.info.MigrationInfoImpl;
import org.flywaydb.core.internal.info.MigrationInfoServiceImpl;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.core.internal.resolver.CompositeMigrationResolver;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.util.ValidatePatternUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

@CustomLog
public class DbDryRun {

    // UNDO_SCRIPT is excluded intentionally: undo scripts are never pending in a forward-migration dry run.
    private static final Set<String> SQL_TYPES = Set.of(
            CoreMigrationType.SQL.toString(),
            CoreMigrationType.SCRIPT.toString());

    private final Database database;
    private final SchemaHistory schemaHistory;
    private final Schema schema;
    private final CompositeMigrationResolver migrationResolver;
    private final Configuration configuration;

    public DbDryRun(Database database, SchemaHistory schemaHistory, Schema schema,
                    CompositeMigrationResolver migrationResolver, Configuration configuration) {
        this.database = database;
        this.schemaHistory = schemaHistory;
        this.schema = schema;
        this.migrationResolver = migrationResolver;
        this.configuration = configuration;
    }

    public DryRunResult dryRun() throws FlywayException {
        DryRunResult result = new DryRunResult(
                VersionPrinter.getVersion(),
                database.getCatalog(),
                String.join(", ", configuration.getSchemas()));

        // Dry run is intentionally not schema-locked: it is a read-only preview and does not modify schema
        // history. A concurrent migration could apply between the info-service snapshot and the result being
        // returned, but that is acceptable — the caller should treat this output as advisory, not definitive.
        MigrationInfoServiceImpl infoService = new MigrationInfoServiceImpl(
                migrationResolver, schemaHistory, database, configuration,
                configuration.getTarget(), configuration.isOutOfOrder(),
                ValidatePatternUtils.getIgnoreAllPattern(), configuration.getCherryPick());
        infoService.refresh();

        MigrationInfo current = infoService.current();
        result.currentSchemaVersion = current == null || current.getVersion() == null
                ? MigrationVersion.EMPTY.getVersion()
                : current.getVersion().getVersion();

        MigrationInfoImpl[] pending = infoService.pending();

        if (pending.length == 0) {
            LOG.info("Schema " + schema + " is up to date. No pending migrations.");
            return result;
        }

        for (MigrationInfoImpl migration : pending) {
            String version = migration.getVersion() != null ? migration.getVersion().getVersion() : null;
            String description = migration.getDescription();
            String type = migration.getType().toString();
            String filepath = migration.getPhysicalLocation() != null ? migration.getPhysicalLocation() : "";
            String sqlContent = readSqlContent(type, filepath);

            if (version != null) {
                LOG.info(">> Would apply SQL migration version " + version + " (" + description + ") [" + filepath + "]");
            } else {
                LOG.info(">> Would apply repeatable migration (" + description + ") [" + filepath + "]");
            }

            result.pendingMigrations.add(new DryRunOutput(version, description, type, filepath, sqlContent));
        }

        return result;
    }

    private String readSqlContent(String type, String filepath) {
        if (!SQL_TYPES.contains(type) || filepath.isEmpty()) {
            return null;
        }
        Path path = Path.of(filepath);
        if (!path.isAbsolute()) {
            // Classpath-embedded resources (e.g. "classpath:db/migration/V1__Init.sql") do not resolve to
            // a filesystem path. Skip rather than throwing an opaque IOException.
            LOG.debug("Skipping SQL content read for non-filesystem path: " + filepath);
            return null;
        }
        try {
            return Files.readString(path);
        } catch (IOException e) {
            LOG.warn("Could not read SQL content from " + filepath + ": " + e.getMessage());
            return null;
        }
    }
}
