/*-
 * ========================LICENSE_START=================================
 * flyway-nc-utils
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
package org.flywaydb.nc;

import lombok.CustomLog;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.nc.executors.Executor;

@CustomLog
public class ErrorUtils {

    public static <T> String calculateErrorMessage(final Exception e,
        final String title,
        final LoadableResource loadableResource,
        final String physicalLocation,
        final Executor<T> executor,
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
}
