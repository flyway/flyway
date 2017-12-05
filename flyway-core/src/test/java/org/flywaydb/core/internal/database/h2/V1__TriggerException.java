/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.database.h2;

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
                    "\"org.flywaydb.core.internal.database.h2.TestTrigger\";");
        } finally {
            JdbcUtils.closeStatement(statement);
        }
    }
}
