/*
 * Copyright (C) Red Gate Software Ltd 2010-2023
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
package  org.flywaydb.core.internal.database.sinodb;

import com.googlecode.flyway.core.api.FlywayException;
import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.dbsupport.JdbcTemplate;
import com.googlecode.flyway.core.dbsupport.Schema;
import com.googlecode.flyway.core.dbsupport.Table;
import java.sql.SQLException;

public class SinoDBTable extends Table {
    public SinoDBTable(JdbcTemplate jdbcTemplate, DbSupport dbSupport, Schema schema, String name) {
        super(jdbcTemplate, dbSupport, schema, name);

        try {
            jdbcTemplate.execute("SET LOCK MODE TO WAIT", new Object[0]);
        } catch (SQLException var6) {
            throw new FlywayException("Can not Set Lock Mode!", var6);
        }
    }

    protected void doDrop() throws SQLException {
        this.jdbcTemplate.execute("DROP TABLE " + this.dbSupport.quote(new String[]{this.name}), new Object[0]);
    }

    protected boolean doExists() throws SQLException {
        return this.exists((Schema)null, this.schema, this.name, new String[0]);
    }

    protected boolean doExistsNoQuotes() throws SQLException {
        return this.exists((Schema)null, this.dbSupport.getSchema(this.schema.getName().toUpperCase()), this.name.toUpperCase(), new String[0]);
    }

    protected void doLock() throws SQLException {
        this.jdbcTemplate.update("LOCK TABLE " + this + " IN EXCLUSIVE MODE", new Object[0]);
    }
}