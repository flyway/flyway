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
package com.googlecode.flyway.core.migration;

import com.googlecode.flyway.core.api.MigrationVersion;

/**
 * Complete info about a migration, aggregated from all sources (classpath, DB, ...)
 *
 * TODO: Not so happy with the name of this class. Suggestions welcome.
 */
public class FullMigration implements Comparable<FullMigration> {
    /**
     * The version of this migration.
     */
    private final MigrationVersion version;

    /**
     * The applied migration with this version.
     */
    private ExecutedMigration executedMigration;

    /**
     * The available migration with this version.
     */
    private ResolvedMigration resolvedMigration;

    /**
     * Creates a new FullMigration for this version.
     *
     * @param version The version of this migration.
     */
    public FullMigration(MigrationVersion version) {
        this.version = version;
    }

    /**
     * @return The version of this migration.
     */
    public MigrationVersion getVersion() {
        return version;
    }

    /**
     * @param executedMigration The applied migration with this version.
     */
    public void setExecutedMigration(ExecutedMigration executedMigration) {
        this.executedMigration = executedMigration;
    }

    /**
     * @param resolvedMigration The available migration with this version.
     */
    public void setResolvedMigration(ResolvedMigration resolvedMigration) {
        this.resolvedMigration = resolvedMigration;
    }

    public int compareTo(FullMigration o) {
        return version.compareTo(o.version);
    }
}
