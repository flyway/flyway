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

import com.googlecode.flyway.core.api.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of MigrationInfos.
 */
public class MigrationInfosImpl implements MigrationInfos {
    /**
     * The migration infos.
     */
    private final List<? extends MigrationInfo> migrationInfos;

    /**
     * Creates a new migrationInfos based on these migration infos.
     *
     * @param migrationInfos The migration infos.
     */
    public MigrationInfosImpl(List<? extends MigrationInfo> migrationInfos) {
        this.migrationInfos = migrationInfos;
    }

    public MigrationInfoImpl[] all() {
        return migrationInfos.toArray(new MigrationInfoImpl[migrationInfos.size()]);
    }

    public MigrationInfo current() {
        for (int i = migrationInfos.size() - 1; i >= 0; i--) {
            MigrationInfo migrationInfo = migrationInfos.get(i);
            if (migrationInfo.getState().isApplied()) {
                return migrationInfo;
            }
        }

        return null;
    }

    public MigrationInfo failed() {
        for (int i = migrationInfos.size() - 1; i >= 0; i--) {
            MigrationInfo migrationInfo = migrationInfos.get(i);
            if (migrationInfo.getState().isFailed()) {
                return migrationInfo;
            }
        }

        return null;
    }

    public MigrationInfo[] pending() {
        List<MigrationInfo> pendingMigrations = new ArrayList<MigrationInfo>();
        for (MigrationInfo migrationInfo : migrationInfos) {
            if (com.googlecode.flyway.core.api.MigrationState.PENDING == migrationInfo.getState()) {
                pendingMigrations.add(migrationInfo);
            }
        }

        return pendingMigrations.toArray(new MigrationInfo[pendingMigrations.size()]);
    }

    /**
     * @return The last (highest version) available migration.
     */
    public MigrationInfo lastAvailable() {
        for (int i = migrationInfos.size() - 1; i >= 0; i--) {
            MigrationInfo migrationInfo = migrationInfos.get(i);
            if (migrationInfo.getState().isAvailable()) {
                return migrationInfo;
            }
        }

        return null;
    }
}
