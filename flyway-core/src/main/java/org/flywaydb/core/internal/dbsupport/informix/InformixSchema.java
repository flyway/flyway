/**
 * Copyright 2010-2016 Boxfuse GmbH
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
package org.flywaydb.core.internal.dbsupport.informix;

import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

import java.sql.SQLException;
import java.util.List;

public class InformixSchema extends Schema<InformixDbSupport> {

    private static final Log LOG = LogFactory.getLog(InformixSchema.class);

    public InformixSchema(JdbcTemplate jdbcTemplate, InformixDbSupport dbSupport, String name) {
        super(jdbcTemplate, dbSupport, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM systables where owner = ? and tabid > 99", name) > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT count(*) FROM systables WHERE owner = ? and tabid > 99 ", name) == 0;
    }

    @Override
    protected void doCreate() throws SQLException {
    }

    @Override
    protected void doDrop() throws SQLException {
    }

    @Override
    protected void doClean() throws SQLException {
    }

    @Override
    protected Table[] doAllTables() throws SQLException {

        List<String> tableNames = jdbcTemplate.queryForStringList(
                "SELECT TRIM(t.tabname) AS table FROM \"informix\".systables  AS t WHERE t.tabid > 99 ORDER BY t.tabname");

        Table[] tables = new Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new InformixTable(jdbcTemplate, dbSupport, this, tableNames.get(i));
        }
        return tables;

    }

    @Override
    public Table getTable(String tableName) {
        return new InformixTable(jdbcTemplate, dbSupport, this, tableName);
    }

}
