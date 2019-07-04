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
package org.flywaydb.core.internal.database.cloudspanner;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;

/**
 * Google Cloud Spanner connection.
 */
public class CloudSpannerConnection extends Connection<CloudSpannerDatabase> {
    private static final Log LOG = LogFactory.getLog(CloudSpannerConnection.class);
    
    /**
     * Whether the warning message has already been printed.
     */
    private static boolean schemaMessagePrinted;

    CloudSpannerConnection(CloudSpannerDatabase database, java.sql.Connection connection) {
        super(database, connection);
    }


    @Override
    protected String getCurrentSchemaNameOrSearchPath() {
        return "";
    }

    @Override
    public void doChangeCurrentSchemaOrSearchPathTo(String schema) {
        if (!schemaMessagePrinted) {
            LOG.info("Google Cloud Spanner does not support setting the schema. Default schema NOT changed to " + schema);
            schemaMessagePrinted = true;
        }
    }

    @Override
    public Schema<CloudSpannerDatabase, CloudSpannerTable> getSchema(String name) {
        return new CloudSpannerSchema(jdbcTemplate, database, name);
    }
}