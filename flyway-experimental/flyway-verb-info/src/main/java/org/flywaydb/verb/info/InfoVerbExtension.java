/*-
 * ========================LICENSE_START=================================
 * flyway-verb-info
 * ========================================================================
 * Copyright (C) 2010 - 2024 Red Gate Software Ltd
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
package org.flywaydb.verb.info;

import java.sql.SQLException;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.experimental.ExperimentalDatabasePluginResolverImpl;
import org.flywaydb.core.extensibility.VerbExtension;
import org.flywaydb.core.internal.configuration.models.ResolvedEnvironment;
import org.flywaydb.core.internal.configuration.resolvers.ProvisionerMode;
import org.flywaydb.core.internal.util.StringUtils;

public class InfoVerbExtension implements VerbExtension {
    @Override
    public boolean handlesVerb(final String verb) {
        return "info".equals(verb);
    }

    @Override
    public Object executeVerb(final Configuration configuration) {
        System.out.println("InfoVerbExtension.executeVerb");

        final var experimentalDatabasePluginResolver = new ExperimentalDatabasePluginResolverImpl(configuration.getPluginRegister());
        final var resolvedExperimentalDatabase = experimentalDatabasePluginResolver.resolve(configuration.getUrl());
        if (resolvedExperimentalDatabase.isEmpty()) {
            return null;
        }
        final var experimentalDatabase = resolvedExperimentalDatabase.get();

        try {
            experimentalDatabase.initialize(getResolvedEnvironment(configuration));
            final var schemaHistoryModel = experimentalDatabase.getSchemaHistoryModel(configuration.getTable());
            throw new FlywayException("Found " + schemaHistoryModel.getSchemaHistoryItems().size() + " migrations");
        } catch (final SQLException e) {
            throw new FlywayException(e);
        }            
    }
    
    private ResolvedEnvironment getResolvedEnvironment(final Configuration configuration) {
        final var envName =  configuration.getCurrentEnvironmentName();
        final var envProvisionMode = configuration.getModernConfig().getFlyway().getEnvironmentProvisionMode();
        final var provisionerMode = StringUtils.hasText(envProvisionMode) ? ProvisionerMode.fromString(envProvisionMode) : ProvisionerMode.Provision;
        final var resolved = configuration.getResolvedEnvironment(envName, provisionerMode, null);
        if (resolved == null) {
            throw new FlywayException("Environment '" + envName + "' not found. Check that this environment exists in your configuration.");
        }
        return resolved;
    }
}
