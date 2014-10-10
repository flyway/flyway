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

import org.flywaydb.core.internal.dbsupport.*;
import org.flywaydb.core.internal.util.jdbc.RowMapper;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Firebird-specific JdbcTemplate which commits automatically after all possible data-changing operations. This
 * behaviour is currently necessary because Firebird does not perform implicit commits after DDL operations within a
 * single transaction.
 *
 */
public class FirebirdJdbcTemplate extends JdbcTemplate {

    private static final Log LOG = LogFactory.getLog(FirebirdJdbcTemplate.class);

    /**
     * Creates a new Firebird-specific JdbcTemplate.
     *
     * @param connection The DB connection to use.
     * @param nullType The type to assign to a null value.
     */
    public FirebirdJdbcTemplate(Connection connection, int nullType) {
        super(connection, nullType);
    }

    /**
     * {@inheritDoc}
     * <b>Attention! this method also commits!</b>
     */
    @Override
    public void execute(String sql, Object... params) throws SQLException {
        super.execute(sql, params);
        fakeAutoCommit();
    }

    /**
     * {@inheritDoc}
     * <b>Attention! this method also commits!</b>
     */
    @Override
    public void executeStatement(String sql) throws SQLException {
        super.executeStatement(sql);
        fakeAutoCommit();
    }

    /**
     * {@inheritDoc}
     * <b>Attention! this method also commits!</b>
     */
    @Override
    public void update(String sql, Object... params) throws SQLException {
        super.update(sql, params);
        fakeAutoCommit();
    }

    /**
     * {@inheritDoc}
     * <b>Attention! this method also commits!</b>
     */
    @Override
    public <T> List<T> query(String query, RowMapper<T> rowMapper) throws SQLException {
        List<T> ret = super.query(query, rowMapper);
        fakeAutoCommit();
        return ret;
    }

    private void fakeAutoCommit() throws SQLException {
        if (!getConnection().getAutoCommit()) {
            getConnection().commit();
        }
    }

}
