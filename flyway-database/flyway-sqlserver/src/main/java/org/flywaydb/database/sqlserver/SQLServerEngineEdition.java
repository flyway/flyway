/*-
 * ========================LICENSE_START=================================
 * flyway-sqlserver
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
package org.flywaydb.database.sqlserver;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/*
 * SQL Server engine editions. Some restrict the functionality available. See
 * https://docs.microsoft.com/en-us/sql/t-sql/functions/serverproperty-transact-sql?view=sql-server-ver15
 * for details of what each edition supports.
 */
@RequiredArgsConstructor
public enum SQLServerEngineEdition {

    PERSONAL_DESKTOP(1),
    STANDARD(2),
    ENTERPRISE(3),
    EXPRESS(4),
    SQL_DATABASE(5),
    SQL_DATA_WAREHOUSE(6),
    MANAGED_INSTANCE(8),
    AZURE_SQL_EDGE(9);

    @Getter
    private final int code;

    public static SQLServerEngineEdition fromCode(int code) {
        for (SQLServerEngineEdition edition : values()) {
            if (edition.code == code) {
                return edition;
            }
        }
        throw new IllegalArgumentException("Unknown SQL Server engine edition: " + code);
    }
}
