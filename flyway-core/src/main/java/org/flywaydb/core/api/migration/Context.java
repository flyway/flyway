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
package org.flywaydb.core.api.migration;

import org.flywaydb.core.api.configuration.Configuration;

import java.sql.Connection;

/**
 * The context relevant to a Java-based migration.
 */
public interface Context {
    /**
     * @return The configuration currently in use.
     */
    Configuration getConfiguration();

    /**
     * @return The JDBC connection being used. Transaction are managed by Flyway.
     * When the context is passed to the migrate method, a transaction will already have
     * been started if required and will be automatically committed or rolled back afterwards, unless the
     * canExecuteInTransaction method has been implemented to return false.
     */
    Connection getConnection();
}