/*-
 * ========================LICENSE_START=================================
 * flyway-command-test-connection
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
package org.flywaydb.testconnection;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.flywaydb.core.extensibility.ConfigurationExtension;
import org.flywaydb.core.extensibility.ConfigurationParameter;

@Getter
@Setter
public class TestConnectionConfigurationExtension implements ConfigurationExtension {
    private static final String NAMESPACE = "testConnection";

    private String environmentName;

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    static List<ConfigurationParameter> getConfigurationParameters() {
        return List.of(new ConfigurationParameter("testConnection.environmentName",
            """
            The name to give the environment supplied on standard input when `-environment=-` is used.
            An environment already configured under that name is replaced for the duration of the command,
            so the connection is tested as that environment rather than as a separate one. Provisioners that
            derive resources from the environment name, such as the `docker` provisioner, therefore act on the
            environment being tested instead of standing up a second one. Defaults to `default`.""",
            false));
    }
}
