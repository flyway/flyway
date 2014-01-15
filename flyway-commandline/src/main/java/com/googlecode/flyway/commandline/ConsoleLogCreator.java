/**
 * Copyright 2010-2014 Axel Fontaine and the many contributors.
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
package com.googlecode.flyway.commandline;

import com.googlecode.flyway.core.util.logging.Log;
import com.googlecode.flyway.core.util.logging.LogCreator;

/**
 * Log Creator for the Command-Line console.
 */
public class ConsoleLogCreator implements LogCreator {
    /**
     * Is debug mode enabled?
     */
    private final boolean debug;

    /**
     * Creates a new Console Log Creator.
     *
     * @param debug {@code true} for also printing debug statements, {@code false} for only info and higher.
     */
    public ConsoleLogCreator(boolean debug) {
        this.debug = debug;
    }

    public Log createLogger(Class<?> clazz) {
        return new ConsoleLog(debug);
    }
}
