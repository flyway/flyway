package org.flywaydb.core.internal.resolver;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.resolver.UnresolvedMigration;

public class UnresolvedMigrationImpl implements UnresolvedMigration {

    String validityMessage;

    public UnresolvedMigrationImpl(String validityMessage) {
        this.validityMessage = validityMessage;
    }

    @Override
    public String getValidityMessage() {
        return this.validityMessage;
    }
}
