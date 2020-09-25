/*
 * Copyright 2010-2020 Redgate Software Ltd
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
package org.flywaydb.core.api.output;

import java.util.List;

public class InfoResult extends OperationResultBase {

    public String schemaVersion;
    public String schemaName;
    public List<InfoOutput> migrations;
    public boolean allSchemasEmpty;

    public InfoResult(String flywayVersion,
                      String database,
                      String schemaVersion,
                      String schemaName,
                      List<InfoOutput> migrations,
                      boolean allSchemasEmpty) {
        this.flywayVersion = flywayVersion;
        this.database = database;
        this.schemaVersion = schemaVersion;
        this.schemaName = schemaName;
        this.migrations = migrations;
        this.operation = "info";
        this.allSchemasEmpty = allSchemasEmpty;
    }
    
}