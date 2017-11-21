/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * A sql statement from a script that can be executed at once against a database.
 */
public interface SqlStatement {
    /**
     * @return The original line number where the statement was located in the script it came from.
     */
    int getLineNumber();

    /**
     * @return The sql to send to the database.
     */
    String getSql();

    /**
     * Executes this statement against the database.
     *
     * @param connection The connection to use to execute this script.
     * @throws SQLException when the execution fails.
     */
    void execute(Connection connection) throws SQLException;
}
