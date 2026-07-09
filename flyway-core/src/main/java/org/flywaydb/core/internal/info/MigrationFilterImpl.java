/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.core.internal.info;

import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.MigrationFilter;

import java.util.Arrays;
import java.util.Date;

public class MigrationFilterImpl implements MigrationFilter {
    public final Date fromDate;
    public final Date toDate;
    public final MigrationVersion fromVersion;
    public final MigrationVersion toVersion;
    public final MigrationState[] ofStates;

    public MigrationFilterImpl(final Date fromDate,
        final Date toDate,
        final MigrationVersion fromVersion,
        final MigrationVersion toVersion,
        MigrationState... ofStates) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.fromVersion = fromVersion;
        this.toVersion = toVersion;
        this.ofStates = ofStates;
    }

    @Override
    public boolean matches(final MigrationInfo info) {
        final MigrationState state = info.getState();
        final Date installedOn = info.getInstalledOn();
        final MigrationVersion version = info.getVersion();

        final boolean afterFromDate = fromDate == null || installedOn == null || installedOn.after(fromDate);
        final boolean beforeToDate = toDate == null || (installedOn != null && installedOn.before(toDate));

        final boolean afterFromVersion = fromVersion == null || version == null || version.compareTo(fromVersion) >= 0;
        final boolean beforeToVersion = toVersion == null || (version != null && version.compareTo(toVersion) <= 0);

        final boolean matchesState = ofStates == null || ofStates.length == 0 || Arrays.stream(ofStates)
            .anyMatch(s -> s == state);

        return afterFromDate && beforeToDate && afterFromVersion && beforeToVersion && matchesState;
    }
}
