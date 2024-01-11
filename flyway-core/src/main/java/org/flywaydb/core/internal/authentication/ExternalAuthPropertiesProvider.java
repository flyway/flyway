package org.flywaydb.core.internal.authentication;

import java.util.Properties;

public interface ExternalAuthPropertiesProvider {
    /**
     * @return Get authentication properties from an external source (e.g. pgpass)
     */
    Properties get();
}