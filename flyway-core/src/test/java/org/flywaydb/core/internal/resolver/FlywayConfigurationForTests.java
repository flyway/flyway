/*
 * Copyright 2010-2018 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
 */
package org.flywaydb.core.internal.resolver;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.errorhandler.ErrorHandler;
import org.flywaydb.core.api.resolver.MigrationResolver;

import javax.sql.DataSource;
import java.io.OutputStream;
import java.util.Map;

/**
 * Dummy Implementation of {@link FlywayConfiguration} for unit tests.
 */
public class FlywayConfigurationForTests implements FlywayConfiguration {

    private ClassLoader classLoader;
    private String[] locations;
    private String encoding;
    private String sqlMigrationPrefix;
    private String repeatableSqlMigrationPrefix;
    private String sqlMigrationSeparator;
    private String[] sqlMigrationSuffixes;
    private MyCustomMigrationResolver[] migrationResolvers = new MyCustomMigrationResolver[0];
    private boolean skipDefaultResolvers;
    private boolean skipDefaultCallbacks;
    private String undoSqlMigrationPrefix = "U";

    public FlywayConfigurationForTests(ClassLoader contextClassLoader, String[] locations, String encoding,
            String sqlMigrationPrefix, String repeatableSqlMigrationPrefix, String sqlMigrationSeparator, String[] sqlMigrationSuffixes,
            MyCustomMigrationResolver... myCustomMigrationResolver) {
        this.classLoader = contextClassLoader;
        this.locations = locations;
        this.encoding = encoding;
        this.sqlMigrationPrefix = sqlMigrationPrefix;
        this.repeatableSqlMigrationPrefix = repeatableSqlMigrationPrefix;
        this.sqlMigrationSeparator = sqlMigrationSeparator;
        this.sqlMigrationSuffixes = sqlMigrationSuffixes;
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

    public void setMigrationResolvers(MyCustomMigrationResolver[] migrationResolvers) {
        this.migrationResolvers = migrationResolvers;
    }

    public static FlywayConfigurationForTests create() {
        return new FlywayConfigurationForTests(Thread.currentThread().getContextClassLoader(), new String[0], "UTF-8", "V", "R", "__", new String[] {".sql"});
    }

    public static FlywayConfigurationForTests createWithLocations(String... locations) {
        return new FlywayConfigurationForTests(Thread.currentThread().getContextClassLoader(), locations, "UTF-8", "V", "R", "__", new String[] {".sql"});
    }

    public static FlywayConfigurationForTests createWithPrefix(String prefix) {
        return new FlywayConfigurationForTests(Thread.currentThread().getContextClassLoader(), new String[0], "UTF-8", prefix, "R", "__", new String[] {".sql"});
    }

    public static FlywayConfigurationForTests createWithPrefixAndLocations(String prefix, String... locations) {
        return new FlywayConfigurationForTests(Thread.currentThread().getContextClassLoader(), locations, "UTF-8", prefix, "R", "__", new String[] {".sql"});
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
        return sqlMigrationSuffixes[0];
    }

    @Override
    public String[] getSqlMigrationSuffixes() {
        return sqlMigrationSuffixes;
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
    public String getUndoSqlMigrationPrefix() {
        return undoSqlMigrationPrefix;
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

    @Override
    public ErrorHandler[] getErrorHandlers() {
        return null;
    }

    @Override
    public OutputStream getDryRunOutput() {
        return null;
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

    public void setUndoSqlMigrationPrefix(String undoSqlMigrationPrefix) {
        this.undoSqlMigrationPrefix = undoSqlMigrationPrefix;
    }
}
