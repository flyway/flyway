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
package org.flywaydb.core.api.output;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InfoOutput {
    public String category;
    public String version;
    public String rawVersion;
    public String description;
    public String type;
    public String installedOnUTC;
    public String state;
    public String undoable;
    public String filepath;
    public String undoFilepath;
    public String installedBy;
    public String shouldExecuteExpression;
    public int executionTime;

    public InfoOutput(String category, String version, String rawVersion, String description, String type,
        String installedOnUTC, String state, String undoable, String filepath, String undoFilepath, String installedBy,
        int executionTime) {
        this.category = category;
        this.version = version;
        this.rawVersion = rawVersion;
        this.description = description;
        this.type = type;
        this.installedOnUTC = installedOnUTC;
        this.state = state;
        this.undoable = undoable;
        this.filepath = filepath;
        this.undoFilepath = undoFilepath;
        this.installedBy = installedBy;
        this.executionTime = executionTime;
        this.shouldExecuteExpression = null;
    }
}
