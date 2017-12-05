/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.database.h2;

import org.h2.tools.TriggerAdapter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * H2 Trigger that always fails.
 */
public class TestTrigger extends TriggerAdapter {
    @Override
    public void init(Connection connection, String schemaName, String triggerName, String tableName,
                     boolean before, int type) throws SQLException {
        throw new SQLException("Expected");
    }

    @Override
    public void fire(Connection conn, ResultSet oldRow, ResultSet newRow) throws SQLException {
        // Do nothing
    }
}
