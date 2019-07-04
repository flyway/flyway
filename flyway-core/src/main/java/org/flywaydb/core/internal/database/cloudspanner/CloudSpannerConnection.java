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
package org.flywaydb.core.internal.database.cloudspanner;

import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.Connection;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.database.Table;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * Google Cloud Spanner connection.
 */
public class CloudSpannerConnection extends Connection<CloudSpannerDatabase> {
    private static final Log LOG = LogFactory.getLog(CloudSpannerConnection.class);
    
    /**
     * Whether the warning message has already been printed.
     */
    private static boolean schemaMessagePrinted;

    CloudSpannerConnection(FlywayConfiguration configuration, CloudSpannerDatabase database, java.sql.Connection connection, int nullType



    ) {
        super(configuration, database, connection, nullType



        );
    }


    @Override
    protected String doGetCurrentSchemaName() throws SQLException {
        return jdbcTemplate.getConnection().getSchema();
    }

    @Override
    public void doChangeCurrentSchemaTo(String schema) throws SQLException {
        if (!schemaMessagePrinted) {
            LOG.info("Google Cloud Spanner does not support setting the schema. Default schema NOT changed to " + schema);
            schemaMessagePrinted = true;
        }
    }

    @Override
    public Schema getSchema(String name) {
        return new CloudSpannerSchema(jdbcTemplate, database, name);
    }
}