/**
 * Copyright 2010-2014 Axel Fontaine
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
package org.flywaydb.core.internal.dbsupport.monetdb;

import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

/**
 * MonetDB-specific support.
 */
public class MonetDBDbSupport extends DbSupport {
    /**
     * Creates a new instance.
     *
     * @param connection The connection to use.
     */
    public MonetDBDbSupport(Connection connection) {
        super(new JdbcTemplate(connection, Types.VARCHAR));
    }

    public String getDbName() {
        return "monetdb";
    }

    public String getCurrentUserFunction() {
        return "\'flyway\'"; // FIXME
    }

    @Override
    protected String doGetCurrentSchema() throws SQLException {
        return jdbcTemplate.getConnection().getCatalog();
    }

    @Override
    protected void doSetCurrentSchema(Schema schema) throws SQLException {
        if ("".equals(schema.getName())) {
            // Weird hack to switch back to no database selected...
            String newDb = quote(UUID.randomUUID().toString());
            jdbcTemplate.execute("CREATE SCHEMA " + newDb);
            jdbcTemplate.execute("USE " + newDb);
            jdbcTemplate.execute("DROP SCHEMA " + newDb);
        } else {
            jdbcTemplate.execute("USE " + schema);
        }
    }

    public boolean supportsDdlTransactions() {
        return true;
    }

    public String getBooleanTrue() {
        return "true";
    }

    public String getBooleanFalse() {
        return "false";
    }

    public SqlStatementBuilder createSqlStatementBuilder() {
        return new MonetDBSqlStatementBuilder();
    }

    @Override
    public String doQuote(String identifier) {
        return "\"" + identifier + "\"";
    }

    @Override
    public Schema getSchema(String name) {
        return new MonetDBSchema(jdbcTemplate, this, name);
    }

    @Override
    public boolean catalogIsSchema() {
        return true;
    }
}
