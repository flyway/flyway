package org.flywaydb.core.api.migration.baseline;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.extensibility.MigrationType;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.resolver.java.JavaMigrationExecutor;
import org.flywaydb.core.internal.util.ClassUtils;

public abstract class BaselineJavaMigration extends BaseJavaMigration {

    private static final String BASELINE_MIGRATION_PREFIX = "B";

    @Override
    protected void init() {
        String shortName = getClass().getSimpleName();
        if (!shortName.startsWith(BASELINE_MIGRATION_PREFIX)) {
            throw new FlywayException("Invalid baseline Java migration class name: " + getClass().getName() +
                                              " => ensure it starts with " +
                                              BASELINE_MIGRATION_PREFIX +
                                              " or implement org.flywaydb.core.api.migration.JavaMigration directly for non-default naming");
        }
        extractVersionAndDescription(shortName, BASELINE_MIGRATION_PREFIX, false);
    }

    @Override
    public MigrationType getType() {
        return BaselineMigrationType.JDBC_BASELINE;
    }

    @Override
    public ResolvedMigration getResolvedMigration(Configuration config, StatementInterceptor statementInterceptor) {
        return new BaselineResolvedMigration(getVersion(),
                                             getDescription(),
                                             getClass().getName(),
                                             getChecksum(),
                                             null,
                                             getType(),
                                             ClassUtils.getLocationOnDisk(getClass()),
                                             new JavaMigrationExecutor(this, statementInterceptor),
                                             config);
    }
}