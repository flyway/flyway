package com.googlecode.flyway.core.migration.init;

import com.googlecode.flyway.core.migration.Migration;
import com.googlecode.flyway.core.migration.MigrationType;

/**
 * Special type of migration used to mark the initial state of the database from which Flyway can migrate to subsequent
 * versions. There can only be one init migration per database, and, if present, it must be the first one.
 */
public class InitMigration extends Migration {
    @Override
    public MigrationType getMigrationType() {
        return MigrationType.INIT;
    }

    @Override
    public Integer getChecksum() {
        return null;
    }
}
