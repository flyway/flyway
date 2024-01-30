package org.flywaydb.core.internal.jdbc;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.internal.database.base.Table;

import java.util.concurrent.Callable;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class TableLockingExecutionTemplate implements ExecutionTemplate {
    private final Table table;
    private final ExecutionTemplate executionTemplate;

    @Override
    public <T> T execute(final Callable<T> callback) {
        return executionTemplate.execute(new Callable<T>() {
            @Override
            public T call() throws Exception {
                try {
                    table.lock();
                    return callback.call();
                } finally {
                    table.unlock();
                }
            }
        });
    }
}