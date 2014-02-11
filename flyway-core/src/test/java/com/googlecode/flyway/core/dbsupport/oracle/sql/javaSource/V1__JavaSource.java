package com.googlecode.flyway.core.dbsupport.oracle.sql.javaSource;

import java.sql.Connection;
import java.sql.PreparedStatement;

import com.googlecode.flyway.core.api.migration.jdbc.JdbcMigration;

public class V1__JavaSource implements JdbcMigration {
    
    private static final String SOURCE_SQL =
            "CREATE OR REPLACE AND COMPILE JAVA SOURCE NAMED \"MyJavaSource\" AS\n" +
                    "public class MyJavaSource {\n" +
                    "  public static int sum(int a, int b) { return a + b; }\n" +
                    "}";

    public static void main(String[] args) {
        System.out.println(SOURCE_SQL);
    }

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
