package org.flywaydb.core.internal.util;

import java.sql.SQLException;

/**
 * An interface analogous to Callable but constrained so that implementations can only throw SqlException,
 * not the more generic Exception.
 */
public interface SqlCallable<V> {

    V call() throws SQLException;

}