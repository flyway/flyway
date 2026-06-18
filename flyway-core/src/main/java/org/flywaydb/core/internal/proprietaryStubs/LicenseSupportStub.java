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
package org.flywaydb.core.internal.proprietaryStubs;

import java.util.Collections;
import java.util.List;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.LicenseSupport;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.license.FlywayPermit;

public class LicenseSupportStub implements LicenseSupport {
    private static final FlywayPermit OSS_PERMIT = new FlywayPermit();

    @Override
    public FlywayPermit getPermit(Configuration configuration, boolean fromCache) {
        return OSS_PERMIT;
    }

    @Override
    public void submitPur(Configuration configuration, String eventType, Database database) { }

    @Override
    public List<String> consumeDeferredWarnings(Configuration configuration) {
        return Collections.emptyList();
    }

    @Override
    public boolean isRedgateEdition() {
        return false;
    }

    @Override
    public int getPriority() {
        return -100;
    }

}
