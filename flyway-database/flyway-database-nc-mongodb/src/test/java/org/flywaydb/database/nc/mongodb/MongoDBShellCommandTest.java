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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Verifies the configurable {@code mongosh} shell command / URL introduced for
 * <a href="https://github.com/flyway/flyway/issues/4242">flyway/flyway#4242</a>.
 */
class MongoDBShellCommandTest {

    @Test
    void extensionDefaultsPreserveLegacyBehaviour() {
        final MongoDBConfigurationExtension extension = new MongoDBConfigurationExtension();

        assertEquals(List.of("mongosh"), extension.getShellCommand(),
            "shellCommand must default to [\"mongosh\"] to keep host-PATH resolution working");
        assertNull(extension.getShellUrl(),
            "shellUrl must default to null so flyway.url is used unchanged");
        assertEquals("mongodb", extension.getNamespace());
    }

    @Test
    void environmentVariablesMapToNamespacedProperties() {
        final MongoDBConfigurationExtension extension = new MongoDBConfigurationExtension();

        assertEquals("flyway.mongodb.shellCommand",
            extension.getConfigurationParameterFromEnvironmentVariable("FLYWAY_MONGODB_SHELL_COMMAND"));
        assertEquals("flyway.mongodb.shellUrl",
            extension.getConfigurationParameterFromEnvironmentVariable("FLYWAY_MONGODB_SHELL_URL"));
        assertNull(extension.getConfigurationParameterFromEnvironmentVariable("FLYWAY_SOMETHING_ELSE"));
    }

    @Test
    void defaultShellCommandProducesBareMongoshInvocation() {
        final MongoshCredential credential =
            new MongoshCredential("mongodb://localhost:27017/test", null, null);

        final List<String> commands =
            MongoDBDatabase.buildMongoshConnectCommands(List.of("mongosh"), credential);

        assertEquals(List.of("mongosh", "mongodb://localhost:27017/test"), commands);
    }

    @Test
    void shellCommandPrefixAndShellUrlReachTheProcessBuilder() {
        final List<String> shellCommand =
            List.of("docker", "exec", "-i", "my-mongo-container", "mongosh");
        final MongoshCredential credential =
            new MongoshCredential("mongodb://localhost:27017/test", "user", "secret");

        final List<String> commands =
            MongoDBDatabase.buildMongoshConnectCommands(shellCommand, credential);

        assertEquals(List.of(
            "docker", "exec", "-i", "my-mongo-container", "mongosh",
            "mongodb://localhost:27017/test",
            "--username", "user",
            "--password", "secret"), commands);
    }

    @Test
    void credentialsAreOmittedWhenNotProvided() {
        final MongoshCredential credential =
            new MongoshCredential("mongodb://localhost:27017/test", "user", null);

        final List<String> commands = MongoDBDatabase.buildMongoshConnectCommands(
            List.of("kubectl", "exec", "-i", "mongo-0", "--", "mongosh"), credential);

        assertEquals(List.of(
            "kubectl", "exec", "-i", "mongo-0", "--", "mongosh",
            "mongodb://localhost:27017/test",
            "--username", "user"), commands);
    }
}
