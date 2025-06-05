/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.core.internal.sqlscript;

import lombok.CustomLog;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.extensibility.LicenseGuard;
import org.flywaydb.core.extensibility.Tier;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.license.FlywayEditionUpgradeRequiredException;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.PlaceholderReplacingReader;

import java.util.HashMap;
import java.util.Map;

import static org.flywaydb.core.internal.configuration.ConfigUtils.removeBoolean;
import static org.flywaydb.core.internal.util.BooleanEvaluator.evaluateExpression;

@CustomLog
public class SqlScriptMetadata {
    private static final String EXECUTE_IN_TRANSACTION = "executeInTransaction";
    private static final String ENCODING = "encoding";
    private static final String PLACEHOLDER_REPLACEMENT = "placeholderReplacement";
    private static final String SHOULD_EXECUTE = "shouldExecute";

    private final Boolean executeInTransaction;
    private final String encoding;
    private final Boolean placeholderReplacement;
    private String shouldExecuteExpression;
    private boolean shouldExecute;

    private SqlScriptMetadata(Map<String, String> metadata, Map<String, String> unmappedMetadata, Configuration config) {
        // Make copy to prevent removing elements from the original
        metadata = new HashMap<>(metadata);

        this.executeInTransaction = removeBoolean(metadata, EXECUTE_IN_TRANSACTION);
        this.encoding = metadata.remove(ENCODING);

        this.placeholderReplacement = removeBoolean(metadata, PLACEHOLDER_REPLACEMENT);
        metadata.remove(PLACEHOLDER_REPLACEMENT);

        this.shouldExecute = true;
        this.shouldExecuteExpression = null;








        {
            if (metadata.containsKey(SHOULD_EXECUTE)) {
                throw new FlywayEditionUpgradeRequiredException(LicenseGuard.getTier(config), "shouldExecute");
            }
        }
        ConfigUtils.checkConfigurationForUnrecognisedProperties(metadata, null);
    }

    public Boolean executeInTransaction() {
        return executeInTransaction;
    }

    public String encoding() {
        return encoding;
    }

    public Boolean placeholderReplacement() {
        return placeholderReplacement;
    }

    public boolean shouldExecute() {
        return shouldExecute;
    }

    public String shouldExecuteExpression() {
        return shouldExecuteExpression;
    }

    public static boolean isMultilineBooleanExpression(String line) {
        return !line.startsWith(SHOULD_EXECUTE) && (line.contains("==") || line.contains("!="));
    }

    public static SqlScriptMetadata fromResource(LoadableResource resource, Parser parser, Configuration config) {
        if (resource != null) {
            LOG.debug("Found script configuration: " + resource.getFilename());
            var unmappedMetadata = ConfigUtils.loadConfigurationFromReader(resource.read(), true);
            if (parser == null) {
                return new SqlScriptMetadata(unmappedMetadata, unmappedMetadata, config);
            }

            var mappedMetadata = ConfigUtils.loadConfigurationFromReader(
                PlaceholderReplacingReader.create(parser.configuration, parser.parsingContext, resource.read()));
            return new SqlScriptMetadata(mappedMetadata, unmappedMetadata, parser.configuration);
        }
        return new SqlScriptMetadata(new HashMap<>(), new HashMap<>(), config);
    }

    public static LoadableResource getMetadataResource(ResourceProvider resourceProvider, LoadableResource resource) {
        if (resourceProvider == null) {
            return null;
        }
        return resourceProvider.getResource(resource.getRelativePath() + ".conf");
    }
}
