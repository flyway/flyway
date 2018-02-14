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
package org.flywaydb.core.internal.database.redshift;

import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.errorhandler.ErrorHandler;
import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.database.SqlScript;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.util.scanner.Resource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Redshift database.
 */
public class RedshiftDatabase extends Database<RedshiftConnection> {
    /**
     * Checks whether this connection is pointing at a Redshift instance.
     *
     * @param connection The connection.
     * @return {@code true} if it is, {@code false} if not.
     */
    public static boolean isRedshift(Connection connection) {
        try {
            return new JdbcTemplate(connection).queryForString("SELECT version()").contains("Redshift");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Creates a new instance.
     *
     * @param configuration The Flyway configuration.
     * @param connection    The connection to use.
     */
    public RedshiftDatabase(FlywayConfiguration configuration, Connection connection



    ) {
        super(configuration, connection, Types.VARCHAR



        );
    }

    @Override
    protected RedshiftConnection getConnection(Connection connection, int nullType



    ) {
        return new RedshiftConnection(configuration, this, connection, nullType



        );
    }

    @Override
    protected final void ensureSupported() {
        // Always latest Redshift version.
    }

    @Override
    protected SqlScript doCreateSqlScript(Resource sqlScriptResource, String sqlScriptSource, boolean mixed



    ) {
        return new RedshiftSqlScript(sqlScriptResource, sqlScriptSource, mixed



        );
    }

    @Override
    public String getDbName() {
        return "redshift";
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
    public String getBooleanTrue() {
        return "TRUE";
    }

    @Override
    public String getBooleanFalse() {
        return "FALSE";
    }

    @Override
    public String doQuote(String identifier) {
        return redshiftQuote(identifier);
    }

    static String redshiftQuote(String identifier) {
        return "\"" + StringUtils.replaceAll(identifier, "\"", "\"\"") + "\"";
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