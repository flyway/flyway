/*-
 * ========================LICENSE_START=================================
 * flyway-core
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
package org.flywaydb.core.extensibility;

import java.util.Collections;
import java.util.List;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.OperationResult;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.StringUtils;

public interface VerbExtension extends CommandExtension<OperationResult> {
    String getCommand();

    @Override
    default boolean handlesCommand(final String command) {
        return getCommand().equals(command);
    }

    default boolean handlesParameter(final String parameter) {
        return false;
    }

    default OperationResult handle(final Configuration config, final List<String> flags) throws FlywayException {
        if (flags != null && !flags.isEmpty()) {
            throw new FlywayException("VerbExtension does not accept flags: " + flags);
        }

        return executeVerb(config);
    }

    OperationResult executeVerb(Configuration configuration);

    @Override
    default List<Pair<String, String>> getUsage() {
        if (StringUtils.hasText(getDescription())) {
            return Collections.singletonList(Pair.of(getCommand(), getDescription()));
        }

        return CommandExtension.super.getUsage();
    }
}
