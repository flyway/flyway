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
package org.flywaydb.commandline.command.version;

import lombok.CustomLog;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.OperationResult;
import org.flywaydb.core.extensibility.CommandExtension;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.core.internal.util.Pair;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    public OperationResult handle(String command, Configuration config, List<String> flags) throws FlywayException {
        VersionPrinter.printVersionOnly();
        LOG.info("");

        LOG.debug("Java " + System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")");
        LOG.debug(System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch") + "\n");

        return new OperationResult() {};
    }

    @Override
    public List<Pair<String, String>> getUsage() {
        return Collections.singletonList(Pair.of(VERSION + ", " + String.join(", ", FLAGS), "Print the Flyway version and edition"));
    }
}