/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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
package org.flywaydb.core.extensibility;

import org.flywaydb.core.internal.command.clean.CleanModeConfigurationExtension.Mode;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;

public interface CleanModePlugin<T extends Database> extends Plugin {
    boolean handlesMode(Mode cleanMode);

    boolean handlesDatabase(Database database);

    void cleanDatabasePostSchema(T database, JdbcTemplate jdbcTemplate) throws SQLException;
}