/*-
 * ========================LICENSE_START=================================
 * flyway-commandline
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
package org.flywaydb.commandline.command.version;

import static org.flywaydb.core.internal.util.TelemetryUtils.getTelemetryManager;

import lombok.CustomLog;
import lombok.SneakyThrows;
import org.flywaydb.core.TelemetrySpan;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.OperationResult;
import org.flywaydb.core.extensibility.CommandExtension;
import org.flywaydb.core.extensibility.EventTelemetryModel;
import org.flywaydb.core.extensibility.LicenseGuard;
import org.flywaydb.core.extensibility.Plugin;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@CustomLog
public class VersionCommandExtension implements CommandExtension {
    public static final String VERSION = "version";
    public static final List<String> FLAGS = Arrays.asList("-v", "--version");

    @Override
    public boolean handlesCommand(String command) {
        return command.equals(VERSION);
    }

    public String getCommandForFlag(String flag) {
        if (FLAGS.contains(flag.toLowerCase())) {
            return VERSION;
        }
        return CommandExtension.super.getCommandForFlag(flag);
    }

    @Override
    public boolean handlesParameter(String parameter) {
        return false;
    }

    @Override
    @SneakyThrows
    public OperationResult handle(String command, Configuration config, List<String> flags) throws FlywayException {
        return TelemetrySpan.trackSpan(new EventTelemetryModel("version", getTelemetryManager(config)), (telemetryModel) -> {
            return version(command, config);
        });
    }

    private static VersionResult version(final String command, final Configuration config) {
        LOG.debug("Java " + System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")");
        LOG.debug(System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch") + "\n");

        List<Plugin> allPlugins = config.getPluginRegister().getPlugins(Plugin.class);

        List<PluginVersionResult> pluginVersions = allPlugins.stream()
            .map(p -> new PluginVersionResult(p.getName(), p.getPluginVersion(config), p.isLicensed(config)))
            .filter(p -> StringUtils.hasText(p.version))
            .collect(Collectors.toList());

        if (!pluginVersions.isEmpty()) {

            int nameLength = pluginVersions.stream().map(p -> p.name.length()).max(Integer::compare).get() + 2;
            int versionLength = pluginVersions.stream().map(p -> p.version.length()).max(Integer::compare).get() + 2;

            LOG.info(StringUtils.rightPad("Plugin Name", nameLength, ' ') + " | " + StringUtils.rightPad("Version", versionLength, ' '));

            LOG.info(StringUtils.rightPad(StringUtils.leftPad("", nameLength, '-'), nameLength, ' ') + " | " +
                StringUtils.rightPad(StringUtils.leftPad("", versionLength, '-'), versionLength, ' '));

            for (PluginVersionResult p : pluginVersions) {
                LOG.info(StringUtils.rightPad(p.name, nameLength, ' ') + " | " + StringUtils.rightPad(p.version, versionLength, ' '));
            }
        }

        return new VersionResult(VersionPrinter.getVersion(), command, LicenseGuard.getTier(config), pluginVersions);
    }

    @Override
    public List<Pair<String, String>> getUsage() {
        return Collections.singletonList(Pair.of(VERSION + ", " + String.join(", ", FLAGS), "Print the Flyway version and edition"));
    }
}
