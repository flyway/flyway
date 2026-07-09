/*-
 * ========================LICENSE_START=================================
 * flyway-commandline
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
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

import java.util.Date;
import java.util.Locale;
import lombok.CustomLog;
import lombok.SneakyThrows;
import org.flywaydb.core.TelemetrySpan;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.CommandExtension;
import org.flywaydb.core.extensibility.EventTelemetryModel;
import org.flywaydb.core.extensibility.LicenseGuard;
import org.flywaydb.core.extensibility.VersionReportable;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.core.internal.util.DateUtils;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@CustomLog
public class VersionCommandExtension implements CommandExtension<VersionResult> {
    public static final String VERSION = "version";
    public static final List<String> FLAGS = Arrays.asList("-v", "--version");

    @Override
    public boolean handlesCommand(final String command) {
        return command.equals(VERSION);
    }

    public String getCommandForFlag(final String flag) {
        if (FLAGS.contains(flag.toLowerCase(Locale.ROOT))) {
            return VERSION;
        }
        return CommandExtension.super.getCommandForFlag(flag);
    }

    @Override
    public boolean requiresFlywayInstance() {
        return false;
    }

    @Override
    public boolean handlesParameter(final String parameter) {
        return false;
    }

    @Override
    @SneakyThrows
    public VersionResult handle(final Configuration config, final List<String> flags) throws FlywayException {
        return TelemetrySpan.trackSpan(new EventTelemetryModel("version", getTelemetryManager(config)),
            (telemetryModel) -> version(VERSION.toLowerCase(Locale.ROOT), config));
    }

    private static VersionResult version(final String command, final Configuration config) {
        LOG.debug("Java " + System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")");
        LOG.debug(System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty(
            "os.arch") + "\n");

        final List<VersionReportable> versionedPlugins = config.getPluginRegister()
            .getInstancesOf(VersionReportable.class);

        final List<PluginVersionResult> pluginVersions = versionedPlugins.stream()
            .map(p -> new PluginVersionResult(p.getName(), p.getPluginVersion(config), p.isLicensed(config)))
            .filter(p -> StringUtils.hasText(p.version))
            .collect(Collectors.toList());

        if (!pluginVersions.isEmpty()) {

            final int nameLength = pluginVersions.stream().map(p -> p.name.length()).max(Integer::compare).get() + 2;
            final int versionLength = pluginVersions.stream().map(p -> p.version.length()).max(Integer::compare).get()
                + 2;

            LOG.info(StringUtils.rightPad("Plugin Name", nameLength, ' ') + " | " + StringUtils.rightPad("Version",
                versionLength,
                ' '));

            LOG.info(StringUtils.rightPad(StringUtils.leftPad("", nameLength, '-'), nameLength, ' ')
                + " | "
                + StringUtils.rightPad(StringUtils.leftPad("", versionLength, '-'), versionLength, ' '));

            for (final PluginVersionResult p : pluginVersions) {
                LOG.info(StringUtils.rightPad(p.name, nameLength, ' ') + " | " + StringUtils.rightPad(p.version,
                    versionLength,
                    ' '));
            }
        }

        final Date permitExpiry = LicenseGuard.getPermit(config).getPermitExpiry();
        return new VersionResult(VersionPrinter.getVersion(),
            command,
            LicenseGuard.getTier(config),
            pluginVersions,
            permitExpiry == null ? null : DateUtils.toDateString(permitExpiry));
    }

    @Override
    public List<Pair<String, String>> getUsage() {
        return Collections.singletonList(Pair.of(VERSION + ", " + String.join(", ", FLAGS),
            "Print the Flyway version and edition"));
    }
}
