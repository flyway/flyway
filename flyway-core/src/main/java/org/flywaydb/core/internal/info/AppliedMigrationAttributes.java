package org.flywaydb.core.internal.info;

public class AppliedMigrationAttributes {
    public boolean outOfOrder;
    public boolean deleted;
    public boolean undone = false;
}