/*
 * Copyright 2010-2019 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.sqlite;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.resource.LoadableResource;
import org.flywaydb.core.internal.resource.ResourceProvider;
import org.flywaydb.core.internal.resource.StringResource;
import org.flywaydb.core.internal.sqlscript.ParserSqlScript;
import org.flywaydb.core.internal.sqlscript.SqlScript;

import java.sql.Connection;
import java.util.Map;

/**
 * SQLite database.
 */
public class SQLiteDatabase extends Database<SQLiteConnection> {
    /**
     * Creates a new instance.
     *
     * @param configuration The Flyway configuration.
     * @param connection    The connection to use.
     */
    public SQLiteDatabase(Configuration configuration, Connection connection, boolean originalAutoCommit



    ) {
        super(configuration, connection, originalAutoCommit



        );
    }

    @Override
    protected SQLiteConnection getConnection(Connection connection



    ) {
        return new SQLiteConnection(configuration, this, connection, originalAutoCommit



        );
    }

    @Override
    public final void ensureSupported() {
        // #2221: Should be 3.7.2 but older versions of the Xerial JDBC driver misreport 3.x versions as being 3.0.
        ensureDatabaseIsRecentEnough("3.0");
    }

    @Override
    protected SqlScript getCreateScript(Map<String, String> placeholders) {
        Parser parser = new SQLiteParser(new FluentConfiguration().placeholders(placeholders));
        return new ParserSqlScript(parser, getRawCreateScript(), false);
    }

    @Override
    protected LoadableResource getRawCreateScript() {
        return new StringResource("CREATE TABLE \"${schema}\".\"${table}\" (\n" +
                "    \"installed_rank\" INT NOT NULL PRIMARY KEY,\n" +
                "    \"version\" VARCHAR(50),\n" +
                "    \"description\" VARCHAR(200) NOT NULL,\n" +
                "    \"type\" VARCHAR(20) NOT NULL,\n" +
                "    \"script\" VARCHAR(1000) NOT NULL,\n" +
                "    \"checksum\" INT,\n" +
                "    \"installed_by\" VARCHAR(100) NOT NULL,\n" +
                "    \"installed_on\" TEXT NOT NULL DEFAULT (strftime('%Y-%m-%d %H:%M:%f','now')),\n" +
                "    \"execution_time\" INT NOT NULL,\n" +
                "    \"success\" BOOLEAN NOT NULL\n" +
                ");\n" +
                "\n" +
                "CREATE INDEX \"${schema}\".\"${table}_s_idx\" ON \"${table}\" (\"success\");");
    }

    @Override
    public SqlScript createSqlScript(LoadableResource resource, boolean mixed



    ) {
        return new ParserSqlScript(new SQLiteParser(configuration), resource, mixed);
    }

    public String getDbName() {
        return "sqlite";
    }

    @Override
    protected String doGetCurrentUser() {
        return "";
    }

    @Override
    public boolean supportsDdlTransactions() {
        return true;
    }

    @Override
    public boolean supportsChangingCurrentSchema() {
        return false;
    }








    @Override
    public String getBooleanTrue() {
        return "1";
    }

    @Override
    public String getBooleanFalse() {
        return "0";
    }

    @Override
    public String doQuote(String identifier) {
        return "\"" + identifier + "\"";
    }

    @Override
    public boolean catalogIsSchema() {
        return true;
    }

    @Override
    public boolean useSingleConnection() {
        return true;
    }
}