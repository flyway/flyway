package org.flywaydb.core.internal.jdbc;

import lombok.RequiredArgsConstructor;
import lombok.Getter;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class Result {
    private final long updateCount;
    private final List<String> columns;
    private final List<List<String>> data;
    private final String sql;
}