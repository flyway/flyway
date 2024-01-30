package org.flywaydb.core.internal.jdbc;

import lombok.Getter;
import lombok.Setter;
import org.flywaydb.core.api.callback.Error;
import org.flywaydb.core.api.callback.Warning;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Container for all results, warnings, errors and remaining side-effects of a sql statement.
 */
@Getter
public class Results {
    public static final Results EMPTY_RESULTS = new Results();

    private final List<Result> results = new ArrayList<>();
    private final List<Warning> warnings = new ArrayList<>();
    private final List<Error> errors = new ArrayList<>();
    @Setter
    private SQLException exception = null;

    public void addResult(Result result) {
        results.add(result);
    }

    public void addWarning(Warning warning) {
        warnings.add(warning);
    }

    public void addError(Error error) {
        errors.add(error);
    }
}