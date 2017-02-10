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
package org.flywaydb.core.internal.dbsupport;

/**
 * A user defined type within a schema.
 */
public abstract class Function extends SchemaObject {
    /**
     * The arguments of the function.
     */
    protected String[] args;

    /**
     * Creates a new function with this name within this schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param dbSupport    The database-specific support.
     * @param schema       The schema this function lives in.
     * @param name         The name of the function.
     * @param args         The arguments of the function.
     */
    public Function(JdbcTemplate jdbcTemplate, DbSupport dbSupport, Schema schema, String name, String... args) {
        super(jdbcTemplate, dbSupport, schema, name);
        this.args = args == null ? new String[0] : args;
    }
}
