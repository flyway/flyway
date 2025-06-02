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
package org.flywaydb.core.internal.exception.sqlExceptions;

import java.sql.SQLException;
import javax.sql.DataSource;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.util.FlywayDbWebsiteLinks;

@SuppressWarnings("ClassTooDeepInInheritanceTree")
public class FlywaySqlNoIntegratedAuthException extends FlywaySqlException {
    public FlywaySqlNoIntegratedAuthException(final SQLException sqlException, final DataSource dataSource) {
        super("Unable to obtain connection from database: "
            + getDataSourceInfo(dataSource, true)
            + sqlException.getMessage()
            + "\nTo setup integrated authentication see "
            + FlywayDbWebsiteLinks.WINDOWS_AUTH, sqlException);
    }

    @SuppressWarnings("unused")
    public static boolean isFlywaySpecificVersionOf(final SQLException e) {
        return "08S01".equals(e.getSQLState()) && e.getMessage()
            .contains("This driver is not configured for integrated authentication");
    }
}
