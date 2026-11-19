package org.flywaydb.nc.info;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Reader;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.List;
import org.flywaydb.core.api.CoreMigrationType;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.api.migration.baseline.BaselineMigrationType;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.api.resource.LoadableResourceMetadata;
import org.flywaydb.core.internal.nc.schemahistory.ResolvedSchemaHistoryItem;
import org.flywaydb.core.internal.util.Pair;
import org.junit.jupiter.api.Test;

/**
 * Proves that a newly added baseline migration is never {@code PENDING} on a
 * database that already has applied migrations, matching the classic engine
 * ({@code BaselineResolvedMigration}) and the documented rule that baseline
 * migrations are ignored on existing environments.
 *
 * <p>Upstream is free to delete this test; it exists only to reproduce the
 * bug fixed alongside it (native-connectors marked a new baseline on an
 * existing schema as {@code PENDING}, so {@code migrate} would execute a
 * from-scratch build script against a populated database).
 */
class CoreMigrationStateCalculatorBaselineTest {

    private final CoreMigrationStateCalculator calculator = new CoreMigrationStateCalculator();

    @Test
    void newBaselineOnExistingSchemaIsNotPending() {
        final Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata> baseline =
            Pair.of(null, baselineMetadata("3", "B3__create_table.sql"));

        final List<Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata>> sortedMigrations = List.of(
            Pair.of(appliedVersionedItem(1, "1"), null),
            Pair.of(appliedVersionedItem(2, "2"), null),
            baseline);

        assertEquals(MigrationState.BASELINE_IGNORED,
            calculator.calculateState(baseline, sortedMigrations, configuration()));
    }

    @Test
    void newBaselineOnFreshSchemaIsPending() {
        final Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata> baseline =
            Pair.of(null, baselineMetadata("3", "B3__create_table.sql"));

        assertEquals(MigrationState.PENDING,
            calculator.calculateState(baseline, List.of(baseline), configuration()));
    }

    @Test
    void appliedBaselineStaysBaseline() {
        final LoadableResourceMetadata metadata = baselineMetadata("3", "B3__create_table.sql");
        final Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata> appliedBaseline = Pair.of(
            ResolvedSchemaHistoryItem.builder()
                .installedRank(1)
                .version(MigrationVersion.fromVersion("3"))
                .description("create table")
                .type(BaselineMigrationType.SQL_BASELINE)
                .script("B3__create_table.sql")
                .checksum(1234)
                .installedBy("test")
                .installedOn(LocalDateTime.now())
                .executionTime(0)
                .success(true)
                .build(),
            metadata);

        assertEquals(MigrationState.BASELINE,
            calculator.calculateState(appliedBaseline, List.of(appliedBaseline), configuration()));
    }

    private static FluentConfiguration configuration() {
        return new FluentConfiguration();
    }

    private static ResolvedSchemaHistoryItem appliedVersionedItem(final int rank, final String version) {
        return ResolvedSchemaHistoryItem.builder()
            .installedRank(rank)
            .version(MigrationVersion.fromVersion(version))
            .description("migration " + version)
            .type(CoreMigrationType.SQL)
            .script("V" + version + "__migration.sql")
            .checksum(1000 + rank)
            .installedBy("test")
            .installedOn(LocalDateTime.now())
            .executionTime(0)
            .success(true)
            .build();
    }

    private static LoadableResourceMetadata baselineMetadata(final String version, final String filename) {
        return new LoadableResourceMetadata(MigrationVersion.fromVersion(version),
            "create table",
            "B",
            stubResource(filename),
            null,
            1234,
            BaselineMigrationType.SQL_BASELINE);
    }

    private static LoadableResource stubResource(final String filename) {
        return new LoadableResource() {
            @Override
            public Reader read() {
                return new StringReader("");
            }

            @Override
            public String getAbsolutePath() {
                return filename;
            }

            @Override
            public String getAbsolutePathOnDisk() {
                return filename;
            }

            @Override
            public String getFilename() {
                return filename;
            }

            @Override
            public String getRelativePath() {
                return filename;
            }
        };
    }
}
