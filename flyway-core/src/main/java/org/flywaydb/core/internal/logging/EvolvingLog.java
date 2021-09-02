/*
 * Copyright (C) Red Gate Software Ltd 2010-2021
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
package org.flywaydb.core.internal.logging;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.logging.buffered.BufferedLog;

public class EvolvingLog implements Log {
    private Log log;
    private final Class<?> clazz;

    public EvolvingLog(Log log, Class<?> clazz) {
        this.log = log;
        this.clazz = clazz;
    }

    private void updateLog() {
        Log newLog = ((EvolvingLog)LogFactory.getLog(clazz)).getLog();

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
    public void debug(String message) {
        updateLog();
        log.debug(message);
    }

    @Override
    public void info(String message) {
        updateLog();
        log.info(message);
    }

    @Override
    public void warn(String message) {
        updateLog();
        log.warn(message);
    }

    @Override
    public void error(String message) {
        updateLog();
        log.error(message);
    }

    @Override
    public void error(String message, Exception e) {
        updateLog();
        log.error(message, e);
    }
}