/*-
 * ========================LICENSE_START=================================
 * flyway-database-saphana
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
package org.flywaydb.database.saphana;

import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.exception.FlywaySqlException;

import java.sql.SQLException;
import org.flywaydb.core.internal.util.StringUtils;

public class SAPHANAConnection extends Connection<SAPHANADatabase> {
    private final boolean isCloud;

    SAPHANAConnection(SAPHANADatabase database, java.sql.Connection connection) {
        super(database, connection);
        try {
            // If build_qrc_version not detected, it could be an On-premise version or an old Cloud version. So fall back to the previous way of checking build_branch
            // Cloud databases will be fa/CE<year>.<build> eg. fa/CE2020.48
            // On-premise will be fa/hana<version>sp<servicepack> eg. fa/hana2sp05

            String buildQrcVersion = jdbcTemplate.queryForString("SELECT VALUE FROM M_HOST_INFORMATION WHERE KEY='build_qrc_version'");
            if (StringUtils.hasText(buildQrcVersion)) {
                isCloud = true;
            } else {
                String buildBranch = jdbcTemplate.queryForString("SELECT VALUE FROM M_HOST_INFORMATION WHERE KEY='build_branch'");
                isCloud = StringUtils.hasText(buildBranch) && buildBranch.startsWith("fa/CE");
            }

        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to determine build edition", e);
        }
    }

    public boolean isCloudConnection() {
        return isCloud;
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
        return jdbcTemplate.queryForString("SELECT CURRENT_SCHEMA FROM DUMMY");
    }

    @Override
    public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
        jdbcTemplate.execute("SET SCHEMA " + database.doQuote(schema));
    }

    @Override
    public Schema getSchema(String name) {
        return new SAPHANASchema(jdbcTemplate, database, name);
    }
}
