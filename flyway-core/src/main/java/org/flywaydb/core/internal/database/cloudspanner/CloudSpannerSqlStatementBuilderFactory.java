package org.flywaydb.core.internal.database.cloudspanner;

import org.flywaydb.core.internal.placeholder.PlaceholderReplacer;
import org.flywaydb.core.internal.sqlscript.AbstractSqlStatementBuilderFactory;
import org.flywaydb.core.internal.sqlscript.SqlStatementBuilder;

class CloudSpannerSqlStatementBuilderFactory extends AbstractSqlStatementBuilderFactory {
    public CloudSpannerSqlStatementBuilderFactory(PlaceholderReplacer placeholderReplacer) {
        super(placeholderReplacer);
    }
    
    @Override
    public SqlStatementBuilder createSqlStatementBuilder() {
        return new CloudSpannerSqlStatementBuilder();
    }
}
