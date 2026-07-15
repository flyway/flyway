/*-
 * ========================LICENSE_START=================================
 * flyway-starrocks
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.database.starrocks;

import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.BaseDatabaseType;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.database.mysql.MySQLParser;

import java.sql.Connection;
import java.sql.Types;

/**
 * StarRocks database type.
 * <p>
 * StarRocks uses MySQL protocol (jdbc:mysql://) and MySQL Connector/J driver.
 * Detection is via {@code SELECT version()} returning a string containing "starrocks",
 * with a fallback to {@code SELECT @@language} for proxied environments.
 * Priority=1 ensures this type is checked before MySQL (priority=0).
 */
public class StarRocksDatabaseType extends BaseDatabaseType {

    @Override
    public String getName() {
        return "StarRocks";
    }

    @Override
    public int getNullType() {
        return Types.VARCHAR;
    }

    @Override
    public boolean handlesJDBCUrl(String url) {
        return url.startsWith("jdbc:mysql:") || url.startsWith("jdbc:p6spy:mysql:");
    }

    @Override
    public String getDriverClass(String url, ClassLoader classLoader) {
        if (url.startsWith("jdbc:p6spy:mysql:")) {
            return "com.p6spy.engine.spy.P6SpyDriver";
        }
        return "com.mysql.cj.jdbc.Driver";
    }

    @Override
    public boolean handlesDatabaseProductNameAndVersion(String databaseProductName,
        String databaseProductVersion,
        Connection connection) {
        // MySQL Connector/J reports "MySQL" even for StarRocks.
        // SELECT version() returns "5.1.0" (hidden by proxy), so we must
        // use another approach: SELECT @@language returns "/starrocks/share/english/"
        if (!databaseProductName.contains("MySQL")) {
            return false;
        }
        try {
            String version = BaseDatabaseType.getSelectVersionOutput(connection);
            // First check -- SELECT version() might contain "starrocks" on
            // some installations
            if (version != null && version.toLowerCase().contains("starrocks")) {
                return true;
            }
            // Second check -- SELECT @@language contains "starrocks"
            try (java.sql.Statement stmt = connection.createStatement()) {
                try (java.sql.ResultSet rs = stmt.executeQuery("SELECT @@language")) {
                    if (rs.next()) {
                        String language = rs.getString(1);
                        return language != null
                            && language.toLowerCase().contains("starrocks");
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Database createDatabase(Configuration configuration,
        JdbcConnectionFactory jdbcConnectionFactory,
        StatementInterceptor statementInterceptor) {
        return new StarRocksDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    public Parser createParser(Configuration configuration,
        ResourceProvider resourceProvider,
        ParsingContext parsingContext) {
        return new MySQLParser(configuration, parsingContext);
    }

    @Override
    public String instantiateClassExtendedErrorMessage() {
        return "Failure probably due to inability to load dependencies. "
            + "Please ensure you have downloaded MySQL Connector/J "
            + "(https://dev.mysql.com/downloads/connector/j/) "
            + "and placed it in the 'flyway/drivers' folder.";
    }

    /**
     * Priority=1 ensures StarRocks is checked before MySQL (priority=0).
     */
    @Override
    public int getPriority() {
        return 1;
    }
}
