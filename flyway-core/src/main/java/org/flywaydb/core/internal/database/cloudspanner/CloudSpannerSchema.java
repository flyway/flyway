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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.List;

public class CloudSpannerSchema extends Schema<CloudSpannerDatabase, CloudSpannerTable> {
    /**
     * Creates a new schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param name         The name of the schema.
     */
    public CloudSpannerSchema(JdbcTemplate jdbcTemplate, CloudSpannerDatabase database, String name) {
        super(jdbcTemplate, database, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return name.equals("");
    }

    @Override
    protected boolean doEmpty() throws SQLException {

        try (Connection c = database.getNewRawConnection()){
            Statement s = c.createStatement();
            s.execute("SET READONLY = true");
            s.close();
            try(ResultSet tables = c.getMetaData().getTables("", "", null, null)){
                return !tables.next();
            }
        }
    }

    @Override
    protected void doCreate() throws SQLException {
        throw new SQLFeatureNotSupportedException("CREATE DATABASE not supported");
    }

    @Override
    protected void doDrop() throws SQLException {
        throw new SQLFeatureNotSupportedException("DROP DATABASE not supported");
    }

    @Override
    protected void doClean() throws SQLException {
        throw new SQLFeatureNotSupportedException("CLEAN DATABASE not supported");

    }

    @Override
    protected CloudSpannerTable[] doAllTables() throws SQLException {
        List<CloudSpannerTable> tablesList = new ArrayList<>();
        try (Connection c = database.getNewRawConnection()) {
            Statement s = c.createStatement();
            s.execute("SET READONLY = true");
            s.close();
            ResultSet tablesRs = c.getMetaData().getTables("", "", null, null);
            while (tablesRs.next()) {
                tablesList.add(new CloudSpannerTable(jdbcTemplate, database, this,
                        tablesRs.getString("TABLE_NAME")));
            }
            tablesRs.close();
        }

        CloudSpannerTable[] tables = new CloudSpannerTable[tablesList.size()];
        return tablesList.toArray(tables);
    }

    @Override
    public Table getTable(String tableName) {
        return new CloudSpannerTable(jdbcTemplate, database, this, tableName);
    }
}