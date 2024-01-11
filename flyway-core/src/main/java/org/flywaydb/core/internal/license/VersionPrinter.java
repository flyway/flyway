package org.flywaydb.core.internal.license;

import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.FileUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@CustomLog
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VersionPrinter {

    @Getter
    private static final String version = readVersion();

    private static String readVersion() {
        try {
            return FileUtils.copyToString(
                    VersionPrinter.class.getClassLoader().getResourceAsStream("org/flywaydb/core/internal/version.txt"),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new FlywayException("Unable to read Flyway version: " + e.getMessage(), e);
        }
    }
}