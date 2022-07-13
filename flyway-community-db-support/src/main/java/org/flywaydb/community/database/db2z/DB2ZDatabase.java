/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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
package org.flywaydb.community.database.db2z;

import org.flywaydb.core.api.configuration.Configuration;
// import org.flywaydb.community.configuration.DB2ZConfiguration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * DB2 database.
 */
public class DB2ZDatabase extends Database<DB2ZConnection> {
	private String name;
	
    /**
     * Creates a new instance.
     *
     * @param configuration The Flyway configuration.
     */
    public DB2ZDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        super(configuration, jdbcConnectionFactory, statementInterceptor);
        name = configuration.getDb2zDatabaseName();
    }

    @Override
    protected DB2ZConnection doGetConnection(Connection connection) {
        return new DB2ZConnection(this, connection);
    }

	public String getName() {
		return name;
	}

    @Override
    public final void ensureSupported() {
        ensureDatabaseIsRecentEnough("10.0");

        ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition("11.0", org.flywaydb.core.internal.license.Edition.ENTERPRISE);

        recommendFlywayUpgradeIfNecessary("12.1");
    }

    @Override
    public String getRawCreateScript(Table table, boolean baseline) {
		String tableSpaceName = "SFLYWAY";
		String configurationTablespaceName = configuration.getTablespace();
		if(configurationTablespaceName != null) {
			tableSpaceName = configurationTablespaceName;
		}

        return "SET CURRENT SQLID = '" + table.getSchema().getName() + "';\n" +
		        "CREATE TABLESPACE " + tableSpaceName + " IN \"" + name + "\" MAXPARTITIONS 1 LOCKSIZE ROW CLOSE YES COMPRESS YES;\n" +
		        "CREATE TABLE " + table + " (\n" +
                "    \"installed_rank\" INT NOT NULL,\n" +
                "    \"version\" VARCHAR(50),\n" +
                "    \"description\" VARCHAR(200) NOT NULL,\n" +
                "    \"type\" VARCHAR(20) NOT NULL,\n" +
                "    \"script\" VARCHAR(1000) NOT NULL,\n" +
                "    \"checksum\" INT,\n" +
                "    \"installed_by\" VARCHAR(100) NOT NULL,\n" +
                "    \"installed_on\" TIMESTAMP DEFAULT NOT NULL,\n" +
                "    \"execution_time\" INT NOT NULL,\n" +
                "    \"success\" SMALLINT NOT NULL,\n" +
                "    CONSTRAINT \"" + table.getName() + "_s\" CHECK (\"success\" in (0, 1))\n" +
                ") IN \"" + name + "\"." + tableSpaceName + ";\n" +
                "CREATE UNIQUE INDEX \"" + table.getSchema().getName() + "\".\"" + table.getName() + "_pk_idx\" ON " + table + " (\"installed_rank\");" +
                "ALTER TABLE " + table + " ADD CONSTRAINT \"" + table.getName() + "_pk\" PRIMARY KEY (\"installed_rank\");\n" +
                "CREATE INDEX \"" + table.getSchema().getName() + "\".\"" + table.getName() + "_s_idx\" ON " + table + " (\"success\");" +
                (baseline ? getBaselineStatement(table) + ";\n" : "");
    }

    @Override
    public String getSelectStatement(Table table) {
        return super.getSelectStatement(table)
                // Allow uncommitted reads so info can be invoked while migrate is running
                + " WITH UR";
    }

    @Override
    protected String doGetCurrentUser() throws SQLException {
        return getMainConnection().getJdbcTemplate().queryForString("select USER from sysibm.sysdummy1");
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
        return true;
    }

}