/*
 * Copyright 2010-2020 Redgate Software Ltd
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
package org.flywaydb.core.internal.database.cloudspanner;

import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CloudSpannerTable extends Table<CloudSpannerDatabase, CloudSpannerSchema> {


    /**
     * Creates a new table.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param schema       The schema this table lives in.
     * @param name         The name of the table.
     */
    public CloudSpannerTable(JdbcTemplate jdbcTemplate, CloudSpannerDatabase database, CloudSpannerSchema schema, String name) {
        super(jdbcTemplate, database, schema, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        try (Connection c = database.getNewRawConnection()){
            Statement s = c.createStatement();
            s.execute("SET READONLY = true");
            s.close();
            try(ResultSet tables = c.getMetaData().getTables("", "", this.name, null)){
                return tables.next();
            }
        }
    }

    @Override
    protected void doLock() throws SQLException {
    }

    @Override
    protected void doDrop() throws SQLException {
        try(Statement statement = jdbcTemplate.getConnection().createStatement()){
            statement.execute("DROP TABLE " + database.quote(name));
        }
    }

    @Override
    public String toString() {
        return database.quote(name);
    }
}