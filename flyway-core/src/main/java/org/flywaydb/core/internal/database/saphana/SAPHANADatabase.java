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
package org.flywaydb.core.internal.database.saphana;

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
 * SAP HANA database.
 */
public class SAPHANADatabase extends Database<SAPHANAConnection> {
    /**
     * Creates a new instance.
     *
     * @param connection The connection to use.
     */
    public SAPHANADatabase(Configuration configuration, Connection connection, boolean originalAutoCommit



    ) {
        super(configuration, connection, originalAutoCommit



        );
    }

    @Override
    protected SAPHANAConnection getConnection(Connection connection



    ) {
        return new SAPHANAConnection(configuration, this, connection, originalAutoCommit



        );
    }

    @Override
    public void ensureSupported() {

        ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition("2", org.flywaydb.core.internal.license.Edition.ENTERPRISE);

        recommendFlywayUpgradeIfNecessaryForMajorVersion("2");
    }

    @Override
    public SqlScript createSqlScript(LoadableResource resource, boolean mixed



    ) {
        return new ParserSqlScript(new SAPHANAParser(configuration), resource, mixed);
    }

    @Override
    protected SqlScript getCreateScript(Map<String, String> placeholders) {
        Parser parser = new SAPHANAParser(new FluentConfiguration().placeholders(placeholders));
        return new ParserSqlScript(parser, getRawCreateScript(), false);
    }

    @Override
    protected LoadableResource getRawCreateScript() {
        return new StringResource("CREATE TABLE \"${schema}\".\"${table}\" (\n" +
                "    \"installed_rank\" INT NOT NULL,\n" +
                "    \"version\" VARCHAR(50),\n" +
                "    \"description\" VARCHAR(200) NOT NULL,\n" +
                "    \"type\" VARCHAR(20) NOT NULL,\n" +
                "    \"script\" VARCHAR(1000) NOT NULL,\n" +
                "    \"checksum\" INT,\n" +
                "    \"installed_by\" VARCHAR(100) NOT NULL,\n" +
                "    \"installed_on\" TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,\n" +
                "    \"execution_time\" INT NOT NULL,\n" +
                "    \"success\" TINYINT NOT NULL\n" +
                ");\n" +
                "ALTER TABLE \"${schema}\".\"${table}\" ADD CONSTRAINT \"${table}_pk\" PRIMARY KEY (\"installed_rank\");\n" +
                "\n" +
                "CREATE INDEX \"${schema}\".\"${table}_s_idx\" ON \"${schema}\".\"${table}\" (\"success\");");
    }

    @Override
    public boolean supportsDdlTransactions() {
        return false;
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

}