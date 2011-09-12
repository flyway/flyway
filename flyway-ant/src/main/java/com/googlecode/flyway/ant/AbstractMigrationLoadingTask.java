/**
 * Copyright (C) 2010-2011 the original author or authors.
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
package com.googlecode.flyway.ant;

import com.googlecode.flyway.core.Flyway;

/**
 * Base class for tasks that rely on loading migrations from the classpath.
 */
@SuppressWarnings({"UnusedDeclaration"})
public abstract class AbstractMigrationLoadingTask extends AbstractFlywayTask {
    /**
     * The base package where the Java migrations are located. (default: db.migration)<br/>Also configurable with Ant Property: ${flyway.basePackage}
     */
    private String basePackage;

    /**
     * The base directory on the classpath where the Sql migrations are located. (default: db/migration)<br/>Also configurable with Ant Property: ${flyway.baseDir}
     */
    private String baseDir;

    /**
     * The encoding of Sql migrations. (default: UTF-8)<br/>Also configurable with Ant Property: ${flyway.encoding}
     */
    private String encoding;

    /**
     * The file name prefix for Sql migrations (default: V)<br/>Also configurable with Ant Property: ${flyway.sqlMigrationPrefix}
     */
    private String sqlMigrationPrefix;

    /**
     * The file name suffix for Sql migrations (default: .sql)<br/>Also configurable with Ant Property: ${flyway.sqlMigrationSuffix}
     */
    private String sqlMigrationSuffix;

    /**
     * @param basePackage The base package where the Java migrations are located. (default: db.migration)<br/>Also configurable with Ant Property: ${flyway.basePackage}
     */
    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    /**
     * @param baseDir The base directory on the classpath where the Sql migrations are located. (default: db/migration)<br/>Also configurable with Ant Property: ${flyway.baseDir}
     */
    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    /**
     * @param encoding The encoding of Sql migrations. (default: UTF-8)<br/>Also configurable with Ant Property: ${flyway.encoding}
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * @param sqlMigrationPrefix The file name prefix for Sql migrations (default: V)<br/>Also configurable with Ant Property: ${flyway.sqlMigrationPrefix}
     */
    public void setSqlMigrationPrefix(String sqlMigrationPrefix) {
        this.sqlMigrationPrefix = sqlMigrationPrefix;
    }

    /**
     * @param sqlMigrationSuffix The file name suffix for Sql migrations (default: .sql)<br/>Also configurable with Ant Property: ${flyway.sqlMigrationSuffix}
     */
    public void setSqlMigrationSuffix(String sqlMigrationSuffix) {
        this.sqlMigrationSuffix = sqlMigrationSuffix;
    }

    @Override
    protected void doExecute(Flyway flyway) throws Exception {
        String basePackageValue = useValueIfPropertyNotSet(basePackage, "basePackage");
        if (basePackageValue != null) {
            flyway.setBasePackage(basePackageValue);
        }
        String baseDirValue = useValueIfPropertyNotSet(baseDir, "baseDir");
        if (baseDirValue != null) {
            flyway.setBaseDir(baseDirValue);
        }
        String encodingValue = useValueIfPropertyNotSet(encoding, "encoding");
        if (encodingValue != null) {
            flyway.setEncoding(encodingValue);
        }
        String sqlMigrationPrefixValue = useValueIfPropertyNotSet(sqlMigrationPrefix, "sqlMigrationPrefix");
        if (sqlMigrationPrefixValue != null) {
            flyway.setSqlMigrationPrefix(sqlMigrationPrefixValue);
        }
        String sqlMigrationSuffixValue = useValueIfPropertyNotSet(sqlMigrationSuffix, "sqlMigrationSuffix");
        if (sqlMigrationSuffixValue != null) {
            flyway.setSqlMigrationSuffix(sqlMigrationSuffixValue);
        }
    }
}
