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
import lombok.Getter;
import org.flywaydb.core.api.CoreErrorCode;
import org.flywaydb.core.api.ErrorCode;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.output.MigrateErrorResult;
import org.flywaydb.core.api.output.MigrateResult;
import org.flywaydb.core.internal.sqlscript.FlywaySqlScriptException;
import org.flywaydb.core.internal.sqlscript.SqlStatement;
import org.flywaydb.core.internal.util.ExceptionUtils;

@Getter
public class FlywayMigrateException extends FlywayException {
    private final MigrationInfo migration;
    private final boolean executableInTransaction;
    private final boolean outOfOrder;
    private final MigrateErrorResult errorResult;
    private final int lineNumber;
    private final String absolutePathOnDisk;
    private final String sqlState;
    private final int sqlErrorCode;

    public ErrorCode getMigrationErrorCode() {
        if (migration.getVersion() != null) {
            if (migration.getType().isBaseline()) {
                return CoreErrorCode.FAILED_BASELINE_MIGRATION;
            }
            return CoreErrorCode.FAILED_VERSIONED_MIGRATION;
        } else {
            return CoreErrorCode.FAILED_REPEATABLE_MIGRATION;
        }
    }

    public FlywayMigrateException(final MigrationInfo migration,
        final boolean outOfOrder,
        final SQLException e,
        final boolean canExecuteInTransaction,
        final MigrateResult partialResult) {
        super(ExceptionUtils.toMessage(e), e);
        this.migration = migration;
        this.outOfOrder = outOfOrder;
        this.executableInTransaction = canExecuteInTransaction;
        this.errorResult = new MigrateErrorResult(partialResult, this);
        this.lineNumber = -1;
        this.absolutePathOnDisk = migration.getScript();
        this.sqlState = e.getSQLState();
        this.sqlErrorCode = e.getErrorCode();
    }

    public FlywayMigrateException(final MigrationInfo migration,
        final String message,
        final boolean canExecuteInTransaction,
        final MigrateResult partialResult) {
        super(message);
        this.outOfOrder = false;
        this.migration = migration;
        this.executableInTransaction = canExecuteInTransaction;
        this.errorResult = new MigrateErrorResult(partialResult, this);
        this.lineNumber = -1;
        this.absolutePathOnDisk = migration.getScript();
        this.sqlState = null;
        this.sqlErrorCode = 0;
    }

    public FlywayMigrateException(final MigrationInfo migration,
        final boolean outOfOrder,
        final FlywayException e,
        final boolean canExecuteInTransaction,
        final MigrateResult partialResult) {
        super(e.getMessage(), e);
        if (e instanceof final FlywaySqlScriptException flywaySqlScriptException) {
            this.lineNumber = flywaySqlScriptException.getLineNumber();
            this.absolutePathOnDisk = flywaySqlScriptException.getResource().getAbsolutePathOnDisk();
            this.sqlState = flywaySqlScriptException.getSqlState();
            this.sqlErrorCode = flywaySqlScriptException.getSqlErrorCode();
        } else {
            this.lineNumber = -1;
            this.absolutePathOnDisk = migration.getScript();
            this.sqlState = null;
            this.sqlErrorCode = 0;
        }
        this.migration = migration;
        this.outOfOrder = outOfOrder;
        this.executableInTransaction = canExecuteInTransaction;
        this.errorResult = new MigrateErrorResult(partialResult, this);
    }

    public FlywayMigrateException(final MigrationInfo migration,
        final boolean outOfOrder,
        final String message,
        final FlywayException e,
        final boolean canExecuteInTransaction,
        final MigrateResult partialResult,
        final SqlStatement sqlStatement) {
        super(message, e.getCause());
        this.lineNumber = sqlStatement.getLineNumber();
        this.absolutePathOnDisk = migration.getScript();
        this.migration = migration;
        this.outOfOrder = outOfOrder;
        this.executableInTransaction = canExecuteInTransaction;
        this.errorResult = new MigrateErrorResult(partialResult, this);

        if (e instanceof final FlywaySqlScriptException flywaySqlScriptException) {
            this.sqlState = flywaySqlScriptException.getSqlState();
            this.sqlErrorCode = flywaySqlScriptException.getSqlErrorCode();
        } else {
            this.sqlState = null;
            this.sqlErrorCode = 0;
        }
    }
}
