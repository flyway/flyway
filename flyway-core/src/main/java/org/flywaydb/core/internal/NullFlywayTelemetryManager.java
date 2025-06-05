/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
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
package org.flywaydb.core.internal;

import org.flywaydb.core.FlywayTelemetryManager;
import org.flywaydb.core.FlywayTelemetryProperties;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.experimental.MetaData;
import org.flywaydb.core.extensibility.EventTelemetryModel;
import org.flywaydb.core.internal.license.FlywayPermit;

@SuppressWarnings("unused")
public class NullFlywayTelemetryManager implements FlywayTelemetryManager {

    @Override
    public int getPriority() {
        return -1;
    }

    @Override
    public AutoCloseable start() {
        return () -> {};
    }

    @Override
    public void logEvent(final EventTelemetryModel model) {

    }

    @Override
    public void notifyRootConfigChanged(final Configuration config) {

    }

    @Override
    public void notifyPermitChanged(final FlywayPermit permit) {

    }

    @Override
    public void notifyDatabaseChanged(final String engine, final String version, final String hosting) {

    }

    @Override
    public void notifyExperimentalMetadataChanged(final MetaData metadata) {

    }

    @Override
    public FlywayTelemetryProperties getProperties() {
        return new FlywayTelemetryProperties(null, null, null);
    }
}
