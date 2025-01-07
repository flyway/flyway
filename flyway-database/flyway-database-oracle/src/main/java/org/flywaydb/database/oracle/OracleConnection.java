/*-
 * ========================LICENSE_START=================================
 * flyway-database-oracle
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.database.oracle;

import lombok.Getter;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;

import java.sql.SQLException;
import org.flywaydb.core.internal.util.StringUtils;

public class OracleConnection extends Connection<OracleDatabase> {
    @Getter
    private final boolean awsRds;

    OracleConnection(OracleDatabase database, java.sql.Connection connection) {
        super(database, connection);

        awsRds = rdsAdminExists();
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
        return jdbcTemplate.queryForString("SELECT SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') FROM DUAL");
    }

    @Override
    public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
        jdbcTemplate.execute("ALTER SESSION SET CURRENT_SCHEMA=" + database.quote(schema));
    }

    @Override
    public Schema getSchema(String name) {
        return new OracleSchema(jdbcTemplate, database, name);
    }

    private boolean rdsAdminExists() {
        try {
            return StringUtils.hasText(jdbcTemplate.queryForString("SELECT username FROM all_users WHERE UPPER(username) = 'RDSADMIN'"));
        } catch (Exception e) {
            return false;
        }
    }
}
