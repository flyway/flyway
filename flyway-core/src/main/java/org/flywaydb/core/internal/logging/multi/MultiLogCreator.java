package org.flywaydb.core.internal.logging.multi;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogCreator;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class MultiLogCreator implements LogCreator {
    private final List<LogCreator> logCreators;

    @Override
    public Log createLogger(Class<?> clazz) {
        List<Log> logs = new ArrayList<>();

        for (LogCreator logCreator : logCreators) {
            logs.add(logCreator.createLogger(clazz));
        }

        return new MultiLogger(logs);
    }

    public static MultiLogCreator empty() {
        return new MultiLogCreator(new ArrayList<>());
    }
}