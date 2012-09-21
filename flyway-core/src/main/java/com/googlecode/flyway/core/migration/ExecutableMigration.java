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

/**
 * An executable migration that can be applied against a DB.
 */
public class ExecutableMigration implements Comparable<ExecutableMigration> {
    /**
     * The info about the migration.
     */
    private final MigrationInfoImpl info;

    /**
     * The physical location of the migration on disk.
     */
    private final String physicalLocation;

    /**
     * The executor to run this migration.
     */
    private final MigrationExecutor executor;

    /**
     * Creazes a new executable migration.
     *
     * @param info             The info about the migration.
     * @param physicalLocation The physical location of the migration on disk.
     * @param executor         The executor to run this migration.
     */
    public ExecutableMigration(MigrationInfoImpl info, String physicalLocation, MigrationExecutor executor) {
        this.info = info;
        this.physicalLocation = physicalLocation;
        this.executor = executor;
    }

    /**
     * @return The info about the migration.
     */
    public MigrationInfoImpl getInfo() {
        return info;
    }

    /**
     * @return The physical location of the migration on disk.
     */
    public String getPhysicalLocation() {
        return physicalLocation;
    }

    /**
     * @return The executor to run this migration.
     */
    public MigrationExecutor getExecutor() {
        return executor;
    }

    public int compareTo(ExecutableMigration o) {
        return info.compareTo(o.info);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExecutableMigration migration = (ExecutableMigration) o;

        return info.equals(migration.info);
    }

    @Override
    public int hashCode() {
        return info.hashCode();
    }
}
