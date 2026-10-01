/*-
 * ========================LICENSE_START=================================
 * flyway-benchmark
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.benchmark;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.api.resource.LoadableResourceMetadata;
import org.flywaydb.core.internal.nc.schemahistory.SchemaHistoryItem;
import org.flywaydb.core.internal.nc.schemahistory.SchemaHistoryModel;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.nc.utils.VerbUtils;

/**
 * Reproduces the {@code -validateOnMigrate} workload without a database so it can be profiled in
 * isolation, and reports the two phases separately:
 *
 * <ol>
 *   <li><b>scan</b> &mdash; {@link VerbUtils#scanForResources} (directory walk, file metadata, CRC32
 *       checksums). Runs once per Flyway invocation.</li>
 *   <li><b>info</b> &mdash; {@link VerbUtils#getMigrations} (schema-history join plus
 *       {@code MigrationState} calculation). Runs once per {@code PreparationContext} initialize or
 *       refresh, i.e. 3-4 times per migrate when {@code validateOnMigrate=true}.</li>
 * </ol>
 *
 * <h2>Running</h2>
 * <pre>
 * ./mvnw -q -Pbenchmark -pl flyway-benchmark -am install -DskipTests
 * ./mvnw -Pbenchmark -pl flyway-benchmark exec:exec \
 *     -Dbench.migrations=4400 -Dbench.folders=40 -Dbench.appliedPercent=90
 * </pre>
 *
 * The forked JVM records to {@code flyway-benchmark/target/validate-on-migrate.jfr}. Open it with
 * JDK Mission Control, or {@code jfr summary} / {@code jfr print --events jdk.ExecutionSample}.
 *
 * <p>The generated corpus is cached under {@code bench.dir} (default {@code $TMPDIR/flyway-bench})
 * and reused across runs when the shape matches; pass {@code -Dbench.regenerate=true} to force it.
 */
public final class ValidateOnMigrateBenchmark {

    private static final String SCHEMA_HISTORY_INSTALLED_BY = "benchmark";

    private ValidateOnMigrateBenchmark() {}

    public static void main(final String[] args) throws IOException {
        final String phase = prop("phase", "all");
        final int folderCount = intProp("folders", 40);
        final int repeatableCount = intProp("repeatable", 0);
        final int linesPerScript = intProp("lines", 25);
        final int confEvery = intProp("confEvery", 100);
        final int appliedPercent = intProp("appliedPercent", 90);
        final boolean outOfOrder = Boolean.parseBoolean(prop("outOfOrder", "false"));
        final int warmup = intProp("warmup", 0);
        final int iterations = intProp("iterations", 3);
        final int refreshes = intProp("refreshes", 3);
        final boolean regenerate = Boolean.parseBoolean(prop("regenerate", "false"));

        System.out.printf(Locale.ROOT,
            "folders=%d repeatable=%d linesPerScript=%d appliedPercent=%d outOfOrder=%b%n",
            folderCount,
            repeatableCount,
            linesPerScript,
            appliedPercent,
            outOfOrder);
        System.out.printf(Locale.ROOT, "warmup=%d iterations=%d refreshesPerIteration=%d phase=%s%n",
            warmup,
            iterations,
            refreshes,
            phase);

        for (final int migrationCount : sizes()) {
            runOne(phase,
                migrationCount,
                folderCount,
                repeatableCount,
                linesPerScript,
                confEvery,
                appliedPercent,
                outOfOrder,
                warmup,
                iterations,
                refreshes,
                regenerate);
        }
    }

    private static void runOne(final String phase,
        final int migrationCount,
        final int folderCount,
        final int repeatableCount,
        final int linesPerScript,
        final int confEvery,
        final int appliedPercent,
        final boolean outOfOrder,
        final int warmup,
        final int iterations,
        final int refreshes,
        final boolean regenerate) throws IOException {

        final Path root = corpusRoot(migrationCount, folderCount, repeatableCount, linesPerScript, confEvery);
        generateCorpus(root, migrationCount, folderCount, repeatableCount, linesPerScript, confEvery, regenerate);

        final FluentConfiguration configuration = new FluentConfiguration().locations(locationsFor(root, folderCount))
            .outOfOrder(outOfOrder);

        System.out.printf(Locale.ROOT, "%n===== migrations=%d =====%n", migrationCount);

        final boolean runScan = "all".equals(phase) || "scan".equals(phase);
        final boolean runInfo = "all".equals(phase) || "info".equals(phase);

        // Scanned once outside the timing loop so the info phase can be measured on its own.
        Collection<LoadableResourceMetadata> resources = scan(configuration);
        final SchemaHistoryModel schemaHistoryModel = buildSchemaHistory(resources, appliedPercent);
        final int applied = schemaHistoryModel.getSchemaHistoryItems().size() - 1;
        System.out.printf(Locale.ROOT, "resources=%d  appliedInHistory=%d  pending=%d%n",
            resources.size(),
            applied,
            resources.size() - applied);

        final LoadableResourceMetadata[] resourceArray = resources.toArray(LoadableResourceMetadata[]::new);

        if (runScan) {
            final Stats stats = new Stats("scanForResources");
            for (int i = 0; i < warmup + iterations; i++) {
                final long start = System.nanoTime();
                resources = scan(configuration);
                final long elapsed = System.nanoTime() - start;
                if (i >= warmup) {
                    stats.record(elapsed);
                }
                blackhole(resources.size());
                progress("scan", i, warmup, elapsed);
            }
            stats.print();
        }

        if (runInfo) {
            final Stats perCall = new Stats("getMigrations (single call)");
            for (int i = 0; i < warmup + iterations; i++) {
                final long start = System.nanoTime();
                final MigrationInfo[] migrations = VerbUtils.getMigrations(schemaHistoryModel,
                    resourceArray,
                    configuration);
                final long elapsed = System.nanoTime() - start;
                if (i >= warmup) {
                    perCall.record(elapsed);
                }
                blackhole(migrations.length);
                progress("info", i, warmup, elapsed);
                if (i == 0) {
                    printStateHistogram(migrations);
                }
            }
            perCall.print();
            System.out.printf(Locale.ROOT,
                "%-38s %8.1f ms  (%d calls per migrate when validateOnMigrate=true)%n",
                "=> getMigrations per migrate",
                perCall.medianMillis() * refreshes,
                refreshes);
        }
    }

    /** Printed so a before/after run can be diffed for behaviour changes, not just timings. */
    private static void printStateHistogram(final MigrationInfo[] migrations) {
        final Map<String, Long> histogram = new TreeMap<>();
        for (final MigrationInfo migration : migrations) {
            histogram.merge(String.valueOf(migration.getState()), 1L, Long::sum);
        }
        System.out.println("  states: " + histogram);
    }

    private static void progress(final String tag, final int index, final int warmup, final long elapsedNanos) {
        System.out.printf(Locale.ROOT, "  [%s] %s %d: %.1f ms%n",
            tag,
            index < warmup ? "warmup" : "iter",
            index < warmup ? index + 1 : index - warmup + 1,
            elapsedNanos / 1_000_000.0);
        System.out.flush();
    }

    /** {@code -Dbench.sizes=500,1000,2000,4400} sweeps corpus sizes to expose super-linear growth. */
    private static int[] sizes() {
        final String sizes = prop("sizes", "");
        if (sizes.isBlank()) {
            return new int[] { intProp("migrations", 4400) };
        }
        return Stream.of(sizes.split(",")).map(String::trim).mapToInt(Integer::parseInt).toArray();
    }

    private static Collection<LoadableResourceMetadata> scan(final FluentConfiguration configuration) {
        return VerbUtils.scanForResources(configuration, new ParsingContext(), configuration.getLocations());
    }

    /**
     * Marks the lowest {@code appliedPercent} of versioned migrations as applied, mirroring a
     * long-lived database where most scripts are already in the schema history and a handful are
     * pending. The pending tail is what drives the quadratic {@code highestSHTVersion} path.
     */
    private static SchemaHistoryModel buildSchemaHistory(final Collection<LoadableResourceMetadata> resources,
        final int appliedPercent) {
        final List<LoadableResourceMetadata> versioned = resources.stream()
            .filter(LoadableResourceMetadata::isVersioned)
            .sorted(Comparator.comparing(LoadableResourceMetadata::version))
            .toList();
        final int appliedCount = (int) (versioned.size() * (appliedPercent / 100.0));

        final List<SchemaHistoryItem> items = new ArrayList<>(appliedCount + 1);
        items.add(SchemaHistoryItem.builder()
            .installedRank(0)
            .description("<< Flyway Schema Creation >>")
            .type("SCHEMA")
            .script("benchmark")
            .installedBy(SCHEMA_HISTORY_INSTALLED_BY)
            .installedOn(LocalDateTime.now())
            .success(true)
            .build());

        for (int i = 0; i < appliedCount; i++) {
            final LoadableResourceMetadata resource = versioned.get(i);
            items.add(SchemaHistoryItem.builder()
                .installedRank(i + 1)
                .version(resource.version().getVersion())
                .description(resource.description())
                .type("SQL")
                .script(resource.loadableResource().getRelativePath())
                .checksum(resource.checksum())
                .installedBy(SCHEMA_HISTORY_INSTALLED_BY)
                .installedOn(LocalDateTime.now())
                .executionTime(1)
                .success(true)
                .build());
        }
        return new SchemaHistoryModel(items);
    }

    private static Path corpusRoot(final int migrations,
        final int folders,
        final int repeatable,
        final int lines,
        final int confEvery) {
        final String configured = prop("dir", "");
        if (!configured.isBlank()) {
            return Path.of(configured);
        }
        return Path.of(System.getProperty("java.io.tmpdir"),
            String.format(Locale.ROOT, "flyway-bench-%d-%d-%d-%d-%d",
                migrations,
                folders,
                repeatable,
                lines,
                confEvery));
    }

    private static void generateCorpus(final Path root,
        final int migrations,
        final int folders,
        final int repeatable,
        final int linesPerScript,
        final int confEvery,
        final boolean regenerate) throws IOException {
        final Path marker = root.resolve(".generated");
        if (Files.exists(marker) && !regenerate) {
            System.out.println("reusing corpus at " + root);
            return;
        }
        if (Files.exists(root)) {
            deleteRecursively(root);
        }
        System.out.println("generating corpus at " + root + " ...");

        for (int f = 0; f < folders; f++) {
            Files.createDirectories(folderPath(root, f));
        }
        int confCount = 0;
        for (int i = 1; i <= migrations; i++) {
            final Path folder = folderPath(root, i % folders);
            final String fileName = String.format(Locale.ROOT, "V%d__migration_%d.sql", i, i);
            Files.writeString(folder.resolve(fileName), scriptBody(i, linesPerScript), StandardCharsets.UTF_8);
            if (confEvery > 0 && i % confEvery == 0) {
                Files.writeString(folder.resolve(fileName + ".conf"), "encoding=UTF-8\n", StandardCharsets.UTF_8);
                confCount++;
            }
        }
        for (int i = 1; i <= repeatable; i++) {
            final Path folder = folderPath(root, i % folders);
            Files.writeString(folder.resolve(String.format(Locale.ROOT, "R__repeatable_%d.sql", i)),
                scriptBody(i, linesPerScript),
                StandardCharsets.UTF_8);
        }
        Files.writeString(marker, "ok", StandardCharsets.UTF_8);
        System.out.println("generated " + (migrations + repeatable) + " scripts, " + confCount + " .conf sidecars");
    }

    private static Path folderPath(final Path root, final int index) {
        return root.resolve(String.format(Locale.ROOT, "folder_%03d", index));
    }

    private static String scriptBody(final int seed, final int lines) {
        final StringBuilder sb = new StringBuilder(lines * 64);
        for (int i = 0; i < lines; i++) {
            sb.append("INSERT INTO bench_table_")
                .append(seed)
                .append(" (id, payload) VALUES (")
                .append(i)
                .append(", 'row ")
                .append(i)
                .append(" of script ")
                .append(seed)
                .append("');\n");
        }
        return sb.toString();
    }

    private static String[] locationsFor(final Path root, final int folders) {
        final String[] locations = new String[folders];
        for (int f = 0; f < folders; f++) {
            locations[f] = Location.FILESYSTEM_PREFIX + folderPath(root, f);
        }
        return locations;
    }

    private static void deleteRecursively(final Path path) throws IOException {
        try (Stream<Path> walk = Files.walk(path)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.delete(p);
                } catch (final IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
    }

    @SuppressWarnings("unused")
    private static void blackhole(final int value) {
        if (value == Integer.MIN_VALUE) {
            throw new IllegalStateException("unreachable");
        }
    }

    private static String prop(final String name, final String defaultValue) {
        final String value = System.getProperty("flyway.bench." + name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static int intProp(final String name, final int defaultValue) {
        return Integer.parseInt(prop(name, Integer.toString(defaultValue)));
    }

    private static final class Stats {
        private final String label;
        private final List<Long> samplesNanos = new ArrayList<>();

        private Stats(final String label) {
            this.label = label;
        }

        private void record(final long nanos) {
            samplesNanos.add(nanos);
        }

        private double medianMillis() {
            final List<Long> sorted = samplesNanos.stream().sorted().toList();
            return sorted.isEmpty() ? 0 : sorted.get(sorted.size() / 2) / 1_000_000.0;
        }

        private void print() {
            if (samplesNanos.isEmpty()) {
                return;
            }
            final List<Long> sorted = samplesNanos.stream().sorted().toList();
            final double mean = sorted.stream().mapToLong(Long::longValue).average().orElse(0) / 1_000_000.0;
            System.out.printf(Locale.ROOT, "%-38s n=%d  min=%8.1f ms  median=%8.1f ms  max=%8.1f ms  mean=%8.1f ms%n",
                label,
                sorted.size(),
                sorted.get(0) / 1_000_000.0,
                sorted.get(sorted.size() / 2) / 1_000_000.0,
                sorted.get(sorted.size() - 1) / 1_000_000.0,
                mean);
        }
    }
}
