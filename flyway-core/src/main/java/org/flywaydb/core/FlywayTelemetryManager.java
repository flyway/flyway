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
package org.flywaydb.core;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.nc.MetaData;
import org.flywaydb.core.extensibility.EventTelemetryModel;
import org.flywaydb.core.extensibility.Plugin;
import org.flywaydb.core.internal.license.FlywayPermit;

public interface FlywayTelemetryManager extends Plugin {

    AutoCloseable start();

    void logEvent(final EventTelemetryModel model);

    void notifyRootConfigChanged(final Configuration config);

    void notifyPermitChanged(final FlywayPermit permit);

    void notifyDatabaseChanged(final String engine, final String version, final String hosting);

    void notifyExperimentalMetadataChanged(final MetaData metadata);

    FlywayTelemetryProperties getProperties();
}
