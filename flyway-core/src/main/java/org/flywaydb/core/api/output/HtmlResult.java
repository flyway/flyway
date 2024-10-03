/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2024 Red Gate Software Ltd
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
package org.flywaydb.core.api.output;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Tolerate;

@NoArgsConstructor
public class HtmlResult implements OperationResult {

    @Setter
    private String timestamp;
    @Getter
    @Setter
    private String operation;
    @Getter
    private String exception;
    public transient Exception exceptionObject;
    @Getter
    @Setter
    private boolean licenseFailed;
    @Getter
    @Setter
    private boolean deleteFileOnJvmExit = false;

    public HtmlResult(final LocalDateTime timestamp, final String operation) {
        setTimestamp(timestamp);
        this.operation = operation;
    }

    @Tolerate
    public void setTimestamp(final LocalDateTime timestamp) {
        this.timestamp = timestamp.toString();
    }

    public LocalDateTime getTimestamp() {
        return LocalDateTime.parse(timestamp);
    }

    public void setException(final Exception exception) {
        if (exception != null) {
            this.exceptionObject = exception;
            this.exception = exception.getMessage();
        }
    }
}
