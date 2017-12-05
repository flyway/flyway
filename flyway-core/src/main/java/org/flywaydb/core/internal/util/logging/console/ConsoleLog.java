/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.util.logging.console;

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

    public void debug(String message) {
        if (level == Level.DEBUG) {
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
