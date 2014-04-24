/**
 * Copyright 2010-2014 Axel Fontaine and the many contributors.
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
package org.flywaydb.core.dbsupport.redshift;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import org.flywaydb.core.dbsupport.DbSupport;
import org.flywaydb.core.dbsupport.JdbcTemplate;
import org.flywaydb.core.dbsupport.Schema;
import org.flywaydb.core.dbsupport.SqlStatementBuilder;
import org.flywaydb.core.util.StringUtils;

/**
 * Redshift-specific support.
 */
public class RedshiftDbSupport extends DbSupport
{
    /**
     * Creates a new instance.
     *
     * @param connection The connection to use.
     */
    public RedshiftDbSupport(Connection connection)
    {
        super(new JdbcTemplate(connection, Types.NULL));
    }

    @Override
    public String getDbName()
    {
        return "redshift";
    }

    public String getCurrentUserFunction()
    {
        return "current_user";
    }

    @Override
    protected String doGetCurrentSchema() throws SQLException
    {
        return jdbcTemplate.queryForString("SELECT current_schema()");
    }

    @Override
    protected void doSetCurrentSchema(Schema schema) throws SQLException
    {
        if (schema == null) {
            jdbcTemplate.execute("SELECT set_config('search_path', '', false)");
            return;
        }

        String searchPath = jdbcTemplate.queryForString("SHOW search_path");
        if (StringUtils.hasText(searchPath) && !searchPath.equals("unset")) {
            // Redshift throws an error on the $ character of $user when setting search_path. It needs to be quoted.
            if (searchPath.contains("$user") && !searchPath.contains(doQuote("$user"))) {
                searchPath = searchPath.replace("$user", doQuote("$user"));
            }

            jdbcTemplate.execute("SET search_path = " + schema + "," + searchPath);
        } else {
            jdbcTemplate.execute("SET search_path = " + schema);
        }

    }

    public boolean supportsDdlTransactions()
    {
        return true;
    }

    public String getBooleanTrue()
    {
        return "TRUE";
    }

    public String getBooleanFalse()
    {
        return "FALSE";
    }

    public SqlStatementBuilder createSqlStatementBuilder()
    {
        return new RedshiftSqlStatementBuilder();
    }

    @Override
    public String doQuote(String identifier)
    {
        return "\"" + StringUtils.replaceAll(identifier, "\"", "\"\"") + "\"";
    }

    @Override
    public Schema getSchema(String name)
    {
        return new RedshiftSchema(jdbcTemplate, this, name);
    }

    @Override
    public boolean catalogIsSchema()
    {
        return false;
    }
}
