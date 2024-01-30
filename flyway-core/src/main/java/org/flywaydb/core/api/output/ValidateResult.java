package org.flywaydb.core.api.output;

import org.flywaydb.core.api.ErrorDetails;

import java.util.List;
import java.util.stream.Collectors;

public class ValidateResult extends OperationResultBase {
    public final ErrorDetails errorDetails;
    public final List<ValidateOutput> invalidMigrations;
    public final boolean validationSuccessful;
    public final int validateCount;

    public ValidateResult(
            String flywayVersion,
            String database,
            ErrorDetails errorDetails,
            boolean validationSuccessful,
            int validateCount,
            List<ValidateOutput> invalidMigrations,
            List<String> warnings) {
        this.flywayVersion = flywayVersion;
        this.database = database;
        this.errorDetails = errorDetails;
        this.validationSuccessful = validationSuccessful;
        this.validateCount = validateCount;
        this.invalidMigrations = invalidMigrations;
        this.warnings.addAll(warnings);
        this.operation = "validate";
    }

    public String getAllErrorMessages() {
        return invalidMigrations.stream().map(m -> m.errorDetails.errorMessage).collect(Collectors.joining("\n"));
    }
}