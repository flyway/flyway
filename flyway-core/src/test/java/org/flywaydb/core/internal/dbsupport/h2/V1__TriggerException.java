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
package org.flywaydb.core.internal.dbsupport.h2;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.flywaydb.core.internal.util.jdbc.JdbcUtils;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Migration triggering the exception.
 */
@SuppressWarnings("UnusedDeclaration")
public class V1__TriggerException implements JdbcMigration {
    public void migrate(Connection connection) throws Exception {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.execute("CREATE TABLE clinics (id INT NOT NULL)");
            statement.execute("CREATE TRIGGER clinics_history_trigger AFTER INSERT ON clinics FOR EACH ROW CALL\n" +
                    "\"org.flywaydb.core.internal.dbsupport.h2.TestTrigger\";");
        } finally {
            JdbcUtils.closeStatement(statement);
        }
    }
}
