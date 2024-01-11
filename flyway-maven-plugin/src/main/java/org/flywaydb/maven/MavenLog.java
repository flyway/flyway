package org.flywaydb.maven;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.flywaydb.core.api.logging.Log;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class MavenLog implements Log {
    @Delegate(types = Log.class, excludes = ExcludeNotice.class)
    private final org.apache.maven.plugin.logging.Log logger;

    public void notice(String message) {}
}

interface ExcludeNotice {
    void notice(String message);
}