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
package org.flywaydb.core.internal.command.clean;

import lombok.Getter;
import lombok.Setter;
import org.flywaydb.core.extensibility.ConfigurationExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.flywaydb.core.internal.configuration.ConfigUtils.FLYWAY_PLUGINS_PREFIX;

@Getter
@Setter
public class CleanModeConfigurationExtension implements ConfigurationExtension {
    public enum Mode {
        DEFAULT, SCHEMA, ALL;
    }

    private static final String CLEAN_MODE = FLYWAY_PLUGINS_PREFIX + "clean.mode";
    private static final String CLEAN_SCHEMAS_EXCLUDE = FLYWAY_PLUGINS_PREFIX + "clean.schemas.exclude";

    CleanModel clean = new CleanModel();

    @Override
    public String getNamespace() {
        return "plugins";
    }

    @Override
    public String getConfigurationParameterFromEnvironmentVariable(String environmentVariable) {
        switch (environmentVariable) {
            case "FLYWAY_PLUGINS_CLEAN_MODE":
                return CLEAN_MODE;
            case "FLYWAY_PLUGINS_CLEAN_SCHEMAS_EXCLUDE":
                return CLEAN_SCHEMAS_EXCLUDE;
            default:
                return null;
        }
    }

    public void setCleanSchemasExclude(String... cleanSchemasExclude) {
        this.clean.setSchemas(new SchemaModel());
        this.clean.getSchemas().setExclude(Arrays.asList(cleanSchemasExclude));
    }
}