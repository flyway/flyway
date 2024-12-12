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
package org.flywaydb.core.internal.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DockerUtils {
    private static final String NON_REDGATE_DOCKER = "Non-Redgate Docker";
    private static final String NON_REDGATE_CONTAINER = "Non-Redgate Container";
    private static final String UNKNOWN = "Unknown";
    private static final String NOT_CONTAINER = "Not container";
    private static final String REDGATE_DOCKER = "Redgate Docker";

    public static String getContainerType(final java.util.function.Function<String, Path> getPath){
        final var redgateDocker = System.getenv("REDGATE_DOCKER");
        if("true".equals(redgateDocker)) {
            return REDGATE_DOCKER;
        }

        final var osName = System.getProperty("os.name", "generic");
        if(osName.startsWith("Windows")) {
            return NOT_CONTAINER;
        }

        // https://www.baeldung.com/linux/is-process-running-inside-container
        final var cgroupPath = getPath.apply("/proc/1/cgroup");
        if(Files.exists(cgroupPath)) {
            try (final var lines = Files.lines(cgroupPath)) {
                final var groups = lines.map(x -> x.split(":")[2]).toList();
                if (groups.stream().anyMatch(line -> line.startsWith("/docker"))) {
                    return NON_REDGATE_DOCKER;
                }
                if (groups.stream().anyMatch(line -> line.startsWith("/lxc"))) {
                    return NON_REDGATE_CONTAINER;
                }
            } catch (IOException e) {
                return UNKNOWN;
            }
        }

        final var schedPath = getPath.apply("/proc/1/sched");
        if(Files.exists(schedPath)) {
            try (final var lines = Files.lines(schedPath)){
                final var firstLine = lines.findFirst();
                if (firstLine.isPresent()) {
                    if (firstLine.get().contains("init") || firstLine.get().contains("system")) {
                        return NOT_CONTAINER;
                    }
                }
            } catch (IOException e) {
                return UNKNOWN;
            }
        }

        return UNKNOWN;
    }

    public static boolean isContainer() {
        final String containerType = getContainerType(Paths::get);
        return !(containerType.equals(NOT_CONTAINER) || containerType.equals(UNKNOWN));
    }
}
