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
package org.flywaydb.maven;

import org.flywaydb.core.Flyway;

/**
 * Repairs the Flyway metadata table. This will perform the following actions:
 * <ul>
 *     <li>Remove any failed migrations on databases without DDL transactions (User objects left behind must still be cleaned up manually)</li>
 *     <li>Correct wrong checksums</li>
 * </ul>
 *
 * @goal repair
 */
@SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
public class RepairMojo extends AbstractFlywayMojo {
    @Override
    protected void doExecute(Flyway flyway) throws Exception {
        flyway.repair();
    }
}