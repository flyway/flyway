/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.resolver;

import java.util.Map;

import javax.sql.DataSource;

import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.flywaydb.core.api.resolver.MigrationResolver;

/**
 * Dummy Implementation of {@link FlywayConfiguration} for unit tests.
 */
public class FlywayConfigurationForTests implements FlywayConfiguration {

    private ClassLoader classLoader;
    private String[] locations = new String[0];
    private String encoding;
    private String sqlMigrationPrefix;
    private String repeatableSqlMigrationPrefix;
    private String sqlMigrationSeparator;
    private String sqlMigrationSuffix;
    private MyCustomMigrationResolver[] migrationResolvers = new MyCustomMigrationResolver[0];
    private boolean skipDefaultResolvers;
    private boolean skipDefaultCallbacks;

    public FlywayConfigurationForTests(ClassLoader contextClassLoader, String[] locations, String encoding,
            String sqlMigrationPrefix, String repeatableSqlMigrationPrefix, String sqlMigrationSeparator, String sqlMigrationSuffix,
            MyCustomMigrationResolver... myCustomMigrationResolver) {
        this.classLoader = contextClassLoader;
        this.locations = locations;
        this.encoding = encoding;
        this.sqlMigrationPrefix = sqlMigrationPrefix;
        this.repeatableSqlMigrationPrefix = repeatableSqlMigrationPrefix;
        this.sqlMigrationSeparator = sqlMigrationSeparator;
        this.sqlMigrationSuffix = sqlMigrationSuffix;
        this.migrationResolvers = myCustomMigrationResolver;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void setLocations(String[] locations) {
        this.locations = locations;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setSqlMigrationSeparator(String sqlMigrationSeparator) {
        this.sqlMigrationSeparator = sqlMigrationSeparator;
    }

    public void setSqlMigrationSuffix(String sqlMigrationSuffix) {
        this.sqlMigrationSuffix = sqlMigrationSuffix;
    }

    public void setMigrationResolvers(MyCustomMigrationResolver[] migrationResolvers) {
        this.migrationResolvers = migrationResolvers;
    }

    public static FlywayConfigurationForTests create() {
        return new FlywayConfigurationForTests(Thread.currentThread().getContextClassLoader(), new String[0], "UTF-8", "V", "R", "__", ".sql");
    }

    public static FlywayConfigurationForTests createWithLocations(String... locations) {
        return new FlywayConfigurationForTests(Thread.currentThread().getContextClassLoader(), locations, "UTF-8", "V", "R", "__", ".sql");
    }

    public static FlywayConfigurationForTests createWithPrefix(String prefix) {
        return new FlywayConfigurationForTests(Thread.currentThread().getContextClassLoader(), new String[0], "UTF-8", prefix, "R", "__", ".sql");
    }

    public static FlywayConfigurationForTests createWithPrefixAndLocations(String prefix, String... locations) {
        return new FlywayConfigurationForTests(Thread.currentThread().getContextClassLoader(), locations, "UTF-8", prefix, "R", "__", ".sql");
    }

    @Override
    public FlywayCallback[] getCallbacks() {
        return null;
    }

    @Override
    public boolean isSkipDefaultCallbacks() {
        return skipDefaultCallbacks;
    }

    public void setSkipDefaultCallbacks(boolean skipDefaultCallbacks) {
        this.skipDefaultCallbacks = skipDefaultCallbacks;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setSkipDefaultResolvers(boolean skipDefaultResolvers) {
        this.skipDefaultResolvers = skipDefaultResolvers;
    }

    @Override
    public boolean isSkipDefaultResolvers() {
        return skipDefaultResolvers;
    }

    @Override
    public DataSource getDataSource() {
        return null;
    }

    @Override
    public MigrationResolver[] getResolvers() {
        return migrationResolvers;
    }

    @Override
    public String getBaselineDescription() {
        return null;
    }

    @Override
    public MigrationVersion getBaselineVersion() {
        return null;
    }

    @Override
    public String getSqlMigrationSuffix() {
        return sqlMigrationSuffix;
    }

    @Override
    public String getRepeatableSqlMigrationPrefix() {
        return repeatableSqlMigrationPrefix;
    }

    @Override
    public String getSqlMigrationSeparator() {
        return sqlMigrationSeparator;
    }

    @Override
    public String getSqlMigrationPrefix() {
        return sqlMigrationPrefix;
    }

    @Override
    public boolean isPlaceholderReplacement() {
        return false;
    }

    @Override
    public String getPlaceholderSuffix() {
        return null;
    }

    @Override
    public String getPlaceholderPrefix() {
        return null;
    }

    @Override
    public Map<String, String> getPlaceholders() {
        return null;
    }

    @Override
    public MigrationVersion getTarget() {
        return null;
    }

    @Override
    public String getTable() {
        return null;
    }

    @Override
    public String[] getSchemas() {
        return null;
    }

    @Override
    public String[] getLocations() {
        return this.locations;
    }

    @Override
    public boolean isBaselineOnMigrate() {
        return false;
    }

    @Override
    public boolean isOutOfOrder() {
        return false;
    }

    @Override
    public boolean isIgnoreMissingMigrations() {
        return false;
    }

    @Override
    public boolean isIgnoreFutureMigrations() {
        return false;
    }

    @Override
    public boolean isValidateOnMigrate() {
        return false;
    }

    @Override
    public boolean isCleanOnValidationError() {
        return false;
    }

    @Override
    public boolean isCleanDisabled() {
        return false;
    }

    @Override
    public boolean isAllowMixedMigrations() {
        return false;
    }

    @Override
    public boolean isMixed() {
        return false;
    }

    @Override
    public boolean isGroup() {
        return false;
    }

    @Override
    public String getInstalledBy() {
        return null;
    }

    @Override
    public String getEncoding() {
        return this.encoding;
    }

    public void setRepeatableSqlMigrationPrefix(String repeatableSqlMigrationPrefix) {
        this.repeatableSqlMigrationPrefix = repeatableSqlMigrationPrefix;
    }

    public void setSqlMigrationPrefix(String sqlMigrationPrefix) {
        this.sqlMigrationPrefix = sqlMigrationPrefix;
    }

    public void setResolvers(MyCustomMigrationResolver... myCustomMigrationResolver) {
        this.migrationResolvers = myCustomMigrationResolver;
    }
}
