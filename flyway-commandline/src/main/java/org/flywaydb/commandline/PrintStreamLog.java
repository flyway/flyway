/*
 * Copyright 2010-2019 Boxfuse GmbH
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

import org.flywaydb.core.api.logging.Log;

import java.io.PrintStream;

/**
 * Wrapper around a simple Console output.
 */
class PrintStreamLog implements Log {
 	public enum Level {
 		DEBUG, INFO, WARN
 	}
	
    private final Level level;
 	private final PrintStream outStream;
 	private final PrintStream errStream;

    /**
     * Creates a new PrintStream Log.
     *
     * @param level the log level.
     * @param outStream the output stream to use.
     * @param errStream the error stream to use.
     */
    public PrintStreamLog(Level level, PrintStream outStream, PrintStream errStream) {
        this.level = level;
        this.outStream = outStream;
        this.errStream = errStream;
    }

    @Override
    public boolean isDebugEnabled() {
        return level == Level.DEBUG;
    }

    public void debug(String message) {
        if (isDebugEnabled()) {
            outStream.println("DEBUG: " + message);
        }
    }

    public void info(String message) {
    	if (level.compareTo(Level.INFO) <= 0) {
            outStream.println(message);
	    }
    }

    public void warn(String message) {
        outStream.println("WARNING: " + message);
    }

    public void error(String message) {
        errStream.println("ERROR: " + message);
    }

    public void error(String message, Exception e) {
        errStream.println("ERROR: " + message);
        e.printStackTrace(errStream);
    }
}