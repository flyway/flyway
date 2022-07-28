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
package org.flywaydb.community.database.kinetica;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.ExecutionTemplate;
import java.sql.SQLException;
import java.util.concurrent.Callable;

public class KineticaExecutionTemplate implements ExecutionTemplate {

    /**
     * Executes this callback.
     * This method is an override to remove all transactional (commits, rollbacks,etc) that are not supported in Kinetica.
     *
     * @param callback The callback to execute.
     * @return The result of the NON transaction KINETICA code.
     */
    @Override
    public <T> T execute(Callable<T> callback) {

        try {
            T result = callback.call();
            return result;
        } catch (Exception e) {
            RuntimeException rethrow;
            if (e instanceof SQLException) {
                rethrow = new FlywaySqlException("Unable to execute SQL statement", (SQLException) e);
            } else if (e instanceof RuntimeException) {
                rethrow = (RuntimeException) e;
            } else {
                rethrow = new FlywayException(e);
            }

            throw rethrow;
        }
    }
}
