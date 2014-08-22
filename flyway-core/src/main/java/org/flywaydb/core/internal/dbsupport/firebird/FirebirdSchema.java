/**
 * Copyright 2010-2014 Axel Fontaine
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.flywaydb.core.internal.dbsupport.firebird;

import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

/**
 * Firebird implementation.
 */
public class FirebirdSchema extends Schema<FirebirdDbSupport> {

    private static final Log LOG = LogFactory.getLog(FirebirdDbSupport.class);

    /**
     * Creates a new Firebird schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param dbSupport The database-specific support.
     * @param name The name of the schema.
     */
    public FirebirdSchema(JdbcTemplate jdbcTemplate, FirebirdDbSupport dbSupport, String name) {
        super(jdbcTemplate, dbSupport, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        try {
            doAllTables();
            return true;
        } catch (SQLException ignored) {
            return false;
        }
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        Table[] tables = allTables();
        return tables.length < 1;
    }

    @Override
    protected void doCreate() throws SQLException {
        LOG.info("Firebird does not support schemas! Schema NOT created: " + name);
        throw new SQLException("Firebird does not support schemas! Schema NOT created: " + name);
    }

    @Override
    protected void doDrop() throws SQLException {
        LOG.info("Firebird does not support schemas! Schema NOT dropped: " + name);
        throw new SQLException("Firebird does not support schemas! Schema NOT dropped: " + name);
    }

    @Override
    protected void doClean() throws SQLException {
        try {
            for (String statement : cleanConstraints()) {
                jdbcTemplate.execute(statement);
            }
            for (String statement : cleanTriggers()) {
                jdbcTemplate.execute(statement);
            }
            for (String statement : cleanGenerators()) {
                jdbcTemplate.execute(statement);
            }
            for (String statement : cleanViews()) {
                jdbcTemplate.execute(statement);
            }
            for (Table table : allTables()) {
                table.drop();
            }
        } catch (SQLException e) {
            throw e;
        }

    }

    @Override
    protected Table[] doAllTables() throws SQLException {
        List<String> tableNames = jdbcTemplate.queryForStringList("select rdb$relation_name from rdb$relations where rdb$view_blr is null and (rdb$system_flag is null or rdb$system_flag = 0)");

        Table[] tables = new Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new FirebirdTable(jdbcTemplate, dbSupport, this, tableNames.get(i).trim());
        }
        return tables;
    }

    @Override
    public Table getTable(String tableName) {
        return new FirebirdTable(jdbcTemplate, dbSupport, this, tableName);
    }

    private List<String> cleanConstraints() throws SQLException {
        final String sql = "select 'alter table '||r.rdb$relation_name ||' drop constraint '||r.rdb$constraint_name||';' "
                + "from rdb$relation_constraints r "
                + "where (r.rdb$constraint_type='FOREIGN KEY') ";
        return jdbcTemplate.queryForStringList(sql);
    }

    private List<String> cleanGenerators() throws SQLException {
        List<String> generators = jdbcTemplate.queryForStringList("select rdb$generator_name from rdb$generators where rdb$system_flag is distinct from 1");

        List<String> statements = new ArrayList<String>();
        for (String generator : generators) {
            statements.add("DROP SEQUENCE " + dbSupport.quote(generator.trim()));
        }
        return statements;
    }

    private List<String> cleanTriggers() throws SQLException {
        List<String> triggers = jdbcTemplate.queryForStringList("select * from rdb$triggers where rdb$system_flag = 0");

        List<String> statements = new ArrayList<String>();
        for (String trigger : triggers) {
            statements.add("DROP TRIGGER " + dbSupport.quote(trigger.trim()));
        }
        return statements;
    }

    private List<String> cleanViews() throws SQLException {
        List<String> views = jdbcTemplate.queryForStringList("select rdb$relation_name from rdb$relations where rdb$view_blr is not null and (rdb$system_flag is null or rdb$system_flag = 0)");

        List<String> statements = new ArrayList<String>();
        for (String view : views) {
            statements.add("DROP VIEW " + dbSupport.quote(view.trim()));
        }
        return statements;
    }

    @Override
    public String toString() {
        return "";
    }

}
