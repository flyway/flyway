/*-
 * ========================LICENSE_START=================================
 * flyway-core
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
package org.flywaydb.core.internal.configuration.extensions;

import java.nio.file.FileSystems;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.flywaydb.core.extensibility.ConfigurationExtension;
import org.flywaydb.core.extensibility.ConfigurationParameter;

@Getter
@Setter
public class PrepareScriptFilenameConfigurationExtension implements ConfigurationExtension {
    private static final String SCRIPT_FOLDER_DEFAULT = "deployments";
    private static final String SCRIPT_FILE_DEFAULT = "D__deployment.sql";
    private static final String UNDO_FILE_DEFAULT = "DU__undo.sql";

    private static final String SCRIPT_FILENAME_DEFAULT = SCRIPT_FOLDER_DEFAULT + FileSystems.getDefault()
        .getSeparator() + SCRIPT_FILE_DEFAULT;

    private static final String UNDO_FILENAME_DEFAULT = SCRIPT_FOLDER_DEFAULT + FileSystems.getDefault()
        .getSeparator() + UNDO_FILE_DEFAULT;

    private String scriptFilename = SCRIPT_FILENAME_DEFAULT;
    private String undoFilename = UNDO_FILENAME_DEFAULT;

    @Override
    public String getNamespace() {
        return "prepare";
    }

    @Override
    public String getConfigurationParameterFromEnvironmentVariable(final String environmentVariable) {
        return null;
    }

    public static List<ConfigurationParameter> getConfigurationParameters() {
        return List.of(new ConfigurationParameter("scriptFilename",
                "A deployment script to generate for use with `deploy`. If not specified the location will default to "
                    + SCRIPT_FILENAME_DEFAULT,
                false),
            new ConfigurationParameter("undoFilename",
                "A deployment undo script to generate for use with `deploy`. If not specified the location will default to "
                    + UNDO_FILENAME_DEFAULT,
                false));
    }
}
