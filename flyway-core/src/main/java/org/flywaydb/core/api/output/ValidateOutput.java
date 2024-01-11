package org.flywaydb.core.api.output;

import lombok.AllArgsConstructor;
import org.flywaydb.core.api.ErrorDetails;

@AllArgsConstructor
public class ValidateOutput {
    public final String version;
    public final String description;
    public final String filepath;
    public final ErrorDetails errorDetails;

}