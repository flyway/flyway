/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.dbsupport.redshift;

import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.FlywaySqlException;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;
import org.flywaydb.core.internal.dbsupport.postgresql.PostgreSQLSqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

import java.sql.SQLException;

/**
 * Redshift-specific support.
 */
public abstract class RedshiftDbSupport extends DbSupport {
    private static final Log LOG = LogFactory.getLog(RedshiftDbSupport.class);

    /**
     * Creates a new instance.
     *
     * @param connection The connection to use.
     */
    public RedshiftDbSupport(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public String getDbName() {
        return "redshift";
    }

    public String getCurrentUserFunction() {
        return "current_user";
    }

    @Override
    public Schema getOriginalSchema() {
        if (originalSchema == null) {
            return null;
        }

        // Defaults to: "$user", public
        String result = originalSchema.replace(doQuote("$user"), "").trim();
        if (result.startsWith(",")) {
            result = result.substring(2);
        }
        if (result.contains(",")) {
            return getSchema(result.substring(0, result.indexOf(",")));
        }
        return getSchema(result);
    }

    @Override
    protected String doGetCurrentSchemaName() throws SQLException {
        String searchPath = jdbcTemplate.queryForString("SHOW search_path");
        if (StringUtils.hasText(searchPath) && !searchPath.equals("unset")) {
            // Redshift throws an error on the $ character of $user when setting search_path. It needs to be quoted.
            if (searchPath.contains("$user") && !searchPath.contains(doQuote("$user"))) {
                searchPath = searchPath.replace("$user", doQuote("$user"));
            }
        }
        return searchPath;
    }

    @Override
    public void changeCurrentSchemaTo(Schema schema) {
        if (schema.getName().equals(originalSchema) || originalSchema.startsWith(schema.getName() + ",") || !schema.exists()) {
            return;
        }

        try {
            if (StringUtils.hasText(originalSchema) && !originalSchema.equals("unset")) {
                doChangeCurrentSchemaTo(schema.toString() + "," + originalSchema);
            } else {
                doChangeCurrentSchemaTo(schema.toString());
            }
        } catch (SQLException e) {
            throw new FlywaySqlException("Error setting current schema to " + schema, e);
        }
    }

    @Override
    protected void doChangeCurrentSchemaTo(String schema) throws SQLException {
        if (!StringUtils.hasLength(schema) || schema.equals("unset")) {
            // After running the following, the "SHOW search_path" command will return "unset"
            jdbcTemplate.execute("SELECT set_config('search_path', '', false)");
            return;
        }
        jdbcTemplate.execute("SET search_path = " + schema);
    }

    public boolean supportsDdlTransactions() {
        return true;
    }

    public String getBooleanTrue() {
        return "TRUE";
    }

    public String getBooleanFalse() {
        return "FALSE";
    }

    public SqlStatementBuilder createSqlStatementBuilder() {
        return new PostgreSQLSqlStatementBuilder();
    }

    @Override
    public String doQuote(String identifier) {
        return "\"" + StringUtils.replaceAll(identifier, "\"", "\"\"") + "\"";
    }

    @Override
    public Schema getSchema(String name) {
        return new RedshiftSchema(jdbcTemplate, this, name);
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }

    /**
     * @return {@code true} if we are connected to Redshift; {@code false} otherwise
     */
    public boolean detect() {
        try {
            return jdbcTemplate.queryForInt("select count(*) from information_schema.tables where table_schema = 'pg_catalog' and table_name = 'stl_s3client'") > 0;
        } catch (SQLException e) {
            LOG.error("Unable to check whether this is a Redshift database", e);
            return false;
        }
    }

}
