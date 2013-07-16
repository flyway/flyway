package com.googlecode.flyway.core.dbsupport.h2;

import com.googlecode.flyway.core.api.migration.jdbc.JdbcMigration;
import com.googlecode.flyway.core.util.jdbc.JdbcUtils;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Migration triggering the exception.
 */
public class V1__TriggerException implements JdbcMigration {
    public void migrate(Connection connection) throws Exception {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.execute("CREATE TABLE clinics (id INT NOT NULL)");
            statement.execute("CREATE TRIGGER clinics_history_trigger AFTER INSERT ON clinics FOR EACH ROW CALL\n" +
                    "\"com.googlecode.flyway.core.dbsupport.h2.TestTrigger\";");
        } finally {
            JdbcUtils.closeStatement(statement);
        }
    }
}
