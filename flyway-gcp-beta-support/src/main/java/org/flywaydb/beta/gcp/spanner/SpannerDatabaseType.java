/*
 * Copyright © Red Gate Software Ltd 2010-2021
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
package org.flywaydb.beta.gcp.spanner;

import org.flywaydb.beta.gcp.GCPDatabaseExtension;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.BaseDatabaseType;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;

import java.sql.Connection;
import java.sql.Types;

public class SpannerDatabaseType extends BaseDatabaseType {
    @Override
    public String getName() {
        return "Google Cloud Spanner";
    }

    @Override
    public int getNullType() {
        return Types.NULL;
    }

    @Override
    public int getPriority() {
        // All regular database types (including non-beta Spanner support) take priority over this beta
        return -1;
    }








    @Override
    public boolean handlesJDBCUrl(String url) {
        return (url.startsWith("jdbc:cloudspanner:") || url.startsWith("jdbc:p6spy:cloudspanner:"));
    }

    @Override
    public String getDriverClass(String url, ClassLoader classLoader) {
        if (url.startsWith("jdbc:p6spy:cloudspanner:")) {
            return "com.p6spy.engine.spy.P6SpyDriver";
        }
        return "com.google.cloud.spanner.jdbc.JdbcDriver";
    }

    @Override
    public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
        return databaseProductName.contains("Google Cloud Spanner");
    }

    @Override
    public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        return new SpannerDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    public Parser createParser(Configuration configuration, ResourceProvider resourceProvider, ParsingContext parsingContext) {
        return new SpannerParser(configuration, parsingContext);
    }

    @Override
    public boolean detectUserRequiredByUrl(String url) {
        return !url.contains("credentials=");
    }

    @Override
    public boolean detectPasswordRequiredByUrl(String url) {
        return !url.contains("credentials=");
    }
}