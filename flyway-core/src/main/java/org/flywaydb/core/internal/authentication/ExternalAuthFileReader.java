package org.flywaydb.core.internal.authentication;

import java.util.List;

public interface ExternalAuthFileReader {
    /**
     * @return The contents of all resolved auth files.
     */
    List<String> getAllContents();
}