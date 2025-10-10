/*-
 * ========================LICENSE_START=================================
 * flyway-core
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
package org.flywaydb.core.internal.logging;

import lombok.AllArgsConstructor;
import lombok.Synchronized;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.logging.buffered.BufferedLog;

@AllArgsConstructor
public class EvolvingLog implements Log {
    private Log log;
    private final Class<?> clazz;

    @Synchronized
    private void updateLog() {
        Log newLog = ((EvolvingLog) LogFactory.getLog(clazz)).getLog();

        if (log instanceof BufferedLog && !(newLog instanceof BufferedLog)) {
            ((BufferedLog) log).flush(newLog);
        }

        log = newLog;
    }

    public Log getLog() {
        return log;
    }

    @Override
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }

    @Override
    public boolean isNoticeEnabled() {
        return log.isNoticeEnabled();
    }

    @Override
    public void debug(String message) {
        if (!isDebugEnabled()) {
            return;
        }
        updateLog();
        log.debug(message);
    }

    @Override
    public void info(String message) {
        if (!isInfoEnabled()) {
            return;
        }
        updateLog();
        log.info(message);
    }

    @Override
    public void warn(String message) {
        if (!isWarnEnabled()) {
            return;
        }
        updateLog();
        log.warn(message);
    }

    @Override
    public void error(String message) {
        if (!isErrorEnabled()) {
            return;
        }
        updateLog();
        log.error(message);
    }

    @Override
    public void error(String message, Exception e) {
        if (!isErrorEnabled()) {
            return;
        }
        updateLog();
        log.error(message, e);
    }

    @Override
    public void notice(String message) {
        if (!isNoticeEnabled()) {
            return;
        }
        updateLog();
        log.notice(message);
    }
}
