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
package org.flywaydb.commandline.logging;

import lombok.NoArgsConstructor;
import org.flywaydb.commandline.configuration.CommandLineArguments;
import org.flywaydb.commandline.logging.console.ConsoleLogCreator;
import org.flywaydb.commandline.logging.file.FileLogCreator;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogCreator;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.logging.multi.MultiLogCreator;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class LoggingUtils {

    public static LogCreator getLogCreator(CommandLineArguments commandLineArguments) {
        // JSON output uses a different mechanism, so we do not create any loggers
        if (commandLineArguments.shouldOutputJson() || (commandLineArguments.hasOperation("info") && commandLineArguments.isFilterOnMigrationIds())) {
            return MultiLogCreator.empty();
        }

        List<LogCreator> logCreators = new ArrayList<>();
        logCreators.add(new ConsoleLogCreator(commandLineArguments));
        if (commandLineArguments.isOutputFileSet()) {
            logCreators.add(new FileLogCreator(commandLineArguments));
        }

        return new MultiLogCreator(logCreators);
    }

    public static Log initLogging(Class<?> clazz, CommandLineArguments commandLineArguments) {
        LogFactory.setFallbackLogCreator(getLogCreator(commandLineArguments));
        return LogFactory.getLog(clazz);
    }
}
