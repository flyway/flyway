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
package org.flywaydb.core.internal.database.db2;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.resource.LoadableResource;
import org.flywaydb.core.internal.resource.ResourceProvider;
import org.flywaydb.core.internal.resource.StringResource;
import org.flywaydb.core.internal.sqlscript.ParserSqlScript;
import org.flywaydb.core.internal.sqlscript.SqlScript;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * DB2 database.
 */
public class DB2Database extends Database<DB2Connection> {
    /**
     * Creates a new instance.
     *
     * @param configuration The Flyway configuration.
     * @param connection    The connection to use.
     */
    public DB2Database(Configuration configuration, Connection connection, boolean originalAutoCommit



    ) {
        super(configuration, connection, originalAutoCommit



        );
    }

    @Override
    protected DB2Connection getConnection(Connection connection



    ) {
        return new DB2Connection(configuration, this, connection, originalAutoCommit



        );
    }










    @Override
    public final void ensureSupported() {
        ensureDatabaseIsRecentEnough("9.7");

        ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition("10.5", org.flywaydb.core.internal.license.Edition.ENTERPRISE);


        ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition("11.1", org.flywaydb.core.internal.license.Edition.PRO);

        recommendFlywayUpgradeIfNecessary("11.1");
    }

    @Override
    protected SqlScript getCreateScript(Map<String, String> placeholders) {
        Parser parser = new DB2Parser(new FluentConfiguration().placeholders(placeholders));
        return new ParserSqlScript(parser, getRawCreateScript(), false);
    }

    @Override
    public SqlScript createSqlScript(LoadableResource resource, boolean mixed



    ) {
        return new ParserSqlScript(new DB2Parser(configuration), resource, mixed);
    }

    @Override
    public LoadableResource getRawCreateScript() {
        String tablespace = configuration.getTablespace() == null
                ? ""
                : " IN \"" + configuration.getTablespace() + "\"";

        return new StringResource("CREATE TABLE \"${schema}\".\"${table}\" (\n" +
                "    \"installed_rank\" INT NOT NULL,\n" +
                "    \"version\" VARCHAR(50),\n" +
                "    \"description\" VARCHAR(200) NOT NULL,\n" +
                "    \"type\" VARCHAR(20) NOT NULL,\n" +
                "    \"script\" VARCHAR(1000) NOT NULL,\n" +
                "    \"checksum\" INT,\n" +
                "    \"installed_by\" VARCHAR(100) NOT NULL,\n" +
                "    \"installed_on\" TIMESTAMP DEFAULT CURRENT TIMESTAMP NOT NULL,\n" +
                "    \"execution_time\" INT NOT NULL,\n" +
                "    \"success\" SMALLINT NOT NULL,\n" +
                "    CONSTRAINT \"${table}_s\" CHECK (\"success\" in(0,1))\n" +
                ")" +



                        " ORGANIZE BY ROW"



                + tablespace + ";\n"
                + "ALTER TABLE \"${schema}\".\"${table}\" ADD CONSTRAINT \"${table}_pk\" PRIMARY KEY (\"installed_rank\");\n" +
                "\n" +
                "CREATE INDEX \"${schema}\".\"${table}_s_idx\" ON \"${schema}\".\"${table}\" (\"success\");");
    }

    @Override
    public String getSelectStatement(Table table, int maxCachedInstalledRank) {
        return super.getSelectStatement(table, maxCachedInstalledRank)
                // Allow uncommitted reads so info can be invoked while migrate is running
                + " WITH UR";
    }

    @Override
    protected String doGetCurrentUser() throws SQLException {
        return getMainConnection().getJdbcTemplate().queryForString("select CURRENT_USER from sysibm.sysdummy1");
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
        return false;
    }

    @Override
    public boolean useSingleConnection() {
        return false;
    }

}