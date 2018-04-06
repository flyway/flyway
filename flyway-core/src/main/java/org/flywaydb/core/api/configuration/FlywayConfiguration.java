/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.api.configuration;

/**
 * Readonly interface for main Flyway configuration. Can be used to provide configuration data to migrations and callbacks.
 *
 * @deprecated Use {@link Configuration} instead. Will be removed in Flyway 6.0.
 */
@Deprecated
public interface FlywayConfiguration extends Configuration {
    /**
     * Retrieves the file name suffix for sql migrations.
     * <p>Sql migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to V1_1__My_description.sql</p>
     *
     * @return The file name suffix for sql migrations. (default: .sql)
     * @deprecated Use {@link FlywayConfiguration#getSqlMigrationSuffixes()} instead. Will be removed in Flyway 6.0.0.
     */
    @Deprecated
    String getSqlMigrationSuffix();
}