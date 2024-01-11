package org.flywaydb.core.api.output;

import java.util.LinkedList;
import java.util.List;

public class CompositeResult<T extends OperationResult> implements OperationResult {
    public List<T> individualResults = new LinkedList<>();

    public CompositeResult() {}

    public CompositeResult(CompositeResult<T> result) {
        this.individualResults = result.individualResults;
    }
}