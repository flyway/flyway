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
package org.flywaydb.core.internal.license;

import lombok.CustomLog;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.extensibility.PluginMetadata;
import org.flywaydb.core.internal.plugin.PluginRegister;
import org.flywaydb.core.internal.util.FileCopyUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@CustomLog
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VersionPrinter {
    public static final String VERSION = readVersion();
    public static Edition EDITION =

            Edition.COMMUNITY




            ;

    public static String getVersion() {
        return VERSION;
    }

    public static void printVersion() {
        printVersionOnly();
    }

    public static void printVersionOnly() {
        LOG.info(EDITION + " " + VERSION + " by Redgate");
        printExtensionVersions();
    }

    private static void printExtensionVersions() {
        for (PluginMetadata plugin : PluginRegister.getPlugins(PluginMetadata.class)) {
            LOG.debug(">\t" + plugin.getDescription());
        }
    }

    private static String readVersion() {
        try {
            return FileCopyUtils.copyToString(
                    VersionPrinter.class.getClassLoader().getResourceAsStream("org/flywaydb/core/internal/version.txt"),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new FlywayException("Unable to read Flyway version: " + e.getMessage(), e);
        }
    }
}