package org.flywaydb.core.internal.jdbc;

// Spanner requires nulls to be the same type as the column and this enum allows us to calculate that
public enum JdbcNullTypes {
    StringNull,
    IntegerNull,
    BooleanNull
}