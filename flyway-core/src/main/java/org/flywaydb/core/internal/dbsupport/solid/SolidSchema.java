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

import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SolidSchema extends Schema<SolidDbSupport> {

    public SolidSchema(final JdbcTemplate jdbcTemplate, final SolidDbSupport dbSupport, final String name) {
<<<<<<< HEAD
        super(jdbcTemplate, dbSupport, name.toUpperCase());
=======
        super(jdbcTemplate, dbSupport, name);
>>>>>>> Initial commit for SolidDB support (#885)
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM _SYSTEM.SYS_SCHEMAS WHERE NAME = ?", name) > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        int count = jdbcTemplate.queryForInt("SELECT COUNT(*) FROM _SYSTEM.SYS_TABLES WHERE TABLE_SCHEMA = ?", name);
        if (count > 0) {
<<<<<<< HEAD
            // This count includes regular tables and views
=======
>>>>>>> Initial commit for SolidDB support (#885)
            return false;
        }
        count = jdbcTemplate.queryForInt("SELECT COUNT(*) FROM _SYSTEM.SYS_TRIGGERS WHERE TRIGGER_SCHEMA = ?", name);
        if (count > 0) {
            return false;
        }
        count = jdbcTemplate.queryForInt("SELECT COUNT(*) FROM _SYSTEM.SYS_PROCEDURES WHERE PROCEDURE_SCHEMA = ?",
                                         name);
        if (count > 0) {
            return false;
        }
        count = jdbcTemplate.queryForInt("SELECT COUNT(*) FROM _SYSTEM.SYS_FORKEYS WHERE KEY_SCHEMA = ?", name);
        if (count > 0) {
            return false;
        }
<<<<<<< HEAD
        //TODO: Query also for possible other items
=======
        //TODO: Query also for views, etc.
>>>>>>> Initial commit for SolidDB support (#885)

        return true;
    }

    @Override
    protected void doCreate() throws SQLException {
<<<<<<< HEAD
        jdbcTemplate.execute("CREATE SCHEMA " + name);
=======
        jdbcTemplate.execute("CREATE SCHEMA ?", name);
>>>>>>> Initial commit for SolidDB support (#885)
    }

    @Override
    protected void doDrop() throws SQLException {
<<<<<<< HEAD
        clean();
        jdbcTemplate.execute("DROP SCHEMA " + name);
=======
        jdbcTemplate.execute("DROP SCHEMA ?", name);
>>>>>>> Initial commit for SolidDB support (#885)
    }

    @Override
    protected void doClean() throws SQLException {
        for (final String statement : dropTriggers()) {
            jdbcTemplate.execute(statement);
        }
        for (final String statement : dropProcedures()) {
            jdbcTemplate.execute(statement);
        }
        for (final String statement : dropConstraints()) {
            jdbcTemplate.execute(statement);
        }
<<<<<<< HEAD
        for (final String statement : dropViews()) {
            jdbcTemplate.execute(statement);
        }
        //TODO: drop maybe other related stuff
=======
        //TODO: drop views (and maybe other related stuff)
>>>>>>> Initial commit for SolidDB support (#885)

        for (final Table table : allTables()) {
            table.drop();
        }
    }

    @Override
    protected Table[] doAllTables() throws SQLException {
        final List<String> tableNames = jdbcTemplate.queryForStringList(
<<<<<<< HEAD
                "SELECT TABLE_NAME FROM _SYSTEM.SYS_TABLES WHERE TABLE_SCHEMA = ? AND TABLE_TYPE = 'BASE TABLE'", name);
=======
                "SELECT TABLE_NAME FROM _SYSTEM.SYS_TABLES WHERE TABLE_SCHEMA = ? AND TABLE_TYPE = 'BASE_TABLE'", name);
>>>>>>> Initial commit for SolidDB support (#885)

        final Table[] tables = new Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new SolidTable(jdbcTemplate, dbSupport, this, tableNames.get(i));
        }

        return tables;
    }

    @Override
    public Table getTable(final String tableName) {
<<<<<<< HEAD
        return new SolidTable(jdbcTemplate, dbSupport, this, tableName);
=======
        return null;
>>>>>>> Initial commit for SolidDB support (#885)
    }

    private Iterable<String> dropTriggers() throws SQLException {
        final List<String> statements = new ArrayList<String>();

        for (final Map<String, String> item : jdbcTemplate.queryForList(
                "SELECT TRIGGER_NAME FROM _SYSTEM.SYS_TRIGGERS WHERE TRIGGER_SCHEMA = ?", name)) {
            statements.add("DROP TRIGGER " + dbSupport.quote(name, item.get("TRIGGER_NAME")));
        }

        return statements;
    }

    private Iterable<String> dropProcedures() throws SQLException {
        final List<String> statements = new ArrayList<String>();

        for (final Map<String, String> item : jdbcTemplate.queryForList(
                "SELECT PROCEDURE_NAME FROM _SYSTEM.SYS_PROCEDURES WHERE PROCEDURE_SCHEMA = ?", name)) {
            statements.add("DROP PROCEDURE " + dbSupport.quote(name, item.get("PROCEDURE_NAME")));
        }

        return statements;
    }

    private Iterable<String> dropConstraints() throws SQLException {
        final List<String> statements = new ArrayList<String>();

        for (final Map<String, String> item : jdbcTemplate.queryForList(
<<<<<<< HEAD
                "SELECT TABLE_NAME, KEY_NAME FROM _SYSTEM.SYS_FORKEYS, _SYSTEM.SYS_TABLES " +
                        "WHERE SYS_FORKEYS.KEY_SCHEMA = ? " +
                        "AND SYS_FORKEYS.CREATE_REL_ID = SYS_FORKEYS.REF_REL_ID " +
                        "AND SYS_FORKEYS.CREATE_REL_ID = SYS_TABLES.ID", name)) {
=======
                "SELECT TABLE_NAME, KEY_NAME FROM _SYSTEM.SYS_FORKEYS, _SYSTEM.SYS_TABLES WHERE SYS_FORKEYS.KEY_SCHEMA = ? AND SYS_FORKEYS.CREATE_REL_ID = SYS_TABLES.ID",
                name)) {
>>>>>>> Initial commit for SolidDB support (#885)
            statements.add("ALTER TABLE " +
                                   dbSupport.quote(name, item.get("TABLE_NAME")) +
                                   " DROP CONSTRAINT " + dbSupport.quote(item.get("KEY_NAME")));
        }

        return statements;
    }
<<<<<<< HEAD

    private Iterable<String> dropViews() throws SQLException {
        final List<String> statements = new ArrayList<String>();

        for (final Map<String, String> item : jdbcTemplate.queryForList(
                "SELECT TABLE_NAME FROM _SYSTEM.SYS_TABLES WHERE TABLE_TYPE = 'VIEW' AND TABLE_SCHEMA = ?", name)) {
            statements.add("DROP VIEW " + dbSupport.quote(name, item.get("TABLE_NAME")));
        }

        return statements;
    }

    private void commitWork() throws SQLException {
        jdbcTemplate.executeStatement("COMMIT WORK");
    }
=======
>>>>>>> Initial commit for SolidDB support (#885)
}
