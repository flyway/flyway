/*
 * Copyright 2010-2018 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.core.internal.jdbc;

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