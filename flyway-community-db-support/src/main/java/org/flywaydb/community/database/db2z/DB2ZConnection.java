/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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
package org.flywaydb.community.database.db2z;

import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.exception.FlywaySqlException;

import lombok.CustomLog;
import java.sql.SQLException;

/**
 * DB2 connection.
 */
@CustomLog
public class DB2ZConnection extends Connection<DB2ZDatabase> {
    DB2ZConnection(DB2ZDatabase database, java.sql.Connection connection) {
        super(database, connection);
        this.jdbcTemplate = new DB2ZJdbcTemplate(connection);
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
        return jdbcTemplate.queryForString("select current_schema from sysibm.sysdummy1");
    }

    @Override
    public void changeCurrentSchemaTo(Schema schema) {
        try {
            if (!schema.exists()) {
                return;
            }
            doChangeCurrentSchemaOrSearchPathTo(schema.getName());
        } catch (SQLException e) {
            String sqlId = (database.getSqlId() == "") ? schema.getName() : database.getSqlId();
            LOG.info("SET CURRENT SQLID = '" + sqlId + "'");
            LOG.info("SET SCHEMA " + database.quote(schema.getName()));
            throw new FlywaySqlException("Error setting current sqlid and/or schema", e);
        }
    }

    @Override
    public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
        // Maybe sqlid not same as schema name and entered as config property
        String sqlId = (database.getSqlId() == "") ? schema : database.getSqlId();
        jdbcTemplate.execute("SET CURRENT SQLID = '" + sqlId + "'");
        jdbcTemplate.execute("SET SCHEMA " + database.quote(schema));
	}

    @Override
    public Schema getSchema(String name) {
        return new DB2ZSchema(jdbcTemplate, database, name);
    }
}