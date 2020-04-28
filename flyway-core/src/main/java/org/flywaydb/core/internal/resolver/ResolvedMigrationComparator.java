/*
 * Copyright 2010-2020 Redgate Software Ltd
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
package org.flywaydb.core.internal.resolver;

import org.flywaydb.core.api.resolver.ResolvedMigration;

import java.util.Comparator;

/**
* Comparator for ResolvedMigration.
*/
public class ResolvedMigrationComparator implements Comparator<ResolvedMigration> {

    public static final Comparator<ResolvedMigration> DEFAULT_REPEATABLE_MIGRATION_COMPARATOR =
            Comparator.comparing(ResolvedMigration::getDescription);

    private final Comparator<ResolvedMigration> repeatableMigrationComparator;

    public ResolvedMigrationComparator(Comparator<ResolvedMigration> repeatableMigrationComparator) {
        this.repeatableMigrationComparator = repeatableMigrationComparator;
    }

    @Override
    public int compare(ResolvedMigration o1, ResolvedMigration o2) {
        if ((o1.getVersion() != null) && o2.getVersion() != null) {
            int v = o1.getVersion().compareTo(o2.getVersion());
            return v;
        }
        if (o1.getVersion() != null) {
            return -1;
        }
        if (o2.getVersion() != null) {
            return 1;
        }
        return repeatableMigrationComparator.compare(o1, o2);
    }
}