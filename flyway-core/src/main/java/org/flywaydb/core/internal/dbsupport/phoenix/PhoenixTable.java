/**
 * Copyright 2010-2015 Axel Fontaine
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
package org.flywaydb.core.internal.dbsupport.phoenix;

import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

import java.sql.SQLException;

/**
 * Phoenix-specific table.
 */
public class PhoenixTable extends Table {
    private static final Log LOG = LogFactory.getLog(PhoenixTable.class);
    /**
     * Creates a new Phoenix table.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param dbSupport    The database-specific support.
     * @param schema       The schema this table lives in.
     * @param name         The name of the table.
     */
    public PhoenixTable(JdbcTemplate jdbcTemplate, DbSupport dbSupport, Schema schema, String name) {
        super(jdbcTemplate, dbSupport, schema, name);
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP TABLE " + dbSupport.quote(schema.getName(), name));
    }

    @Override
    protected boolean doExists() throws SQLException {
        if(schema.getName() == null) {
            return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM SYSTEM.CATALOG WHERE TABLE_NAME = ? and TABLE_SCHEM IS NULL AND TABLE_TYPE = 'u'", name) > 0;
        }
        else {
            return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM SYSTEM.CATALOG WHERE TABLE_NAME = ? and TABLE_SCHEM = ? AND TABLE_TYPE = 'u'", name, schema.getName()) > 0;
        }
    }

    @Override
    protected void doLock() throws SQLException {
        LOG.debug("Unable to lock " + this + " as Phoenix does not support locking. No concurrent migration supported.");
    }
}
