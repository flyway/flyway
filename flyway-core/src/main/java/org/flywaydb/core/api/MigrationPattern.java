package org.flywaydb.core.api;

import lombok.RequiredArgsConstructor;

import java.util.regex.Pattern;

@RequiredArgsConstructor
public class MigrationPattern {
    private static final Pattern VERSION_NUMBER_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)*");
    private final String migrationName;

    public boolean matches(MigrationVersion version, String description) {
        String migrationNameAsVersion = migrationName.replace("_", ".");
        if (version != null && isValidVersionNumber(migrationNameAsVersion)) {
            MigrationVersion patternVersion = MigrationVersion.fromVersion(migrationNameAsVersion);
            return patternVersion.equals(version);
        }
        String pattern = migrationName.replace("_", " ");
        return pattern.equals(description);
    }

    @Override
    public String toString() {
        return migrationName;
    }

    @Override
    public int hashCode() {
        return migrationName.hashCode();
    }

    private static boolean isValidVersionNumber(String versionNumber) {
        return VERSION_NUMBER_PATTERN.matcher(versionNumber).matches();
    }
}