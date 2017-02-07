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
package org.flywaydb.gradle;

import org.gradle.api.plugins.ExtensionContainer;

import java.util.Map;

/**
 * The flyway's configuration properties.
 *
 * <p>More info: <a href="https://flywaydb.org/documentation/gradle">https://flywaydb.org/documentation/gradle</a></p>
 */
/**
 * The flyway's configuration properties plus configurations used to determine how to
 * apply extension containers (if they exist).
 */
public class FlywayExtension extends FlywayExtensionBase {
    public ListPropertyInstruction schemasInstruction = ListPropertyInstruction.PRIORITIZE;
    public ListPropertyInstruction locationsInstruction = ListPropertyInstruction.PRIORITIZE;
    public ListPropertyInstruction resolversInstruction = ListPropertyInstruction.PRIORITIZE;
    public ListPropertyInstruction placeholdersInstruction = ListPropertyInstruction.PRIORITIZE;
    public ListPropertyInstruction callbacksInstruction = ListPropertyInstruction.PRIORITIZE;

    public ExtensionContainer extensions;
}
