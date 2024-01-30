package org.flywaydb.core.api.resolver;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.MigrationType;
import org.flywaydb.core.extensibility.Plugin;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.sqlscript.SqlScriptExecutorFactory;
import org.flywaydb.core.internal.sqlscript.SqlScriptFactory;

import java.util.Collection;

/**
 * Resolves available migrations. This interface can be implemented to create custom resolvers. A custom resolver
 * can be used to create additional types of migrations not covered by the standard resolvers (jdbc, sql).
 * Using the skipDefaultResolvers configuration property, the built-in resolvers can also be completely replaced.
 */
public interface MigrationResolver extends Plugin {
    @RequiredArgsConstructor
    class Context {
        public final Configuration configuration;
        public final ResourceProvider resourceProvider;
        public final SqlScriptFactory sqlScriptFactory;
        public final SqlScriptExecutorFactory sqlScriptExecutorFactory;
        public final StatementInterceptor statementInterceptor;
    }

    Collection<ResolvedMigration> resolveMigrations(Context context);

    /**
     * @return The prefix this resolver looks for. {@code null} if no particular prefix is used
     */
    default String getPrefix(Configuration configuration) {
        return null;
    }

    default MigrationType getDefaultMigrationType() {
        return null;
    }
}