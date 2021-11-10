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
package org.flywaydb.core.internal.logging.buffered;

import org.flywaydb.core.api.logging.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BufferedLog implements Log {
    public final List<BufferedLogMessage> bufferedLogMessages = Collections.synchronizedList(new ArrayList<>());

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public void debug(String message) {
        bufferedLogMessages.add(new BufferedLogMessage(message, Level.DEBUG));
    }

    @Override
    public void info(String message) {
        bufferedLogMessages.add(new BufferedLogMessage(message, Level.INFO));
    }

    @Override
    public void warn(String message) {
        bufferedLogMessages.add(new BufferedLogMessage(message, Level.WARN));
    }

    @Override
    public void error(String message) {
        bufferedLogMessages.add(new BufferedLogMessage(message, Level.ERROR));
    }

    @Override
    public void error(String message, Exception e) {
        bufferedLogMessages.add(new BufferedLogMessage(message, Level.ERROR, e));
    }

    @Override
    public void notice(String message){
        bufferedLogMessages.add(new BufferedLogMessage(message, Level.NOTICE));
    }

    public void flush(Log target) {
        synchronized (bufferedLogMessages) {
            for (BufferedLog.BufferedLogMessage message : bufferedLogMessages) {
                switch (message.level) {
                    case DEBUG:
                        target.debug(message.message);
                        break;
                    case INFO:
                        target.info(message.message);
                        break;
                    case WARN:
                        target.warn(message.message);
                        break;
                    case NOTICE:
                        target.notice(message.message);
                        break;
                    case ERROR:
                        if (message.e == null) {
                            target.error(message.message);
                        } else {
                            target.error(message.message, message.e);
                        }
                        break;
                }
            }
            bufferedLogMessages.clear();
        }
    }

    public static class BufferedLogMessage {
        public final String message;
        public final Level level;
        public final Exception e;

        public BufferedLogMessage(String message, Level level) {
            this(message, level, null);
        }

        public BufferedLogMessage(String message, Level level, Exception e) {
            this.message = message;
            this.level = level;
            this.e = e;
        }
    }

    public enum Level {
        DEBUG, INFO, WARN, ERROR, NOTICE
    }
}