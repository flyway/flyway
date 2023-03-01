package org.flywaydb.community.database.clickhouse;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;

public class ClickHouseParser extends Parser {
    protected ClickHouseParser(Configuration configuration, ParsingContext parsingContext, int peekDepth) {
        super(configuration, parsingContext, peekDepth);
    }
}
