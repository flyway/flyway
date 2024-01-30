package org.flywaydb.database.sqlserver;

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