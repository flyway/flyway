/*-
 * ========================LICENSE_START=================================
 * flyway-core
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
package org.flywaydb.core.internal.configuration.models;

import javax.sql.DataSource;
import lombok.Getter;
import lombok.Setter;
import org.flywaydb.core.internal.database.DatabaseType;

@Getter
@Setter
public class DataSourceModel {
    private DataSource dataSource;
    private boolean dataSourceGenerated;
    private DatabaseType databaseType;

    public DataSourceModel(DataSource dataSource, boolean dataSourceGenerated) {
        this.dataSource = dataSource;
        this.dataSourceGenerated = dataSourceGenerated;
        this.databaseType = null;
    }
}
