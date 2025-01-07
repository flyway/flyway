/*-
 * ========================LICENSE_START=================================
 * flyway-commandline
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
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
package org.flywaydb.commandline.command.version;

import lombok.AllArgsConstructor;
import org.flywaydb.core.api.output.OperationResult;
import org.flywaydb.core.extensibility.Tier;

import java.util.List;

@AllArgsConstructor
public class VersionResult implements OperationResult {
    public String version;
    public String command;
    public Tier edition;
    public List<PluginVersionResult> pluginVersions;
}
