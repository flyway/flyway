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
package org.flywaydb.core.internal.database.cockroachdb;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.util.SqlCallable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * CockroachDB implementation of Schema.
 */
public class CockroachDBSchema extends Schema<CockroachDBDatabase, CockroachDBTable> {
    private static final Log LOG = LogFactory.getLog(CockroachDBSchema.class);

    /**
     * Is this CockroachDB 1.x.
     */
    final boolean cockroachDB1;

    /**
     * Creates a new CockroachDB schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param name         The name of the schema.
     */
    CockroachDBSchema(JdbcTemplate jdbcTemplate, CockroachDBDatabase database, String name) {
        super(jdbcTemplate, database, name);
        cockroachDB1 = !database.getVersion().isAtLeast("2");
    }

    @Override
    protected boolean doExists() throws SQLException {
        return new CockroachDBRetryingStrategy().execute(new SqlCallable<Boolean>() {
            @Override
            public Boolean call() throws SQLException {
                return doExistsOnce();
            }
        });
    }

    private boolean doExistsOnce() throws SQLException {
        return jdbcTemplate.queryForBoolean("SELECT EXISTS ( SELECT 1 FROM pg_database WHERE datname=? )", name);
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        return new CockroachDBRetryingStrategy().execute(new SqlCallable<Boolean>() {
            @Override
            public Boolean call() throws SQLException {
                return doEmptyOnce();
            }
        });
    }

    private boolean doEmptyOnce() throws SQLException {
        if (cockroachDB1) {
            return !jdbcTemplate.queryForBoolean("SELECT EXISTS (" +
                    "  SELECT 1" +
                    "  FROM information_schema.tables" +
                    "  WHERE table_schema=?" +
                    "  AND table_type='BASE TABLE'" +
                    ")", name);
        }
        return !jdbcTemplate.queryForBoolean("SELECT EXISTS (" +
                "  SELECT 1" +
                "  FROM information_schema.tables " +
                "  WHERE table_catalog=?" +
                "  AND table_schema='public'" +
                "  AND table_type='BASE TABLE'" +
                " UNION ALL" +
                "  SELECT 1" +
                "  FROM information_schema.sequences " +
                "  WHERE sequence_catalog=?" +
                "  AND sequence_schema='public'" +
                ")", name, name);
    }

    @Override
    protected void doCreate() throws SQLException {
        new CockroachDBRetryingStrategy().execute(new SqlCallable<Integer>() {
            @Override
            public Integer call() throws SQLException {
                doCreateOnce();
                return null;
            }
        });
    }

    protected void doCreateOnce() throws SQLException {
        jdbcTemplate.execute("CREATE DATABASE " + database.quote(name));
    }

    @Override
    protected void doDrop() throws SQLException {
        new CockroachDBRetryingStrategy().execute(new SqlCallable<Integer>() {
            @Override
            public Integer call() throws SQLException {
                doDropOnce();
                return null;
            }
        });
    }

    protected void doDropOnce() throws SQLException {
        jdbcTemplate.execute("DROP DATABASE " + database.quote(name));
    }

    @Override
    protected void doClean() throws SQLException {
        new CockroachDBRetryingStrategy().execute(new SqlCallable<Integer>() {
            @Override
            public Integer call() throws SQLException {
                doCleanOnce();
                return null;
            }
        });
    }

    protected void doCleanOnce() throws SQLException {
        for (String statement : generateDropStatementsForViews()) {
            jdbcTemplate.execute(statement);
        }

        for (Table table : allTables()) {
            table.drop();
        }

        for (String statement : generateDropStatementsForSequences()) {
            jdbcTemplate.execute(statement);
        }
    }

    /**
     * Generates the statements for dropping the views in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> generateDropStatementsForViews() throws SQLException {
        List<String> names =
                jdbcTemplate.queryForStringList(
                        "SELECT table_name FROM information_schema.views" +
                                " WHERE table_catalog=? AND table_schema='public'", name);
        List<String> statements = new ArrayList<>();
        for (String name : names) {
            statements.add("DROP VIEW IF EXISTS " + database.quote(this.name, name) + " CASCADE");
        }

        return statements;
    }

    /**
     * Generates the statements for dropping the sequences in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> generateDropStatementsForSequences() throws SQLException {
        List<String> names =
                jdbcTemplate.queryForStringList(
                        "SELECT sequence_name FROM information_schema.sequences" +
                                " WHERE sequence_catalog=? AND sequence_schema='public'", name);
        List<String> statements = new ArrayList<>();
        for (String name : names) {
            statements.add("DROP SEQUENCE IF EXISTS " + database.quote(this.name, name) + " CASCADE");
        }

        return statements;
    }

    @Override
    protected CockroachDBTable[] doAllTables() throws SQLException {
        String query;
        if (cockroachDB1) {
            query =
                    //Search for all the table names
                    "SELECT table_name FROM information_schema.tables" +
                            //in this schema
                            " WHERE table_schema=?" +
                            //that are real tables (as opposed to views)
                            " AND table_type='BASE TABLE'";
        } else {
            query =
                    //Search for all the table names
                    "SELECT table_name FROM information_schema.tables" +
                            //in this database
                            " WHERE table_catalog=?" +
                            " AND table_schema='public'" +
                            //that are real tables (as opposed to views)
                            " AND table_type='BASE TABLE'";
        }

        List<String> tableNames = jdbcTemplate.queryForStringList(query, name);
        //Views and child tables are excluded as they are dropped with the parent table when using cascade.

        CockroachDBTable[] tables = new CockroachDBTable[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new CockroachDBTable(jdbcTemplate, database, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    public Table getTable(String tableName) {
        return new CockroachDBTable(jdbcTemplate, database, this, tableName);
    }


}