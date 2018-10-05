package org.flywaydb.core.internal.database.clickhouse;

import org.flywaydb.core.internal.sqlscript.Delimiter;
import org.flywaydb.core.internal.sqlscript.SqlStatementBuilder;

public class ClickhouseStatementBuilder extends SqlStatementBuilder {
    /**
     * Creates a new SqlStatementBuilder.
     *
     */
    public ClickhouseStatementBuilder() {
        super(Delimiter.SEMICOLON);
    }
}
