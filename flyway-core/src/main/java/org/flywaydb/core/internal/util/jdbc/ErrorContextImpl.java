package org.flywaydb.core.internal.util.jdbc;

import org.flywaydb.core.api.errorhandler.Error;
import org.flywaydb.core.api.errorhandler.ErrorContext;
import org.flywaydb.core.api.errorhandler.Warning;

import java.util.ArrayList;
import java.util.List;

public class ErrorContextImpl implements ErrorContext {
    private final List<Warning> warnings = new ArrayList<Warning>();
    private final List<Error> errors = new ArrayList<Error>();

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
