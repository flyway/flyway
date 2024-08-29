/*-
 * ========================LICENSE_START=================================
 * flyway-verb-utils
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
package org.flywaydb.verb.info;

import java.util.Comparator;
import java.util.Map;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.experimental.migration.ExperimentalMigrationComparator;

public class VersionOrderMigrationComparator implements ExperimentalMigrationComparator {
    @Override
    public int getPriority(final Configuration configuration) {
        return 0;
    }
    
    // States in order - highest number goes to the end of the list
    private static final Map<MigrationState, Integer> STATE_ORDER = Map.of(
        MigrationState.IGNORED, 3,
        MigrationState.PENDING, 2,
        MigrationState.AVAILABLE, 1);
                                                                          

    @Override
    public Comparator<MigrationInfo> getComparator(final Configuration configuration) {        
        return (o1, o2) -> {

            final boolean o1BaselineOrdering = o1.getState() == MigrationState.BELOW_BASELINE
                || o1.getState() == MigrationState.BASELINE_IGNORED
                || o1.getState() == MigrationState.BASELINE;
            final boolean o2BaselineOrdering = o2.getState() == MigrationState.BELOW_BASELINE
                || o2.getState() == MigrationState.BASELINE_IGNORED
                || o2.getState() == MigrationState.BASELINE;
            if(o1BaselineOrdering && o2BaselineOrdering) {
                return o1.getState().compareTo(o2.getState());
            }

            if (o1.getInstalledRank() != null && o2.getInstalledRank() != null) {
                return Integer.compare(o1.getInstalledRank(), o2.getInstalledRank());
            }
            
            if (STATE_ORDER.containsKey(o1.getState()) && STATE_ORDER.containsKey(o2.getState())) {
                if(o1.getState() != o2.getState()) {
                    return STATE_ORDER.get(o1.getState()).compareTo(STATE_ORDER.get(o2.getState()));
                }
                if (o1.isVersioned() && o2.isVersioned()) {
                    return o1.getVersion().compareTo(o2.getVersion());
                }else if (o1.isRepeatable() && o2.isRepeatable()) {
                    return o1.getDescription().compareTo(o2.getDescription());
                } else {
                    return o1.getVersion() != null ? -1 : 1;
                }
            }
            
            if (STATE_ORDER.containsKey(o1.getState())) {
                return 1;
            }
            if (STATE_ORDER.containsKey(o2.getState())) {
                return -1;
            }    
//            return o1.getVersion().compareTo(o2.getVersion());
            

            if (o1.isRepeatable() && o2.isRepeatable()) {
                return o1.getDescription().compareTo(o2.getDescription());
            }

            return o1.getVersion() != null ? -1 : 1;
        };        
    }

    @Override
    public String getName() {
        return "Info";
    }
}
