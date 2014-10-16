/**
 * Copyright 2014 Bertrand DONNET
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
package org.flywaydb.core.internal.dbsupport.db2as400;

import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.db2.DB2DbSupport;

import java.sql.Connection;

/**
 * DB2/AS400 Support.
 */
public class DB2as400DbSupport extends DB2DbSupport {


    public DB2as400DbSupport(Connection connection) {
        super(connection);
    }

    @Override
    public String getDbName() {
        return "db2as400";
    }

    @Override
    public String getCurrentUserFunction() {
    	return "USER";
    }

    @Override
    public Schema getSchema(String name) {
        return new DB2as400Schema(jdbcTemplate, this, name);
    }
}
