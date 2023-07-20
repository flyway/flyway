/*
 * Copyright (C) Red Gate Software Ltd 2010-2023
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
package  org.flywaydb.core.internal.database.sinodb;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.database.base.Schema;
import com.googlecode.flyway.core.dbsupport.SqlStatementBuilder;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import java.sql.Connection;
import java.sql.SQLException;

public class SinoDBDbSupport extends DbSupport {
    private static final Log LOG = LogFactory.getLog(SinoDBDbSupport.class);

    public SinoDBDbSupport(Connection connection) {
        super(new JdbcTemplate(connection, 12));
    }

    public SqlStatementBuilder createSqlStatementBuilder() {
        return new SinoDBSqlStatementBuilder();
    }

    public String getScriptLocation() {
        return "com/googlecode/flyway/core/dbsupport/sinodb/";
    }

    protected String doGetCurrentSchema() throws SQLException {
        return this.jdbcTemplate.getMetaData().getUserName();
    }

    protected void doSetCurrentSchema(Schema schema) throws SQLException {
        LOG.warn("SinoDB does not support setting the schema for the current session. Default schema NOT changed to " + schema);
    }

    public String getCurrentUserFunction() {
        return "CURRENT_USER";
    }

    public boolean supportsDdlTransactions() {
        return true;
    }

    public String getBooleanTrue() {
        return "'t'";
    }

    public String getBooleanFalse() {
        return "'f'";
    }

    public String doQuote(String identifier) {
        return identifier;
    }

    public Schema getSchema(String name) {
        return new SinoDBSchema(this.jdbcTemplate, this, name);
    }

    public boolean catalogIsSchema() {
        return false;
    }