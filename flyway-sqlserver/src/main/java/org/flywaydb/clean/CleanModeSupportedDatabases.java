/*
 * Copyright (C) Red Gate Software Ltd 2010-2023
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