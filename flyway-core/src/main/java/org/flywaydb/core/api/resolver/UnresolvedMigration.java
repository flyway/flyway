package org.flywaydb.core.api.resolver;

/**
 * Migration that fails to resolve because it's invalid
 */
public interface UnresolvedMigration {

    String getValidityMessage();

}
