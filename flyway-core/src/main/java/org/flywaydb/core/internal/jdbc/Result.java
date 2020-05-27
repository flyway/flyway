/*
 * Copyright 2010-2020 Redgate Software Ltd
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
package org.flywaydb.core.internal.jdbc;

import java.util.List;

public class Result {
    private final long updateCount;
    private final List<String> columns;
    private final List<List<String>> data;
    private final String sql;

    public Result(long updateCount, List<String> columns, List<List<String>> data, String sql) {
        this.updateCount = updateCount;
        this.columns = columns;
        this.data = data;
        this.sql = sql;
    }

    public long getUpdateCount() {
        return updateCount;
    }

    public List<String> getColumns() {
        return columns;
    }

    public List<List<String>> getData() {
        return data;
    }

    public String getSql() {
        return sql;
    }
}