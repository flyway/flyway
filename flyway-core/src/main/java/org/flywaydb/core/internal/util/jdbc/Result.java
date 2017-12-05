/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.util.jdbc;

import java.util.List;

public class Result {
    private final long updateCount;
    // [pro]
    private final List<String> columns;
    private final List<List<String>> data;
    // [/pro]

    public Result(long updateCount
                  // [pro]
            , List<String> columns, List<List<String>> data
                  // [/pro]
    ) {
        this.updateCount = updateCount;
        // [pro]
        this.columns = columns;
        this.data = data;
        // [/pro]
    }

    public long getUpdateCount() {
        return updateCount;
    }

    // [pro]
    public List<String> getColumns() {
        return columns;
    }

    public List<List<String>> getData() {
        return data;
    }
    // [/pro]
}
