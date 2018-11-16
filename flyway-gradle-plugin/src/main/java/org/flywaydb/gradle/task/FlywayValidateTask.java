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
package org.flywaydb.gradle.task;

import org.flywaydb.core.Flyway;

/**
 * <p>Validate applied migrations against resolved ones (on the filesystem or classpath)
 * to detect accidental changes that may prevent the schema(s) from being recreated exactly.</p>
 * <p>Validation fails if</p>
 * <ul>
 *     <li>differences in migration names, types or checksums are found</li>
 *     <li>versions have been applied that aren't resolved locally anymore</li>
 *     <li>versions have been resolved that haven't been applied yet</li>
 * </ul>
 *
 * <img src="https://flywaydb.org/assets/balsamiq/command-validate.png" alt="validate">
 */
public class FlywayValidateTask extends AbstractFlywayTask {
    public FlywayValidateTask() {
        super();
        setDescription("Validate applied migrations against resolved ones (on the filesystem or classpath)" +
                " to detect accidental changes that may prevent the schema(s) from being recreated exactly." +
                " Validation fails if differences in migration names, types or checksums are found, " +
                "versions have been applied that aren\"t resolved locally anymore or" +
                " versions have been resolved that haven\"t been applied yet");
    }

    @Override
    protected Object run(Flyway flyway) {
        flyway.validate();
        return null;
    }
}