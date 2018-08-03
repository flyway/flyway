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
package org.flywaydb.core.internal.info;

import org.flywaydb.core.api.MigrationVersion;

import java.util.HashMap;
import java.util.Map;

/**
 * The current context of the migrations.
 */
public class MigrationInfoContext {
    /**
     * Whether out of order migrations are allowed.
     */
    public boolean outOfOrder;

    /**
     * Whether pending migrations are allowed.
     */
    public boolean pending;

    /**
     * Whether missing migrations are allowed.
     */
    public boolean missing;

    /**
     * Whether ignored migrations are allowed.
     */
    public boolean ignored;

    /**
     * Whether future migrations are allowed.
     */
    public boolean future;

    /**
     * The migration target.
     */
    public MigrationVersion target;

    /**
     * The SCHEMA migration version that was applied.
     */
    public MigrationVersion schema;

    /**
     * The BASELINE migration version that was applied.
     */
    public MigrationVersion baseline;

    /**
     * The last resolved migration.
     */
    public MigrationVersion lastResolved = MigrationVersion.EMPTY;

    /**
     * The last applied migration.
     */
    public MigrationVersion lastApplied = MigrationVersion.EMPTY;

    public Map<String, Integer> latestRepeatableRuns = new HashMap<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MigrationInfoContext that = (MigrationInfoContext) o;

        if (outOfOrder != that.outOfOrder) return false;
        if (pending != that.pending) return false;
        if (missing != that.missing) return false;
        if (ignored != that.ignored) return false;
        if (future != that.future) return false;
        if (target != null ? !target.equals(that.target) : that.target != null) return false;
        if (schema != null ? !schema.equals(that.schema) : that.schema != null) return false;
        if (baseline != null ? !baseline.equals(that.baseline) : that.baseline != null) return false;
        if (lastResolved != null ? !lastResolved.equals(that.lastResolved) : that.lastResolved != null) return false;
        if (lastApplied != null ? !lastApplied.equals(that.lastApplied) : that.lastApplied != null) return false;
        return latestRepeatableRuns.equals(that.latestRepeatableRuns);

    }

    @Override
    public int hashCode() {
        int result = (outOfOrder ? 1 : 0);
        result = 31 * result + (pending ? 1 : 0);
        result = 31 * result + (missing ? 1 : 0);
        result = 31 * result + (ignored ? 1 : 0);
        result = 31 * result + (future ? 1 : 0);
        result = 31 * result + (target != null ? target.hashCode() : 0);
        result = 31 * result + (schema != null ? schema.hashCode() : 0);
        result = 31 * result + (baseline != null ? baseline.hashCode() : 0);
        result = 31 * result + (lastResolved != null ? lastResolved.hashCode() : 0);
        result = 31 * result + (lastApplied != null ? lastApplied.hashCode() : 0);
        result = 31 * result + latestRepeatableRuns.hashCode();
        return result;
    }
}