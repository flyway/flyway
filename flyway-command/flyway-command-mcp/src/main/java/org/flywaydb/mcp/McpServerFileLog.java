/*-
 * ========================LICENSE_START=================================
 * flyway-command-mcp
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
package org.flywaydb.mcp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.util.JsonUtils;
import tools.jackson.databind.json.JsonMapper;

@RequiredArgsConstructor
public class McpServerFileLog implements Log {
    private final OutputStream stream;
    private final JsonMapper jsonMapper = JsonUtils.getJsonMapper()
        .rebuild()
        .disable(tools.jackson.databind.SerializationFeature.INDENT_OUTPUT)
        .build();

    @Override
    public void debug(final String message) {
        if (LogFactory.isDebugEnabled()) {
            append(makeLine("DEBUG", message));
        }
    }

    @Override
    public void info(final String message) {
        if (!LogFactory.isQuietMode()) {
            append(makeLine("INFO", message));
        }
    }

    @Override
    public void warn(final String message) {
        append(makeLine("WARN", message));
    }

    @Override
    public void error(final String message) {
        append(makeLine("ERROR", message));
    }

    @Override
    public void error(final String message, final Exception e) {
        final String[] stackTrace = Arrays.stream(e.getStackTrace())
            .map(StackTraceElement::toString)
            .toArray(String[]::new);
        append(new LineModel(Instant.now(), "ERROR", message, e.toString(), stackTrace));
    }

    @Override
    public void notice(final String message) {
        if (!LogFactory.isQuietMode()) {
            append(makeLine("NOTICE", message));
        }
    }

    private void append(final LineModel line) {
        try {
            final String json = jsonMapper.writeValueAsString(line) + "\n";
            stream.write(json.getBytes(StandardCharsets.UTF_8));
        } catch (final IOException ignored) {
        }
    }

    private static LineModel makeLine(final String level, final String message) {
        return new LineModel(Instant.now(), level, message, null, null);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({ "time", "level", "message", "exceptionMessage", "stackTrace" })
    private record LineModel(Instant time,
                             String level,
                             String message,
                             String exceptionMessage,
                             String[] stackTrace) {}
}
