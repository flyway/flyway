/*-
 * ========================LICENSE_START=================================
 * flyway-nc-core
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
package org.flywaydb.nc.utils;

import java.nio.file.Path;
import lombok.CustomLog;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.nc.NativeConnectorsDatabase;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.nc.Executor;

@CustomLog
public class ErrorUtils {
    public static <T, DB extends NativeConnectorsDatabase> String calculateErrorMessage(final String title,
        final LoadableResource loadableResource,
        final String physicalLocation,
        final Executor<T, DB> executor,
        final T executionUnit,
        final String message) {

        final String underline = StringUtils.trimOrPad("", title.length(), '-');

        final StringBuilder messageBuilder = new StringBuilder().append(title)
            .append("\n")
            .append(underline)
            .append("\n");

        if (message != null) {
            messageBuilder.append(message);
        }

        if (loadableResource != null) {
            messageBuilder.append("Location   : ")
                .append(loadableResource.getAbsolutePath())
                .append(" (")
                .append(loadableResource.getAbsolutePathOnDisk())
                .append(")\n");
        } else {
            messageBuilder.append("Location   : ").append(physicalLocation);
        }

        if (executionUnit != null && executor != null) {
            executor.appendErrorMessage(executionUnit, messageBuilder, LOG.isDebugEnabled());
        }

        return messageBuilder.toString();
    }

    public static String getScriptExecutionErrorMessageTitle(final Path scriptName, final String environment) {
        final StringBuilder messageBuilder = new StringBuilder("Failed to execute script ").append(scriptName);

        if (!"default".equalsIgnoreCase(environment)) {
            messageBuilder.append(" against ").append(environment).append(" environment");
        }

        return messageBuilder.toString();
    }
}
