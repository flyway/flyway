/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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
package org.flywaydb.core.api.output;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
public class HtmlResult implements OperationResult {
    @Getter
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

    protected HtmlResult(LocalDateTime timestamp, String operation) {
        setTimestamp(timestamp);
        this.operation = operation;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp.toString();
    }

    public LocalDateTime getTimestamp() {
        return LocalDateTime.parse(timestamp);
    }

    public void setException(Exception exception) {
        if (exception != null) {
            this.exceptionObject = exception;
            this.exception = exception.getMessage();
        }
    }
}