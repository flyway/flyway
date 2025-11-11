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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.util.JsonUtils;


public class JsonLog implements Log {
    private JsonMapper mapper;

    private JsonMapper getJsonMapper() {
        if (mapper == null) {
            mapper = JsonUtils.getJsonMapper();
            mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);
        }
        return mapper;
    }

    private void write(String message, LogLevel level) throws JsonProcessingException {
        String[] lines = message.split("\n");
        for (String line : lines) {
            System.err.println(getJsonMapper().writeValueAsString(new JsonLogModel(level, line)));
        }
    }

    @SneakyThrows
    @Override
    public void debug(final String message) {
        if (LogFactory.isJsonLogsEnabled() && LogFactory.isDebugEnabled()) {
            write(message, LogLevel.DEBUG);
        }
    }

    @SneakyThrows
    @Override
    public void info(final String message) {
        if (LogFactory.isJsonLogsEnabled()) {
            write(message, LogLevel.INFO);
        }
    }

    @SneakyThrows
    @Override
    public void warn(final String message) {
        if (LogFactory.isJsonLogsEnabled()) {
            write(message, LogLevel.WARN);
        }
    }

    @SneakyThrows
    @Override
    public void error(final String message) {
        if (LogFactory.isJsonLogsEnabled()) {
            write(message, LogLevel.ERROR);
        }
    }

    @SneakyThrows
    @Override
    //this method will never get hit as command line already handles json exceptions
    public void error(final String message, final Exception e) {
        if (LogFactory.isJsonLogsEnabled()) {
            write(message + " Exception: " + e.getMessage(), LogLevel.ERROR);
        }
    }

    @SneakyThrows
    @Override
    public void notice(final String message) {
        if (LogFactory.isJsonLogsEnabled()) {
            System.err.println(getJsonMapper().writeValueAsString(new JsonLogModel(LogLevel.NOTICE, message)));
        }
    }
}
