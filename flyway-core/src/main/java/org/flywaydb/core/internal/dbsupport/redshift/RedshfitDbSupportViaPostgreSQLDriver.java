/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.dbsupport.redshift;

import java.sql.Connection;
import java.sql.Types;

import org.flywaydb.core.internal.dbsupport.JdbcTemplate;

public class RedshfitDbSupportViaPostgreSQLDriver extends RedshiftDbSupport {

    /**
     * Creates a new instance.
     *
     * @param connection The connection to use.
     */
    public RedshfitDbSupportViaPostgreSQLDriver(Connection connection) {
        super(new JdbcTemplate(connection, Types.NULL));
    }

}
