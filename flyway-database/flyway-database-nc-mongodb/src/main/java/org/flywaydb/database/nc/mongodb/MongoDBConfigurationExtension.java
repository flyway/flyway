/*-
 * ========================LICENSE_START=================================
 * flyway-database-nc-mongodb
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
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
package org.flywaydb.database.nc.mongodb;

import java.util.List;
import lombok.Data;
import org.flywaydb.core.extensibility.ConfigurationExtension;

@Data
public class MongoDBConfigurationExtension implements ConfigurationExtension {
    private static final String MONGODB_SHELL_COMMAND = "flyway.mongodb.shellCommand";
    private static final String MONGODB_SHELL_URL = "flyway.mongodb.shellUrl";

    /**
     * Command prefix used to invoke {@code mongosh} for {@code .js} migrations. Each element is
     * passed as a discrete process argument, so the invocation can be routed through a wrapper such
     * as {@code ["docker", "exec", "-i", "<container>", "mongosh"]}. Defaults to {@code ["mongosh"]}.
     */
    private List<String> shellCommand = List.of("mongosh");

    /**
     * Connection URL passed to {@code mongosh} for {@code .js} migrations. Defaults to {@code null},
     * meaning {@code flyway.url} is used. Override only when {@code mongosh} reaches the database
     * through a different URL than the JVM driver does (e.g. a container with a different port).
     */
    private String shellUrl = null;

    @Override
    public String getNamespace() {
        return "mongodb";
    }

    @Override
    public String getConfigurationParameterFromEnvironmentVariable(final String environmentVariable) {
        switch (environmentVariable) {
            case "FLYWAY_MONGODB_SHELL_COMMAND":
                return MONGODB_SHELL_COMMAND;
            case "FLYWAY_MONGODB_SHELL_URL":
                return MONGODB_SHELL_URL;
            default:
                return null;
        }
    }
}
