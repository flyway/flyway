/*
 * Copyright 2010-2019 Boxfuse GmbH
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
package org.flywaydb.commandline;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogCreator;

class NoopLogCreator implements LogCreator
{
    @Override
    public Log createLogger(Class<?> clazz) {
        return new Log() {
            @Override
            public boolean isDebugEnabled() {
                return false;
            }

            @Override
            public void debug(String message) {
                // No op
            }

            @Override
            public void info(String message) {
                // No op
            }

            @Override
            public void warn(String message) {
                // No op
            }

            @Override
            public void error(String message) {
                // No op
            }

            @Override
            public void error(String message, Exception e) {
                // No op
            }
        };
    }
}