package org.flywaydb.core.api.migration.baseline;

import org.flywaydb.core.extensibility.MigrationType;

public enum BaselineMigrationType implements MigrationType {
    /**
     * JDBC Java-based baseline migrations.
     */
    JDBC_BASELINE,
    /**
     * SQL baseline migrations.
     */
    SQL_BASELINE;

    public static MigrationType fromString(String migrationType) {
        if ("JDBC_STATE_SCRIPT".equals(migrationType)) {
            return JDBC_BASELINE;
        }
        if ("SQL_STATE_SCRIPT".equals(migrationType)) {
            return SQL_BASELINE;
        }
        return valueOf(migrationType);
    }

    @Override
    public boolean isUndo() {
        return false;
    }

    @Override
    public boolean isSynthetic() {
        return false;
    }

    @Override
    public boolean isBaseline() {
        return true;
    }
}