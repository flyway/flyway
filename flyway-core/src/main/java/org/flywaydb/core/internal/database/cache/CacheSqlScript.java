package org.flywaydb.core.internal.database.cache;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.Delimiter;
import org.flywaydb.core.internal.database.ExecutableSqlScript;
import org.flywaydb.core.internal.database.SqlStatementBuilder;
import org.flywaydb.core.internal.util.jdbc.ContextImpl;
import org.flywaydb.core.internal.util.placeholder.PlaceholderReplacer;
import org.flywaydb.core.internal.util.scanner.LoadableResource;

/**
 * Cache-specific SQL script.
 */
class CacheSqlScript extends ExecutableSqlScript<ContextImpl> {

    CacheSqlScript(Configuration configuration, LoadableResource sqlScriptResource, boolean mixed, PlaceholderReplacer placeholderReplacer) {
        super(configuration, sqlScriptResource, mixed, placeholderReplacer);
    }

    @Override
    protected SqlStatementBuilder createSqlStatementBuilder() {
        return new CacheSqlStatementBuilder(Delimiter.SEMICOLON);
    }
}