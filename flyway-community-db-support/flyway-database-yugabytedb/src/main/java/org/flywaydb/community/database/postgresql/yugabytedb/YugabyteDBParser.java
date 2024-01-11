package org.flywaydb.community.database.postgresql.yugabytedb;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.database.postgresql.PostgreSQLParser;

public class YugabyteDBParser extends PostgreSQLParser {
    protected YugabyteDBParser(Configuration configuration, ParsingContext parsingContext) {
        super(configuration, parsingContext);
    }
}