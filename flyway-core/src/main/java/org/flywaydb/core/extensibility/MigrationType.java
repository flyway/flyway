package org.flywaydb.core.extensibility;

public interface MigrationType {
    boolean isUndo();

    String name();

    boolean isSynthetic();

    boolean isBaseline();
}