package org.flywaydb.core.internal.schemahistory.pro;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.dbsupport.FlywaySqlException;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.schemahistory.AppliedMigration;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * In-memory schema history for dry-runs.
 */
public class InMemorySchemaHistory extends SchemaHistory {
    private boolean exists;
    private final String installedBy;
    private final List<AppliedMigration> history = new ArrayList<AppliedMigration>();

    public InMemorySchemaHistory(boolean exists, List<AppliedMigration> existingHistory, String installedBy) {
        this.exists = exists;
        this.installedBy = installedBy;
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
    public boolean hasAppliedMigrations() {
        return !history.isEmpty();
    }

    @Override
    public List<AppliedMigration> allAppliedMigrations() {
        return history;
    }

    @Override
    public boolean hasBaselineMarker() {
        return false;
    }

    @Override
    public AppliedMigration getBaselineMarker() {
        return null;
    }

    @Override
    public void removeFailedMigrations() {

    }

    @Override
    public void addSchemasMarker(Schema[] schemas) {
        doAddSchemasMarker(schemas);
    }

    @Override
    public boolean hasSchemasMarker() {
        return false;
    }

    @Override
    public void update(AppliedMigration appliedMigration, ResolvedMigration resolvedMigration) {

    }

    @Override
    protected void doAddAppliedMigration(MigrationVersion version, String description, MigrationType type, String script, Integer checksum, int executionTime, boolean success) {
        exists = true;
        history.add(new AppliedMigration(history.size(), version, description, type, script, checksum, new Date(),
                installedBy, executionTime, success));
    }
}
