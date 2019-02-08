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
package org.flywaydb.core.internal.database.postgresql;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.resource.LoadableResource;
import org.flywaydb.core.internal.resource.ResourceProvider;
import org.flywaydb.core.internal.resource.StringResource;
import org.flywaydb.core.internal.sqlscript.ParserSqlScript;
import org.flywaydb.core.internal.sqlscript.SqlScript;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * PostgreSQL database.
 */
public class PostgreSQLDatabase extends Database<PostgreSQLConnection> {
    /**
     * Creates a new instance.
     *
     * @param configuration The Flyway configuration.
     * @param connection    The connection to use.
     */
    public PostgreSQLDatabase(Configuration configuration, Connection connection, boolean originalAutoCommit



    ) {
        super(configuration, connection, originalAutoCommit



        );
    }

    @Override
    protected PostgreSQLConnection getConnection(Connection connection



    ) {
        return new PostgreSQLConnection(configuration, this, connection, originalAutoCommit



        );
    }

    @Override
    public final void ensureSupported() {
        ensureDatabaseIsRecentEnough("9.0");

        ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition("9.4", org.flywaydb.core.internal.license.Edition.ENTERPRISE);


        ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition("9.5", org.flywaydb.core.internal.license.Edition.PRO);

        recommendFlywayUpgradeIfNecessaryForMajorVersion("12");
    }

    @Override
    public SqlScript createSqlScript(LoadableResource resource, boolean mixed



    ) {
        return new ParserSqlScript(new PostgreSQLParser(configuration), resource, mixed);
    }

    @Override
    protected SqlScript getCreateScript(Map<String, String> placeholders) {
        Parser parser = new PostgreSQLParser(new FluentConfiguration().placeholders(placeholders));
        return new ParserSqlScript(parser, getRawCreateScript(), false);
    }

    @Override
    protected LoadableResource getRawCreateScript() {
        String tablespace = configuration.getTablespace() == null
                ? ""
                : " TABLESPACE \"" + configuration.getTablespace() + "\"";

        return new StringResource("CREATE TABLE \"${schema}\".\"${table}\" (\n" +
                "    \"installed_rank\" INT NOT NULL,\n" +
                "    \"version\" VARCHAR(50),\n" +
                "    \"description\" VARCHAR(200) NOT NULL,\n" +
                "    \"type\" VARCHAR(20) NOT NULL,\n" +
                "    \"script\" VARCHAR(1000) NOT NULL,\n" +
                "    \"checksum\" INTEGER,\n" +
                "    \"installed_by\" VARCHAR(100) NOT NULL,\n" +
                "    \"installed_on\" TIMESTAMP NOT NULL DEFAULT now(),\n" +
                "    \"execution_time\" INTEGER NOT NULL,\n" +
                "    \"success\" BOOLEAN NOT NULL\n" +
                ")" + tablespace + ";\n" +
                "ALTER TABLE \"${schema}\".\"${table}\" ADD CONSTRAINT \"${table}_pk\" PRIMARY KEY (\"installed_rank\");\n" +
                "\n" +
                "CREATE INDEX \"${table}_s_idx\" ON \"${schema}\".\"${table}\" (\"success\");");
    }

    @Override
    protected String doGetCurrentUser() throws SQLException {
        return getMainConnection().getJdbcTemplate().queryForString("SELECT current_user");
    }

    @Override
    public boolean supportsDdlTransactions() {
        return true;
    }

    @Override
    public boolean supportsChangingCurrentSchema() {
        return true;
    }

    @Override
    public String getBooleanTrue() {
        return "TRUE";
    }

    @Override
    public String getBooleanFalse() {
        return "FALSE";
    }

    @Override
    public String doQuote(String identifier) {
        return pgQuote(identifier);
    }

    static String pgQuote(String identifier) {
        return "\"" + StringUtils.replaceAll(identifier, "\"", "\"\"") + "\"";
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }

    @Override
    public boolean useSingleConnection() {
        return true;
    }

}
