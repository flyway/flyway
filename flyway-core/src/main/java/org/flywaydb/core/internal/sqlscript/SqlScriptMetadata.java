/*
 * Copyright 2010-2020 Redgate Software Ltd
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
package org.flywaydb.core.internal.sqlscript;

import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.resource.LoadableResource;

import java.util.HashMap;
import java.util.Map;

import static org.flywaydb.core.internal.configuration.ConfigUtils.removeBoolean;

public class SqlScriptMetadata {
    private static final Log LOG = LogFactory.getLog(SqlScriptMetadata.class);
    private static final String EXECUTE_IN_TRANSACTION = "executeInTransaction";
    private static final String ENCODING = "encoding";

    private final Boolean executeInTransaction;
    private final String encoding;


    private SqlScriptMetadata(Map<String, String> metadata) {
        // Make copy to prevent removing elements from the original
        metadata = new HashMap<>(metadata);
        this.executeInTransaction = removeBoolean(metadata, EXECUTE_IN_TRANSACTION);
        this.encoding = metadata.remove(ENCODING);

        ConfigUtils.checkConfigurationForUnrecognisedProperties(metadata, null);
    }

    public Boolean executeInTransaction() {
        return executeInTransaction;
    }

    public String encoding() { return encoding; }

    public static SqlScriptMetadata fromResource(LoadableResource resource) {
        if (resource != null) {
            LOG.debug("Found script configuration: " + resource.getFilename());
            return new SqlScriptMetadata(ConfigUtils.loadConfigurationFromReader(resource.read()));
        }
        return new SqlScriptMetadata(new HashMap<>());
    }

    public static LoadableResource getMetadataResource(ResourceProvider resourceProvider, LoadableResource resource) {
        if (resourceProvider == null) {
            return null;
        }
        return resourceProvider.getResource(resource.getRelativePath() + ".conf");
    }
}