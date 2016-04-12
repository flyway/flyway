/**
 * Copyright 2010-2016 Boxfuse GmbH
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
package org.flywaydb.ant;

import org.flywaydb.core.internal.util.logging.Log;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Wrapper around an Ant Logger.
 */
public class AntLog implements Log {
    public void debug(String message) {
        Project antProject = AntLogCreator.INSTANCE.getAntProject();
        Task task = antProject.getThreadTask(Thread.currentThread());
        antProject.log(task, message, Project.MSG_VERBOSE);
    }

    public void info(String message) {
        Project antProject = AntLogCreator.INSTANCE.getAntProject();
        Task task = antProject.getThreadTask(Thread.currentThread());
        antProject.log(task, message, Project.MSG_INFO);
    }

    public void warn(String message) {
        Project antProject = AntLogCreator.INSTANCE.getAntProject();
        Task task = antProject.getThreadTask(Thread.currentThread());
        antProject.log(task, message, Project.MSG_WARN);
    }

    public void error(String message) {
        Project antProject = AntLogCreator.INSTANCE.getAntProject();
        Task task = antProject.getThreadTask(Thread.currentThread());
        antProject.log(task, message, Project.MSG_ERR);
    }

    public void error(String message, Exception e) {
        Project antProject = AntLogCreator.INSTANCE.getAntProject();
        Task task = antProject.getThreadTask(Thread.currentThread());
        antProject.log(task, message, e, Project.MSG_ERR);
    }
}
