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
package org.flywaydb.core.internal.dbsupport.oracle.sql.javaSource;

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