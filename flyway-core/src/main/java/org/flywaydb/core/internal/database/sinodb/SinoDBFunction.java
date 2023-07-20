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

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.dbsupport.Function;
import com.googlecode.flyway.core.dbsupport.JdbcTemplate;
import com.googlecode.flyway.core.dbsupport.Schema;
import com.googlecode.flyway.core.util.StringUtils;
import java.sql.SQLException;

public class SinoDBFunction extends Function {
    public SinoDBFunction(JdbcTemplate jdbcTemplate, DbSupport dbSupport, Schema schema, String name, String... args) {
        super(jdbcTemplate, dbSupport, schema, name, args);
    }

    protected void doDrop() throws SQLException {
        this.jdbcTemplate.execute("DROP FUNCTION " + this.dbSupport.quote(new String[]{this.schema.getName(), this.name}) + "(" + StringUtils.arrayToCommaDelimitedString(this.args) + ")", new Object[0]);
    }

    public String toString() {
        return super.toString() + "(" + StringUtils.arrayToCommaDelimitedString(this.args) + ")";
    }
}
