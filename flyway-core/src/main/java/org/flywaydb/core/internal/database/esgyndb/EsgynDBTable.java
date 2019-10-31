/*
 * Copyright 2010-2019 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.esgyndb;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;

/**
 * EsgynDB-specific table.
 */
public class EsgynDBTable extends Table<EsgynDBDatabase, EsgynDBSchema> {
    private static final Log LOG = LogFactory.getLog(EsgynDBTable.class);

	/**
     * Creates a new EsgynDB table.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param schema       The schema this table lives in.
     * @param name         The name of the table.
     */
    public EsgynDBTable(JdbcTemplate jdbcTemplate, EsgynDBDatabase database, EsgynDBSchema schema, String name) {
        super(jdbcTemplate, database, schema, name);
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP TABLE TRAFODION." + database.quote(schema.getName(), name));
    }

    /**
     * Checks whether this table exists.
     *
     * @return {@code true} if it does, {@code false} if not.
     * @throws SQLException when the check failed.
     */
    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForBoolean(
        		" SELECT CASE COUNT(*) WHEN 0 THEN FALSE ELSE TRUE END\n" +
                "   FROM \"_MD_\".OBJECTS\n" +
				"  WHERE CATALOG_NAME = 'TRAFODION'\n" +
                "    AND OBJECT_TYPE = 'BT'\n" +
                "    AND SCHEMA_NAME = ?\n" +
                "    AND OBJECT_NAME = ?", schema.getName(), name);
    }

    /**
     * Locks this table in this schema using a read/write pessimistic lock until the end of the current transaction.
     *
     * @throws SQLException when this table in this schema could not be locked.
     */
    @Override
    protected void doLock() {
        LOG.debug("Unable to lock " + this + " as EsgynDB does not support table locking. No concurrent migration supported.");
    }
}