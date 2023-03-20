/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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
package org.flywaydb.core;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.extensibility.EventTelemetryModel;
import org.flywaydb.core.extensibility.RootTelemetryModel;
import org.flywaydb.core.extensibility.TelemetryPlugin;
import org.flywaydb.core.internal.plugin.PluginRegister;
import org.flywaydb.core.internal.util.FileUtils;
import org.flywaydb.core.internal.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@ExtensionMethod(StringUtils.class)
public class FlywayTelemetryManager implements AutoCloseable{
    private PluginRegister pluginRegister;

    @Getter
    @Setter
    private RootTelemetryModel rootTelemetryModel = new RootTelemetryModel();
    public FlywayTelemetryManager(PluginRegister pluginRegister){
        this.pluginRegister = pluginRegister;

        List<TelemetryPlugin> telemetryPlugins = pluginRegister.getPlugins(TelemetryPlugin.class);

        for(TelemetryPlugin telemetryPlugin : telemetryPlugins){
            telemetryPlugin.logRootDetails(rootTelemetryModel);
        }

        String userId = System.getenv("RG_TELEMETRY_ANONYMOUS_USER_ID");
        if(!userId.hasText()) {
            boolean isWindows = System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win");
            File redgateAppData;
            if (isWindows) {
                redgateAppData = new File(System.getenv("APPDATA"), "Redgate");
            } else {
                redgateAppData = new File(System.getProperty("user.home"), ".config/Redgate");
            }
            File userIdFile = new File(redgateAppData, "feature_usage_data");
            if (userIdFile.exists()) {
                userId = FileUtils.readAsString(userIdFile.toPath());
            }
            if(!userId.hasText()) {
                userId = UUID.randomUUID().toString();
                if(!redgateAppData.exists()) {
                    redgateAppData.mkdirs();
                }
                try(FileWriter fileWriter = new FileWriter(userIdFile)) {
                    fileWriter.write(userId);
                } catch (IOException ignore) {
                    System.err.println(ignore.getMessage());
                }
            }
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
    }

    public void LogEvent(EventTelemetryModel model) {
        List<TelemetryPlugin> telemetryPlugins = pluginRegister.getPlugins(TelemetryPlugin.class);

        for(TelemetryPlugin telemetryPlugin : telemetryPlugins){
            telemetryPlugin.logEventDetails(model);
        }
    }
    @Override
    public void close() throws Exception {
        List<TelemetryPlugin> telemetryPlugins = pluginRegister.getPlugins(TelemetryPlugin.class);
        for(TelemetryPlugin telemetryPlugin : telemetryPlugins){
            telemetryPlugin.close();
        }
    }
}