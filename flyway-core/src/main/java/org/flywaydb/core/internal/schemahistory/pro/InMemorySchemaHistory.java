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
package org.flywaydb.core.internal.schemahistory.pro;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.schemahistory.AppliedMigration;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.util.jdbc.pro.DryRunStatementInterceptor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * In-memory schema history for dry-runs.
 */
public class InMemorySchemaHistory extends SchemaHistory {
    private boolean exists;
    private final String installedBy;
    private final DryRunStatementInterceptor dryRunStatementInterceptor;
    private final List<AppliedMigration> history = new ArrayList<AppliedMigration>();

    public InMemorySchemaHistory(boolean exists, List<AppliedMigration> existingHistory, String installedBy,
                                 DryRunStatementInterceptor dryRunStatementInterceptor) {
        this.exists = exists;
        this.installedBy = installedBy;
        this.dryRunStatementInterceptor = dryRunStatementInterceptor;
        history.addAll(existingHistory);
    }

    @Override
    public <T> T lock(Callable<T> callable) {
        try {
            // No actual locking in this simple in-memory implementation.
            return callable.call();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to execute changes", e);
        } catch (Exception e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public boolean exists() {
        return exists;
    }

    @Override
    public void create() {
        exists = true;
    }

    @Override
    public boolean hasAppliedMigrations() {
        return !history.isEmpty();
    }

    @Override
    public List<AppliedMigration> allAppliedMigrations() {
        return history;
    }

    @Override
    public boolean hasBaselineMarker() {
        return getBaselineMarker() != null;
    }

    @Override
    public AppliedMigration getBaselineMarker() {
        // Baseline marker can only ever be in one of the first two positions in the history.
        for (int i = 0; i < Math.min(history.size(), 2); i++) {
            AppliedMigration appliedMigration = history.get(i);
            if (appliedMigration.getType() == MigrationType.BASELINE) {
                return appliedMigration;
            }
        }
        return null;
    }

    @Override
    public void removeFailedMigrations() {
        Iterator<AppliedMigration> iterator = history.iterator();
        while (iterator.hasNext()) {
            AppliedMigration appliedMigration = iterator.next();
            if (!appliedMigration.isSuccess()) {
                iterator.remove();
            }
        }
    }

    @Override
    public void addSchemasMarker(Schema[] schemas) {
        doAddSchemasMarker(schemas);
    }

    @Override
    public boolean hasSchemasMarker() {
        return !history.isEmpty() && history.get(0).getType() == MigrationType.SCHEMA;
    }

    @Override
    public void update(AppliedMigration applied, ResolvedMigration resolved) {
        AppliedMigration updated = new AppliedMigration(
                applied.getInstalledRank(),
                applied.getVersion(),
                resolved.getDescription(),
                resolved.getType(),
                resolved.getScript(),
                resolved.getChecksum(),
                applied.getInstalledOn(),
                applied.getInstalledBy(),
                applied.getExecutionTime(),
                applied.isSuccess());
        dryRunStatementInterceptor.schemaHistoryTableInsert(updated);
        history.set(applied.getInstalledRank(), updated);
    }

    @Override
    protected void doAddAppliedMigration(MigrationVersion version, String description, MigrationType type, String script, Integer checksum, int executionTime, boolean success) {
        if (!exists) {
            dryRunStatementInterceptor.schemaHistoryTableCreate();
            exists = true;
        }
        AppliedMigration appliedMigration = new AppliedMigration(history.size(), version, description, type, script, checksum, new Date(),
                installedBy, executionTime, success);
        dryRunStatementInterceptor.schemaHistoryTableInsert(appliedMigration);
        history.add(appliedMigration);
    }
}
