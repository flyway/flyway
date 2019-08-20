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

import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Function;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;

/**
 * EsgynDB-specific function.
 */
public class EsgynDBFunction extends Function {
    /**
     * Creates a new EsgynDB function.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param schema       The schema this function lives in.
     * @param name         The name of the function.
     * @param args         The arguments of the function.
     */
    EsgynDBFunction(JdbcTemplate jdbcTemplate, Database database, Schema schema, String name, String... args) {
        super(jdbcTemplate, database, schema, name, args);
    }

    @Override
    protected void doDrop() throws SQLException {
/*        jdbcTemplate.execute("DROP FUNCTION TRAFODION." + database.quote(schema.getName(), name)); */
    }
}