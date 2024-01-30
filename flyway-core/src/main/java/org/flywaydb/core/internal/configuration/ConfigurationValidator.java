package org.flywaydb.core.internal.configuration;

import org.flywaydb.core.api.ErrorCode;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;

import java.util.Locale;

public class ConfigurationValidator {
    public void validate(Configuration configuration) {
        if (configuration.isBatch() && configuration.getErrorOverrides().length > 0) {
            throw new FlywayException("flyway.batch configuration option is incompatible with flyway.errorOverrides.\n" +
                                              "It is impossible to intercept the errors in a batch process.\n" +
                                              "Set flyway.batch to false, or remove the error overrides.",
                                      ErrorCode.CONFIGURATION);
        }

        if (configuration.getDataSource() == null) {
            throw new FlywayException(
                    "Unable to connect to the database. Configure the url, user and password!",
                    ErrorCode.CONFIGURATION);
        }

        for (String key : configuration.getPlaceholders().keySet()) {
            if (key.toLowerCase(Locale.ENGLISH).startsWith("flyway:")) {
                throw new FlywayException("Invalid placeholder ('flyway:' prefix is reserved): " + key);
            }
        }
    }
}