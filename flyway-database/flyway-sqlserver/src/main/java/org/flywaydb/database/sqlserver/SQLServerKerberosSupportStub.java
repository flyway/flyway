/*-
 * ========================LICENSE_START=================================
 * flyway-sqlserver
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
package org.flywaydb.database.sqlserver;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.license.FlywayEditionUpgradeRequiredException;
import org.flywaydb.core.internal.util.StringUtils;

public class SQLServerKerberosSupportStub implements SQLServerKerberosSupport {
    @Override
    public void configureKerberos(final Configuration config,
        final SQLServerConfigurationExtension configurationExtension) {
        if (StringUtils.hasText(configurationExtension.getKerberos().getLogin().getFile())) {
            throw new FlywayEditionUpgradeRequiredException(null, "sqlserver.kerberos.login.file");
        }
        if (StringUtils.hasText(config.getKerberosConfigFile())) {
            throw new FlywayEditionUpgradeRequiredException(null, "sqlserver.kerberos.config.file");
        }
    }

    @Override
    public int getPriority() {
        return -100;
    }
}
