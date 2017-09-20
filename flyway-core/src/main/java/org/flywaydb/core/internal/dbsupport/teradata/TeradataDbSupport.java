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
package org.flywaydb.core.internal.dbsupport.teradata;

import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Teradata-specific support.
 */
public class TeradataDbSupport extends DbSupport {
    private static final Log LOG = LogFactory.getLog(TeradataDbSupport.class);

    /**
     * Creates a new instance.
     *
     * @param connection The connection to use.
     */
    public TeradataDbSupport(Connection connection) {
        super(new JdbcTemplate(connection, Types.VARCHAR));
    }

    @Override
    public String getDbName() {
        return "teradata";
    }

    @Override
    public String getCurrentUserFunction() {
        return "user";
    }

    @Override
    protected String doGetCurrentSchemaName() throws SQLException {
        return jdbcTemplate.queryForString("SELECT database");
    }

    @Override
    protected void doChangeCurrentSchemaTo(String schema) throws SQLException {
        LOG.info("Teradata does not support schema. Default schema NOT changed to " + schema);
    }

    @Override
    public boolean supportsDdlTransactions() {
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
    public SqlStatementBuilder createSqlStatementBuilder() {
        return new TeradataSqlStatementBuilder();
    }

    @Override
    public String doQuote(String identifier) {
        return "\"" + StringUtils.replaceAll(identifier, "\"", "\"\"") + "\"";
    }

    @Override
    public Schema getSchema(String name) {
        return new TeradataSchema(jdbcTemplate, this, name);
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }
}
