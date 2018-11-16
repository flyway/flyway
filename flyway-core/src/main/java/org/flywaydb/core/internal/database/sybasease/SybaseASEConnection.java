/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.sybasease;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;

/**
 * Sybase ASE Connection.
 */
public class SybaseASEConnection extends Connection<SybaseASEDatabase> {
    SybaseASEConnection(Configuration configuration, SybaseASEDatabase database, java.sql.Connection connection
            , boolean originalAutoCommit



    ) {
        super(configuration, database, connection, originalAutoCommit



        );
    }


    @Override
    public Schema getSchema(String name) {
        //Sybase does not support schemas, nor changing users on the fly. Always return the same dummy schema.
        return new SybaseASESchema(jdbcTemplate, database, "dbo");
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() {
        return "dbo";
    }
}