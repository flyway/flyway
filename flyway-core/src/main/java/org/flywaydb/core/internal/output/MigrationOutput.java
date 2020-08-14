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
package org.flywaydb.core.internal.output;

public class MigrationOutput {
    public String category;
    public String version;
    public String description;
    public String type;
    public String installedOn;
    public String state;
    public String undoable;
    public String filepath;
    public String installedBy;
    public int executionTime;

    public MigrationOutput(String category, String version, String description, String type, String installedOn,
                           String state, String undoable, String filepath, String installedBy, int executionTime) {
        this.category = category;
        this.version = version;
        this.description = description;
        this.type = type;
        this.installedOn = installedOn;
        this.state = state;
        this.undoable = undoable;
        this.filepath = filepath;
        this.installedBy = installedBy;
        this.executionTime = executionTime;
    }
}