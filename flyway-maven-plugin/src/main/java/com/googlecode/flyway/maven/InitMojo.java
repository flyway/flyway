/**
 * Copyright (C) 2010-2012 the original author or authors.
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
package com.googlecode.flyway.maven;

import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.api.MigrationVersion;

/**
 * Maven goal that initializes the metadata table in an existing schema.
 *
 * @goal init
 */
@SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
public class InitMojo extends AbstractFlywayMojo {
    /**
     * The initial version to put in the database. (default: 0) <br>
     * <p>Also configurable with Maven or System Property: ${flyway.initialVersion}</p>
     *
     * @parameter expression="${flyway.initialVersion}"
     */
    private String initialVersion;

    /**
     * The description of the initial version. (default: << Flyway Init >>)<br>
     * <p>Also configurable with Maven or System Property: ${flyway.initialDescription}</p>
     *
     * @parameter expression="${flyway.initialDescription}"
     */
    private String initialDescription;

    @Override
    protected void doExecute(Flyway flyway) throws Exception {
        if (initialVersion != null) {
            flyway.setInitialVersion(new MigrationVersion(initialVersion));
        }
        if (initialDescription != null) {
            flyway.setInitialDescription(initialDescription);
        }

        flyway.init();
    }
}