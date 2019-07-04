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

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;

import java.sql.SQLException;

/**
 * Google Cloud Spanner connection.
 */
public class CloudSpannerConnection extends Connection<CloudSpannerDatabase> {
    private static final Log LOG = LogFactory.getLog(CloudSpannerConnection.class);
    
    /**
     * Whether the warning message has already been printed.
     */
    private static boolean schemaMessagePrinted;

    CloudSpannerConnection(Configuration configuration, CloudSpannerDatabase database, java.sql.Connection connection, boolean originalAutoCommit, int nullType



    ) {
        super(configuration, database, connection, originalAutoCommit, nullType



        );
    }


    @Override
    protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
        return "";
    }

    @Override
    public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
        if (!schemaMessagePrinted) {
            LOG.info("Google Cloud Spanner does not support setting the schema. Default schema NOT changed to " + schema);
            schemaMessagePrinted = true;
        }
    }

    @Override
    public Schema<CloudSpannerDatabase> getSchema(String name) {
        return new CloudSpannerSchema(jdbcTemplate, database, name);
    }
}