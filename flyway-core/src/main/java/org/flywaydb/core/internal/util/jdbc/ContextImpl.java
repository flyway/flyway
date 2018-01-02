/*
 * Copyright 2010-2018 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
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
