/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.internal.logging.console;

import org.flywaydb.core.api.logging.Log;

/**
 * Wrapper around a simple Console output.
 */
public class ConsoleLog implements Log {
 	public enum Level {
 		DEBUG, INFO, WARN
 	}
	
    private final Level level;

    /**
     * Creates a new Console Log.
     *
     * @param level the log level.
     */
    public ConsoleLog(Level level) {
        this.level = level;
    }

    @Override
    public boolean isDebugEnabled() {
        return level == Level.DEBUG;
    }

    public void debug(String message) {
        if (isDebugEnabled()) {
            System.out.println("DEBUG: " + message);
        }
    }

    public void info(String message) {
    	if (level.compareTo(Level.INFO) <= 0) {
	        System.out.println(message);
	    }
    }

    public void warn(String message) {
    	System.out.println("WARNING: " + message);
    }

    public void error(String message) {
        System.err.println("ERROR: " + message);
    }

    public void error(String message, Exception e) {
        System.err.println("ERROR: " + message);
        e.printStackTrace(System.err);
    }
}