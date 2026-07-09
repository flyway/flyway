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
package org.flywaydb.core.internal.logging.apachecommons;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;

@RequiredArgsConstructor
public class ApacheCommonsLog implements Log {

    private final org.apache.commons.logging.Log logger;

    public void debug(final String message) {
        if (LogFactory.isDebugEnabled()) {
            logger.debug(message);
        }
    }

    public void info(final String message) {
        if (!LogFactory.isQuietMode()) {
            logger.info(message);
        }
    }

    public void warn(final String message) {
        logger.warn(message);
    }

    public void error(final String message) {
        logger.error(message);
    }

    public void error(final String message, final Exception e) {
        logger.error(message, e);
    }

    public void notice(final String message) {}
}
