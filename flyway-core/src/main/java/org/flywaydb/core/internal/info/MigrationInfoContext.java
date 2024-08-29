/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2024 Red Gate Software Ltd
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

import org.flywaydb.core.api.MigrationPattern;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.pattern.ValidatePattern;
import org.flywaydb.core.internal.util.ValidatePatternUtils;

import java.util.HashMap;
import java.util.Map;

public class MigrationInfoContext {
    public boolean outOfOrder;
    public ValidatePattern[] ignorePatterns = new ValidatePattern[0];
    public MigrationVersion target;
    public MigrationPattern[] cherryPick;
    public MigrationVersion schema;
    public MigrationVersion pendingBaseline;
    public MigrationVersion appliedBaseline;
    public MigrationVersion lastResolved = MigrationVersion.EMPTY;
    public MigrationVersion lastApplied = MigrationVersion.EMPTY;
    public Map<String, Integer> latestRepeatableRuns = new HashMap<>();

    public boolean isPendingIgnored() {
        return ValidatePatternUtils.isPendingIgnored(ignorePatterns);
    }

    public boolean isIgnoredIgnored() {
        return cherryPick != null || ValidatePatternUtils.isIgnoredIgnored(ignorePatterns);
    }

    public boolean isMissingIgnored() {
        return ValidatePatternUtils.isMissingIgnored(ignorePatterns);
    }

    public boolean isFutureIgnored() {
        return ValidatePatternUtils.isFutureIgnored(ignorePatterns);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MigrationInfoContext that = (MigrationInfoContext) o;

        if (outOfOrder != that.outOfOrder) {
            return false;
        }
        if (target != null ? !target.equals(that.target) : that.target != null) {
            return false;
        }
        if (schema != null ? !schema.equals(that.schema) : that.schema != null) {
            return false;
        }
        if (appliedBaseline != null ? !appliedBaseline.equals(that.appliedBaseline) : that.appliedBaseline != null) {
            return false;
        }
        if (lastResolved != null ? !lastResolved.equals(that.lastResolved) : that.lastResolved != null) {
            return false;
        }
        if (lastApplied != null ? !lastApplied.equals(that.lastApplied) : that.lastApplied != null) {
            return false;
        }
        if (cherryPick != null ? !cherryPick.equals(that.cherryPick) : that.cherryPick != null) {
            return false;
        }
        if (ignorePatterns != null ? !ignorePatterns.equals(that.ignorePatterns) : that.ignorePatterns != null) {
            return false;
        }
        return latestRepeatableRuns.equals(that.latestRepeatableRuns);
    }

    @Override
    public int hashCode() {
        int result = (outOfOrder ? 1 : 0);
        result = 31 * result + (target != null ? target.hashCode() : 0);
        result = 31 * result + (schema != null ? schema.hashCode() : 0);
        result = 31 * result + (appliedBaseline != null ? appliedBaseline.hashCode() : 0);
        result = 31 * result + (lastResolved != null ? lastResolved.hashCode() : 0);
        result = 31 * result + (lastApplied != null ? lastApplied.hashCode() : 0);
        result = 31 * result + (cherryPick != null ? cherryPick.hashCode() : 0);
        result = 31 * result + (ignorePatterns != null ? ignorePatterns.hashCode() : 0);
        result = 31 * result + latestRepeatableRuns.hashCode();
        return result;
    }
}
