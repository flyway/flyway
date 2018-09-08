/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.internal.jdbc;

import org.flywaydb.core.api.callback.Error;
import org.flywaydb.core.api.callback.Warning;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Container for all results, warnings, errors and remaining side-effects of a sql statement.
 */
public class Results {
    private final List<Result> results = new ArrayList<>();
    private final List<Warning> warnings = new ArrayList<>();
    private final List<Error> errors = new ArrayList<>();
    private SQLException exception;

    public void addResult(Result result) {
        results.add(result);
    }

    public void addWarning(Warning warning) {
        warnings.add(warning);
    }

    public void addError(Error error) {
        errors.add(error);
    }

    public List<Warning> getWarnings() {
        return warnings;
    }

    public List<Error> getErrors() {
        return errors;
    }

    public List<Result> getResults() {
        return results;
    }

    public SQLException getException() {
        return exception;
    }

    public void setException(SQLException exception) {
        this.exception = exception;
    }
}