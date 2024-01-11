package org.flywaydb.clean;

import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.database.sqlserver.SQLServerDatabase;

import java.util.Collections;
import java.util.List;

public class CleanModeSupportedDatabases {
    private static final List<Class<? extends Database>> SUPPORTED_DATABASES = Collections.singletonList(SQLServerDatabase.class);

    public static boolean supportsCleanMode(Database database) {
        return SUPPORTED_DATABASES.stream().anyMatch(c -> c.isInstance(database));
    }
}