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
package org.flywaydb.core.api;

import java.util.Date;

/**
 * Info about a migration.
 */
public interface MigrationInfo extends Comparable<MigrationInfo> {
    /**
     * @return The type of migration (BASELINE, SQL, JDBC, ...)
     */
    MigrationType getType();

    /**
     * @return The target version of this migration.
     */
    Integer getChecksum();

    /**
     * @return The schema version after the migration is complete.
     */
    MigrationVersion getVersion();

    /**
     * @return The description of the migration.
     */
    String getDescription();

    /**
     * @return The name of the script to execute for this migration, relative to its classpath or filesystem location.
     */
    String getScript();

    /**
     * @return The state of the migration (PENDING, SUCCESS, ...)
     */
    MigrationState getState();

    /**
     * @return The timestamp when this migration was installed. (Only for applied migrations)
     */
    Date getInstalledOn();

    /**
     * @return The user that installed this migration. (Only for applied migrations)
     */
    String getInstalledBy();

    /**
     * @return The rank of this installed migration. This is the most precise way to sort applied migrations by installation order.
     * Migrations that were applied later have a higher rank. (Only for applied migrations)
     */
    Integer getInstalledRank();

    /**
     * @return The execution time (in millis) of this migration. (Only for applied migrations)
     */
    Integer getExecutionTime();
}