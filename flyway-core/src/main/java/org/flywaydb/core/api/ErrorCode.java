/*
 * Copyright © Red Gate Software Ltd 2010-2021
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
package org.flywaydb.core.api;

public enum ErrorCode {
    // general error codes
    FAULT,
    ERROR,
    JDBC_DRIVER,
    DB_CONNECTION,
    CONFIGURATION,
    DUPLICATE_VERSIONED_MIGRATION,
    DUPLICATE_REPEATABLE_MIGRATION,
    DUPLICATE_UNDO_MIGRATION,
    DUPLICATE_DELETED_MIGRATION,

    // validate error codes
    VALIDATE_ERROR,
    SCHEMA_DOES_NOT_EXIST,
    FAILED_REPEATABLE_MIGRATION,
    FAILED_VERSIONED_MIGRATION,
    APPLIED_REPEATABLE_MIGRATION_NOT_RESOLVED,
    APPLIED_VERSIONED_MIGRATION_NOT_RESOLVED,
    RESOLVED_REPEATABLE_MIGRATION_NOT_APPLIED,
    RESOLVED_VERSIONED_MIGRATION_NOT_APPLIED,
    OUTDATED_REPEATABLE_MIGRATION,
    TYPE_MISMATCH,
    CHECKSUM_MISMATCH,
    DESCRIPTION_MISMATCH
}