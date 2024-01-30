package org.flywaydb.core.internal.resolver.java;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.ClassProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.resolver.ResolvedMigrationComparator;
import org.flywaydb.core.internal.util.ClassUtils;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ScanningJavaMigrationResolver implements MigrationResolver {

    private final ClassProvider<JavaMigration> classProvider;

    private final Configuration configuration;

    @Override
    public List<ResolvedMigration> resolveMigrations(Context context) {
        List<ResolvedMigration> migrations = new ArrayList<>();

        for (Class<?> clazz : classProvider.getClasses()) {
            JavaMigration javaMigration = ClassUtils.instantiate(clazz.getName(), configuration.getClassLoader());
            migrations.add(javaMigration.getResolvedMigration(configuration, context.statementInterceptor));
        }

        migrations.sort(new ResolvedMigrationComparator());
        return migrations;
    }
}