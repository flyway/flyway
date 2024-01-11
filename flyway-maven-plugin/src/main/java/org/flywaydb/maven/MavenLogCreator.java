package org.flywaydb.maven;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogCreator;
import org.apache.maven.plugin.AbstractMojo;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class MavenLogCreator implements LogCreator {

    private final AbstractMojo mojo;

    public Log createLogger(Class<?> clazz) {
        return new MavenLog(mojo.getLog());
    }
}