/**
 * Copyright 2010-2014 Axel Fontaine and the many contributors.
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

import com.googlecode.flyway.core.util.logging.Log;
import com.googlecode.flyway.core.util.logging.LogCreator;
import org.apache.tools.ant.Project;

/**
 * Log Creator for Ant Logging.
 */
public class AntLogCreator implements LogCreator {
    /**
     * The Ant project to log for.
     */
    private final Project antProject;

    /**
     * Creates a new Ant Log Creator for this project.
     *
     * @param antProject The Ant project to log for.
     */
    public AntLogCreator(Project antProject) {
        this.antProject = antProject;
    }

    public Log createLogger(Class<?> clazz) {
        return new AntLog(antProject);
    }
}
