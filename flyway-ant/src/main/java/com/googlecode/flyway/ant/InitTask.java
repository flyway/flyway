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
package com.googlecode.flyway.ant;

import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.api.MigrationVersion;

/**
 * Ant task that initializes the metadata table in an existing schema.
 */
@SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
public class InitTask extends AbstractFlywayTask {
    /**
     * The initial version to put in the database. (default: 0)<br/>Also configurable with Ant Property: ${flyway.initialVersion}
     */
    private String initialVersion;

    /**
     * The description of the initial version. (default: << Flyway Init >>)<br/>Also configurable with Ant Property:
     * ${flyway.initialDescription}
     */
    private String initialDescription;

    /**
     * @param initialVersion The initial version to put in the database. (default: 0)<br/>Also configurable with Ant Property: ${flyway.initialVersion}
     */
    public void setInitialVersion(String initialVersion) {
        this.initialVersion = initialVersion;
    }

    /**
     * @param initialDescription The description of the initial version. (default: << Flyway Init >>)<br/>Also configurable with Ant Property:
     *                           ${flyway.initialDescription}
     */
    public void setInitialDescription(String initialDescription) {
        this.initialDescription = initialDescription;
    }

    @Override
    protected void doExecute(Flyway flyway) throws Exception {
        String initialVersionValue = useValueIfPropertyNotSet(initialVersion, "initialVersion");
        if (initialVersionValue != null) {
            flyway.setInitialVersion(new MigrationVersion(initialVersionValue));
        }
        String initialDescriptionValue = useValueIfPropertyNotSet(initialDescription, "initialDescription");
        if (initialDescriptionValue != null) {
            flyway.setInitialDescription(initialDescriptionValue);
        }

        flyway.init();
    }
}