/**
 * Copyright 2010-2013 Axel Fontaine and the many contributors.
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
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Wrapper around an Ant Logger.
 */
public class AntLog implements Log {
    /**
     * The Ant project to log for.
     */
    private final Project antProject;

    /**
     * Creates a new wrapper around this logger.
     *
     * @param antProject The Ant project to log for.
     */
    public AntLog(Project antProject) {
        this.antProject = antProject;
    }

    public void debug(String message) {
        Task task = antProject.getThreadTask(Thread.currentThread());
        antProject.log(task, message, Project.MSG_VERBOSE);
    }

    public void info(String message) {
        Task task = antProject.getThreadTask(Thread.currentThread());
        antProject.log(task, message, Project.MSG_INFO);
    }

    public void warn(String message) {
        Task task = antProject.getThreadTask(Thread.currentThread());
        antProject.log(task, message, Project.MSG_WARN);
    }

    public void error(String message) {
        Task task = antProject.getThreadTask(Thread.currentThread());
        antProject.log(task, message, Project.MSG_ERR);
    }

    public void error(String message, Exception e) {
        Task task = antProject.getThreadTask(Thread.currentThread());
        antProject.log(task, message, e, Project.MSG_ERR);
    }
}
