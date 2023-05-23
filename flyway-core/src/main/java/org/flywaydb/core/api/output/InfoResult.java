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
package org.flywaydb.core.api.output;

import java.time.LocalDateTime;
import java.util.List;

public class InfoResult extends HtmlResult {
    private static final String COMMAND = "info";
    public String schemaVersion;
    public String schemaName;
    public List<InfoOutput> migrations;
    public String flywayVersion;
    public String database;
    public boolean allSchemasEmpty;

    public InfoResult(String flywayVersion,
                      String database,
                      String schemaVersion,
                      String schemaName,
                      List<InfoOutput> migrations,
                      boolean allSchemasEmpty) {
        super(LocalDateTime.now(), COMMAND);
        this.flywayVersion = flywayVersion;
        this.database = database;
        this.schemaVersion = schemaVersion;
        this.schemaName = schemaName;
        this.migrations = migrations;
        this.allSchemasEmpty = allSchemasEmpty;
    }
}