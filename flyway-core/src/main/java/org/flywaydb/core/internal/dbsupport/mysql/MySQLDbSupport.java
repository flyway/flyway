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
package org.flywaydb.core.internal.dbsupport.mysql;

import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

/**
 * Mysql-specific support.
 */
public class MySQLDbSupport extends DbSupport {
    private static final Log LOG = LogFactory.getLog(MySQLDbSupport.class);

    /**
     * Creates a new instance.
     *
     * @param connection The connection to use.
     */
    public MySQLDbSupport(Connection connection) {
        super(new JdbcTemplate(connection, Types.VARCHAR));
    }

    public String getDbName() {
        return "mysql";
    }

    public String getCurrentUserFunction() {
        return "SUBSTRING_INDEX(USER(),'@',1)";
    }

    @Override
    protected String doGetCurrentSchema() throws SQLException {
        return jdbcTemplate.getConnection().getCatalog();
    }

    @Override
    protected void doSetCurrentSchema(Schema schema) throws SQLException {
        if ("".equals(schema.getName())) {
            try {
                // Weird hack to switch back to no database selected...
                String newDb = quote(UUID.randomUUID().toString());
                jdbcTemplate.execute("CREATE SCHEMA " + newDb);
                jdbcTemplate.execute("USE " + newDb);
                jdbcTemplate.execute("DROP SCHEMA " + newDb);
            } catch (Exception e) {
                LOG.warn("Unable to restore connection to having no default schema");
            }
        } else {
            jdbcTemplate.execute("USE " + schema);
        }
    }

    public boolean supportsDdlTransactions() {
        return false;
    }

    public String getBooleanTrue() {
        return "1";
    }

    public String getBooleanFalse() {
        return "0";
    }

    public SqlStatementBuilder createSqlStatementBuilder() {
        return new MySQLSqlStatementBuilder();
    }

    @Override
    public String doQuote(String identifier) {
        return "`" + identifier + "`";
    }

    @Override
    public Schema getSchema(String name) {
        return new MySQLSchema(jdbcTemplate, this, name);
    }

    @Override
    public boolean catalogIsSchema() {
        return true;
    }
}
