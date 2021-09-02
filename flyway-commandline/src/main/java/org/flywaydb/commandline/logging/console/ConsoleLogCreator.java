/*
 * Copyright (C) Red Gate Software Ltd 2010-2021
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
package org.flywaydb.commandline.logging.console;

import org.flywaydb.commandline.CommandLineArguments;
import org.flywaydb.commandline.CommandLineArguments.Color;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogCreator;

public class ConsoleLogCreator implements LogCreator {
    private final CommandLineArguments commandLineArguments;

    public ConsoleLogCreator(CommandLineArguments commandLineArguments) {
        this.commandLineArguments = commandLineArguments;
    }

    public Log createLogger(Class<?> clazz) {
        ConsoleLog log = new ConsoleLog(commandLineArguments.getLogLevel());
        Color color = commandLineArguments.getColor();

        if (Color.NEVER.equals(color) || (Color.AUTO.equals(color) && System.console() == null)) {
            return log;
        }

        ColorizedConsoleLog.install(Color.ALWAYS.equals(color));
        return new ColorizedConsoleLog(log);
    }
}