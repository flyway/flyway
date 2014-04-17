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
package org.flywaydb.commandline;

import org.flywaydb.core.internal.util.logging.Log;

/**
 * Wrapper around a simple Console output.
 */
public class ConsoleLog implements Log {
    /**
     * Whether to also print debug statement.
     */
    private final boolean debug;

    /**
     * Creates a new Console Log.
     *
     * @param debug {@code true} for also printing debug statements, {@code false} for only info and higher.
     */
    public ConsoleLog(boolean debug) {
        this.debug = debug;
    }

    public void debug(String message) {
        if (debug) {
            System.out.println("DEBUG: " + message);
        }
    }

    public void info(String message) {
        System.out.println(message);
    }

    public void warn(String message) {
        System.out.println("WARNING: " + message);
    }

    public void error(String message) {
        System.out.println("ERROR: " + message);
    }

    public void error(String message, Exception e) {
        System.out.println("ERROR: " + message);
        e.printStackTrace();
    }
}
