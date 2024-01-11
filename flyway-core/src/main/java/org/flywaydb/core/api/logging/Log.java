package org.flywaydb.core.api.logging;

public interface Log {

    boolean isDebugEnabled();

    void debug(String message);

    void info(String message);

    void warn(String message);

    void error(String message);

    void error(String message, Exception e);

    void notice(String message);
}