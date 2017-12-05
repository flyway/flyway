/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.database.oracle.sql.javaSource;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

@SuppressWarnings("UnusedDeclaration")
public class V1__JavaSource implements JdbcMigration {
    private static final String SOURCE_SQL =
            "CREATE OR REPLACE AND COMPILE JAVA SOURCE NAMED \"MyJavaSource\" AS\n" +
                    "public class MyJavaSource {\n" +
                    "  public static int sum(int a, int b) { return a + b; }\n" +
                    "}";

    public void migrate(Connection connection) throws Exception {
        PreparedStatement statement =
            connection.prepareStatement(SOURCE_SQL);
        try {
            statement.setEscapeProcessing(false);
            statement.execute();
        } finally {
            statement.close();
        }
    }
}