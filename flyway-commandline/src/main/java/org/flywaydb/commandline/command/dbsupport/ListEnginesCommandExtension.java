/*
 * Copyright (C) Red Gate Software Ltd 2010-2024
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
package org.flywaydb.commandline.command.dbsupport;

import lombok.CustomLog;
import lombok.SneakyThrows;
import org.flywaydb.core.FlywayTelemetryManager;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.OperationResult;
import org.flywaydb.core.extensibility.CommandExtension;
import org.flywaydb.core.extensibility.EventTelemetryModel;
import org.flywaydb.core.extensibility.LicenseGuard;
import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@CustomLog
public class ListEnginesCommandExtension implements CommandExtension {
    private static final String DB_SUPPORT = "list-engines";
    private static final String HEADERS_DATABASE_NAME = "Database Name";

    @Override
    public boolean handlesCommand(String command) {
        return command.equals(DB_SUPPORT);
    }

    @Override
    public boolean handlesParameter(String parameter) {
        return false;
    }

    @Override
    @SneakyThrows
    public OperationResult handle(String command, Configuration config, List<String> flags,
        FlywayTelemetryManager flywayTelemetryManager) throws FlywayException {
        try (EventTelemetryModel telemetryModel = new EventTelemetryModel(DB_SUPPORT, flywayTelemetryManager)) {
            try {
                List<DatabaseType> allPlugins = config.getPluginRegister().getPlugins(DatabaseType.class);

                List<DbInfoResult> databaseInfos = allPlugins.stream()
                    .map(p -> new DbInfoResult(p.getName()))
                    .collect(Collectors.toList());

                if (!databaseInfos.isEmpty()) {

                    int nameLength = databaseInfos.stream().map(p -> p.name().length()).max(Integer::compare).get() + 2;

                    if (nameLength < HEADERS_DATABASE_NAME.length() + 2) {
                        nameLength = HEADERS_DATABASE_NAME.length() + 2;
                    }

                    LOG.info(StringUtils.rightPad(HEADERS_DATABASE_NAME, nameLength, ' '));
                    LOG.info(StringUtils.rightPad(StringUtils.leftPad("", nameLength, '-'), nameLength, ' '));

                    for (DbInfoResult p : databaseInfos) {
                        LOG.info(StringUtils.rightPad(p.name(), nameLength, ' '));
                    }
                }

                return new DbSupportResult(VersionPrinter.getVersion(), command, LicenseGuard.getTier(config),
                    databaseInfos);
            } catch (Exception e) {
                telemetryModel.setException(e);
                throw e;
            }
        }
    }

    @Override
    public List<Pair<String, String>> getUsage() {
        return List.of(Pair.of(DB_SUPPORT, "Lists the database engines that Flyway has loaded support for."));
    }
}