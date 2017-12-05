/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.util.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Mapper from ResultSet row to object.
 *
 * @param <T> The type of object to map to.
 */
public interface RowMapper<T> {
    /**
     * Maps a row in this resultSet to an object.
     * @param rs The resultset, already positioned on the row to map.
     * @return The corresponding object.
     * @throws SQLException when reading the resultset failed.
     */
    T mapRow(final ResultSet rs) throws SQLException;
}