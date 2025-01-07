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
package org.flywaydb.core;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import lombok.Getter;
import lombok.experimental.ExtensionMethod;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.extensibility.EventTelemetryModel;
import org.flywaydb.core.extensibility.RootTelemetryModel;
import org.flywaydb.core.extensibility.TelemetryPlugin;
import org.flywaydb.core.internal.plugin.PluginRegister;
import org.flywaydb.core.internal.util.FileUtils;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.List;
import java.util.UUID;

@ExtensionMethod(StringUtils.class)
public class FlywayTelemetryManager implements AutoCloseable{
    private final PluginRegister pluginRegister;
    private final Future<List<TelemetryPlugin>> initialized;
    private Future<? extends RootTelemetryModel> rootTelemetryModelFuture;
    private final Collection<Future<Void>> eventFutures = new ConcurrentLinkedDeque<>();

    @Getter
    private RootTelemetryModel rootTelemetryModel = new RootTelemetryModel();
    
    public FlywayTelemetryManager(final PluginRegister pluginRegister) {
        this.pluginRegister = pluginRegister;
        initialized = CompletableFuture.supplyAsync(this::initialize);
    }
    
    public void setRootTelemetryModel(final Future<? extends RootTelemetryModel> rootTelemetryModel) {
        rootTelemetryModelFuture = rootTelemetryModel;
        CompletableFuture.runAsync(() -> {
            try {
                this.rootTelemetryModel = rootTelemetryModel.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new FlywayException(e);
            }
        });
    }

    private List<TelemetryPlugin> initialize() {
        final List<TelemetryPlugin> telemetryPlugins = this.pluginRegister.getPlugins(TelemetryPlugin.class);

        for(final TelemetryPlugin telemetryPlugin : telemetryPlugins){
            telemetryPlugin.logRootDetails(rootTelemetryModel);
        }

        String userId = System.getenv("RG_TELEMETRY_ANONYMOUS_USER_ID");
        if(!userId.hasText()) {
            userId = FileUtils.readUserIdFromFileIfNoneWriteDefault();
        }

        rootTelemetryModel.setUserId(userId);
        String sessionId = System.getenv("RG_TELEMETRY_SESSION_ID");
        if(!sessionId.hasText()) {
            sessionId = UUID.randomUUID().toString();
        }
        rootTelemetryModel.setSessionId(sessionId);

        String operationId = System.getenv("RG_TELEMETRY_OPERATION_ID");
        if(!operationId.hasText()) {
            operationId = UUID.randomUUID().toString();
        }
        rootTelemetryModel.setOperationId(operationId);
        
        return telemetryPlugins;
    }

    public void logEvent(final EventTelemetryModel model) {
        eventFutures.add(CompletableFuture.runAsync(() -> {
            try {
                final List<TelemetryPlugin> telemetryPlugins = initialized.get();
                if (rootTelemetryModelFuture != null) {
                    rootTelemetryModelFuture.get();
                }

                for (final TelemetryPlugin telemetryPlugin : telemetryPlugins) {
                    telemetryPlugin.logEventDetails(model);
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new FlywayException(e);
            }
        }));
    }
    @Override
    public void close() throws FlywayException {
        CompletableFuture.runAsync(() -> {
            try {
                final List<TelemetryPlugin> telemetryPlugins = initialized.get();
                if (rootTelemetryModelFuture != null) {
                    rootTelemetryModelFuture.get();
                }
                for(final Future<Void> eventFuture : eventFutures){
                    eventFuture.get();
                }

                for (final TelemetryPlugin telemetryPlugin : telemetryPlugins) {
                    telemetryPlugin.close();
                }
            } catch (final Exception e) {
                throw new FlywayException(e);
            }
        });
    }
}
