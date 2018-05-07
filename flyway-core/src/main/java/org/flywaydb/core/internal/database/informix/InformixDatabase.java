/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.informix;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.errorhandler.ErrorHandler;
import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.database.SqlScript;
import org.flywaydb.core.internal.exception.FlywayDbUpgradeRequiredException;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.scanner.LoadableResource;
import org.flywaydb.core.internal.util.scanner.StringResource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Informix database.
 */
public class InformixDatabase extends Database<InformixConnection> {
    /**
     * Creates a new instance.
     *
     * @param configuration The Flyway configuration.
     * @param connection    The connection to use.
     */
    public InformixDatabase(Configuration configuration, Connection connection



    ) {
        super(configuration, connection



        );
    }

    @Override
    protected InformixConnection getConnection(Connection connection



    ) {
        return new InformixConnection(configuration, this, connection



        );
    }

    @Override
    protected final void ensureSupported() {
        String version = majorVersion + "." + minorVersion;

        if (majorVersion < 12 || (majorVersion == 12 && minorVersion < 10)) {
            throw new FlywayDbUpgradeRequiredException("Informix", version, "12.10");
        }
        if ((majorVersion == 12 && minorVersion > 10) || majorVersion > 12) {
            recommendFlywayUpgrade("Informix", version);
        }
    }

    @Override
    protected SqlScript doCreateSqlScript(LoadableResource resource,
                                          PlaceholderReplacer placeholderReplacer, boolean mixed



    ) {
        return new InformixSqlScript(resource, placeholderReplacer, mixed



        );
    }

    @Override
    public LoadableResource getRawCreateScript() {
        return new StringResource("CREATE TABLE ${table} (\n" +
                "    installed_rank INT NOT NULL,\n" +
                "    version VARCHAR(50),\n" +
                "    description VARCHAR(200) NOT NULL,\n" +
                "    type VARCHAR(20) NOT NULL,\n" +
                "    script LVARCHAR(1000) NOT NULL,\n" +
                "    checksum INT,\n" +
                "    installed_by VARCHAR(100) NOT NULL,\n" +
                "    installed_on DATETIME YEAR TO FRACTION(3) DEFAULT CURRENT YEAR TO FRACTION(3) NOT NULL,\n" +
                "    execution_time INT NOT NULL,\n" +
                "    success SMALLINT NOT NULL\n" +
                ");\n" +
                "ALTER TABLE ${schema}.${table} ADD CONSTRAINT ${table}_s CHECK (success in(0,1));\n" +
                "ALTER TABLE ${schema}.${table} ADD CONSTRAINT ${table}_pk PRIMARY KEY (installed_rank);\n" +
                "CREATE INDEX ${schema}.${table}_s_idx ON ${schema}.${table} (success);");
    }

    @Override
    public String getDbName() {
        return "informix";
    }

    @Override
    protected String doGetCurrentUser() throws SQLException {
        return getJdbcMetaData().getUserName();
    }

    @Override
    public boolean supportsDdlTransactions() {
        return true;
    }

    @Override
    protected boolean supportsChangingCurrentSchema() {
        return false;
    }

    @Override
    public String getBooleanTrue() {
        return "t";
    }

    @Override
    public String getBooleanFalse() {
        return "f";
    }

    @Override
    public String doQuote(String identifier) {
        return identifier;
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