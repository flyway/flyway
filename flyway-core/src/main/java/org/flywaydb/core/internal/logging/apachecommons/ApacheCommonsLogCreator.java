package org.flywaydb.core.internal.logging.apachecommons;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogCreator;
import org.apache.commons.logging.LogFactory;

public class ApacheCommonsLogCreator implements LogCreator {
    public Log createLogger(Class<?> clazz) {
        return new ApacheCommonsLog(LogFactory.getLog(clazz));
    }
}