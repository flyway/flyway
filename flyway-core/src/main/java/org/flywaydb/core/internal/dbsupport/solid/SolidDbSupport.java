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
<<<<<<< HEAD
/**
 * SolidDB support developed 2014 by Sabine Gallus & Michael Forstner
 * Media-Saturn IT Services GmbH
 * Wankelstr. 5
 * 85046 Ingolstadt, Germany
 * http://www.media-saturn.com
 */
=======
//
// Project: spring-boot-sample-flyway
//
// Media-Saturn IT Services GmbH
//
// Wankelstr. 5
// 85046 Ingolstadt
// Telefon: +49 (841) 634-0
// Telefax: +49 (841) 634-992596
// Web:     www.media-saturn.com
//
>>>>>>> Initial commit for SolidDB support (#885)

package org.flywaydb.core.internal.dbsupport.solid;

import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

public class SolidDbSupport extends DbSupport {

    public SolidDbSupport(final Connection connection) {
        super(new JdbcTemplate(connection, Types.NULL));
    }

    @Override
    public Schema getSchema(final String name) {
        return new SolidSchema(jdbcTemplate, this, name);
    }

    @Override
    public SqlStatementBuilder createSqlStatementBuilder() {
        return new SolidSqlStatementBuilder();
    }

    @Override
    public String getDbName() {
        return "solid";
    }

    @Override
    protected String doGetCurrentSchema() throws SQLException {
        return jdbcTemplate.queryForString("SELECT CURRENT_SCHEMA()");
    }

    @Override
    protected void doSetCurrentSchema(final Schema schema) throws SQLException {
        jdbcTemplate.execute("SET SCHEMA " + schema);
    }

    @Override
    public String getCurrentUserFunction() {
<<<<<<< HEAD
        return "LOGIN_SCHEMA()";
=======
        return "NAME FROM _SYSTEM.SYS_USERS WHERE ID = LOGIN_USERID()";
>>>>>>> Initial commit for SolidDB support (#885)
    }

    @Override
    public boolean supportsDdlTransactions() {
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
    protected String doQuote(final String identifier) {
<<<<<<< HEAD
        return "\"" + identifier + "\"";
=======
        return "'" + identifier + "'";
>>>>>>> Initial commit for SolidDB support (#885)
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }
}
