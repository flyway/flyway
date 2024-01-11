package org.flywaydb.core.internal.logging.buffered;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogCreator;

public class BufferedLogCreator implements LogCreator {
    private static final BufferedLog bufferedLog = new BufferedLog();

    @Override
    public Log createLogger(Class<?> clazz) {
        return bufferedLog;
    }
}