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
package org.flywaydb.core.internal.exception;

import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import lombok.Getter;
import lombok.SneakyThrows;
import org.flywaydb.core.api.CoreErrorCode;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.database.DatabaseTypeRegister;
import org.flywaydb.core.internal.exception.sqlExceptions.FlywaySqlNoDriversForInteractiveAuthException;
import org.flywaydb.core.internal.exception.sqlExceptions.FlywaySqlNoIntegratedAuthException;
import org.flywaydb.core.internal.exception.sqlExceptions.FlywaySqlServerUntrustedCertificateSqlException;
import org.flywaydb.core.internal.jdbc.DriverDataSource;
import org.flywaydb.core.internal.util.ExceptionUtils;
import org.flywaydb.core.internal.util.StringUtils;

/**
 * The specific exception thrown when Flyway encounters a problem in SQL.
 */
@Getter
public class FlywaySqlException extends FlywayException {
    private final String sqlState;
    private final int sqlErrorCode;

    @SuppressWarnings("ClassReferencesSubclass")
    private static List<Class<? extends FlywaySqlException>> getSpecificFlywaySqlExceptionClasses() {
        return List.of(FlywaySqlServerUntrustedCertificateSqlException.class,
            FlywaySqlNoIntegratedAuthException.class,
            FlywaySqlNoDriversForInteractiveAuthException.class);
    }

    public FlywaySqlException(final String message, final SQLException sqlException) {
        super(message, sqlException, CoreErrorCode.DB_CONNECTION);
        sqlState = sqlException.getSQLState();
        sqlErrorCode = sqlException.getErrorCode();
    }

    @Override
    public String getMessage() {
        final String title = super.getMessage();
        final String underline = StringUtils.trimOrPad("", title.length(), '-');

        return title + "\n" + underline + "\n" + ExceptionUtils.toMessage((SQLException) getCause());
    }

    @SneakyThrows
    public static void throwFlywayExceptionIfPossible(final SQLException sqlException, final DataSource dataSource) {
        for (final Class<? extends FlywaySqlException> x : getSpecificFlywaySqlExceptionClasses()) {

            if ((boolean) x.getMethod("isFlywaySpecificVersionOf", SQLException.class).invoke(null, sqlException)) {
                throw x.getDeclaredConstructor(SQLException.class, DataSource.class)
                    .newInstance(sqlException, dataSource);
            }
        }
    }

    protected static String getDataSourceInfo(final DataSource dataSource, final boolean suppressNullUserMessage) {
        if (!(dataSource instanceof final DriverDataSource driverDataSource)) {
            return "";
        }
        final String user = driverDataSource.getUser();
        String info = " (" + DatabaseTypeRegister.redactJdbcUrl(driverDataSource.getUrl()) + ")";
        if (user != null || !suppressNullUserMessage) {
            info += " for user '" + user + "'";
        }
        return info;
    }
}
