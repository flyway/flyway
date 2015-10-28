/**
 * Copyright 2010-2015 Axel Fontaine
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

/**
 * The current context of the migrations.
 */
public class MigrationInfoContext {
    /**
     * Whether out of order migrations are allowed.
     */
    public boolean outOfOrder;

    /**
     * Whether pending or future migrations are allowed.
     */
    public boolean pendingOrFuture;

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

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MigrationInfoContext context = (MigrationInfoContext) o;

        if (outOfOrder != context.outOfOrder) return false;
        if (pendingOrFuture != context.pendingOrFuture) return false;
        if (schema != null ? !schema.equals(context.schema) : context.schema != null) return false;
        if (baseline != null ? !baseline.equals(context.baseline) : context.baseline != null) return false;
        if (!lastApplied.equals(context.lastApplied)) return false;
        if (!lastResolved.equals(context.lastResolved)) return false;
        return target.equals(context.target);
    }

    @Override
    public int hashCode() {
        int result = (outOfOrder ? 1 : 0);
        result = 31 * result + (pendingOrFuture ? 1 : 0);
        result = 31 * result + target.hashCode();
        result = 31 * result + (schema != null ? schema.hashCode() : 0);
        result = 31 * result + (baseline != null ? baseline.hashCode() : 0);
        result = 31 * result + lastResolved.hashCode();
        result = 31 * result + lastApplied.hashCode();
        return result;
    }
}
