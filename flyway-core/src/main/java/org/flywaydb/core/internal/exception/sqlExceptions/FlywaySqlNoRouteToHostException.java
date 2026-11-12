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
package org.flywaydb.core.internal.exception.sqlExceptions;

import java.net.NoRouteToHostException;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.flywaydb.core.internal.util.ExceptionUtils;

@SuppressWarnings("ClassTooDeepInInheritanceTree")
public class FlywaySqlNoRouteToHostException extends FlywaySqlUnableToConnectToDbException {
    public FlywaySqlNoRouteToHostException(final SQLException sqlException, final DataSource dataSource) {
        super(sqlException, dataSource, FlywaySqlServerErrorCode.NO_ROUTE_TO_HOST);
    }

    public static boolean isNoRouteToHost(final SQLException e) {
        return ExceptionUtils.exceptionHasCauseOf(e, NoRouteToHostException.class)
            || (e.getMessage() != null && e.getMessage().contains("No route to host"));
    }
}
