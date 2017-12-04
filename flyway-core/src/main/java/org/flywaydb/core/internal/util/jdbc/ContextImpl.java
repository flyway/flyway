/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.util.jdbc;

import org.flywaydb.core.api.errorhandler.Error;
import org.flywaydb.core.api.errorhandler.Context;
import org.flywaydb.core.api.errorhandler.Warning;

import java.util.ArrayList;
import java.util.List;

public class ContextImpl implements Context {
    private final List<Warning> warnings = new ArrayList<Warning>();
    private final List<Error> errors = new ArrayList<Error>();

    // [pro]
    private Boolean suppressErrors;
    private Boolean serverOutput;
    private String nullText;

    public Boolean getSuppressErrors() {
        return suppressErrors;
    }

    public void setSuppressErrors(Boolean suppressErrors) {
        this.suppressErrors = suppressErrors;
    }

    public Boolean getServerOutput() {
        return serverOutput;
    }

    public void setServerOutput(Boolean serverOutput) {
        this.serverOutput = serverOutput;
    }

    public String getNullText() {
        return nullText;
    }

    public void setNullText(String nullText) {
        this.nullText = nullText;
    }
    // [/pro]

    public void addWarning(Warning warning) {
        warnings.add(warning);
    }

    public void addError(Error error) {
        errors.add(error);
    }

    @Override
    public List<Warning> getWarnings() {
        return warnings;
    }

    @Override
    public List<Error> getErrors() {
        return errors;
    }
}
