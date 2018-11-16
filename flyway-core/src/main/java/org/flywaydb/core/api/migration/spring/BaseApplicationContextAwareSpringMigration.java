/**
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
package org.flywaydb.core.api.migration.spring;

import org.flywaydb.core.api.migration.BaseJavaMigration;

/**
 * <p>This is the recommended class to extend for implementing Spring Bean-based Migrations.</p>
 * <p>Subclasses should follow the default Flyway naming convention of having a class name with the following structure:</p>
 * <ul>
 * <li><strong>Versioned Migrations:</strong> V2__Add_new_table</li>
 * <li><strong>Undo Migrations:</strong> U2__Add_new_table</li>
 * <li><strong>Repeatable Migrations:</strong> R__Add_new_table</li>
 * </ul>
 *
 * <p>The file name consists of the following parts:</p>
 * <ul>
 * <li><strong>Prefix:</strong> V for versioned migrations, U for undo migrations, R for repeatable migrations</li>
 * <li><strong>Version:</strong> Underscores (automatically replaced by dots at runtime) separate as many parts as you like (Not for repeatable migrations)</li>
 * <li><strong>Separator:</strong> __ (two underscores)</li>
 * <li><strong>Description:</strong> Underscores (automatically replaced by spaces at runtime) separate the words</li>
 * </ul>
 * <p>If you need more control over the class name, you can override the default convention by implementing the
 * SJavaMigration interface directly. This will allow you to name your class as you wish. Version, description and
 * migration category are provided by implementing the respective methods.</p>
 */
public abstract class BaseApplicationContextAwareSpringMigration extends BaseJavaMigration {
}
