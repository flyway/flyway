/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.core.internal;

import java.util.Optional;

public enum Topic {
    // Help Links
    BASELINE_ON_MIGRATE("baseline-on-migrate"),
    REBASELINING("rebaselining"),
    // Documentation topics (used by FlywayDbWebsiteLinks constants)
    FILTER_INFO_OUTPUT("reference/usage/command-line"),
    USAGE_COMMAND_LINE("reference/usage/command-line"),
    STAYING_UP_TO_DATE("release-notes-and-older-versions/release-notes-for-flyway-engine"),
    CUSTOM_VALIDATE_RULES("flyway-blog/older-posts/customize-validation-rules-with-ignoremigrationpatterns"),
    IGNORE_MIGRATION_PATTERNS("reference/configuration/flyway-namespace/flyway-ignore-migration-patterns-setting"),
    RESET_THE_BASELINE_MIGRATION("flyway-concepts/migrations/baseline-migrations"),
    ORACLE_SQL_PLUS("reference/configuration/flyway-namespace/flyway-oracle-namespace/flyway-oracle-sqlplus-setting"),
    LOCK_RETRY_COUNT("reference/configuration/flyway-namespace/flyway-lock-retry-count-setting"),
    WINDOWS_AUTH("reference/database-driver-reference/sql-server-database"),
    AZURE_ACTIVE_DIRECTORY("reference/database-driver-reference/sql-server-database"),
    KNOWN_PARSER_LIMITATIONS("learn-more-about-flyway/troubleshooting/known-parser-limitations"),
    TEAMS_FEATURES_FOR_BIG_QUERY("reference/database-driver-reference/google-bigquery"),
    TEAMS_FEATURES_FOR_CLOUD_SPANNER("reference/database-driver-reference/google-cloud-spanner"),
    FILE_ENCODING_HELP("reference/configuration/flyway-namespace/flyway-encoding-setting"),
    TEAMS_ENTERPRISE_DOWNLOAD("reference/usage/command-line"),
    CHANGES_REPORT("deploying-database-changes-using-flyway/generating-a-deployment-changes-report"),
    CODE_ANALYSIS("reference/commands/check"),
    DRIFT_REPORT("reference/commands/check"),
    DRY_RUN_REPORT("reference/commands/check"),
    INFO_REPORT("reference/commands/info"),
    MIGRATION_REPORT("reference/commands/migrate"),
    MIGRATIONS("flyway-concepts/migrations"),
    DATABASE_TROUBLESHOOTING("reference/usage/database-troubleshooting"),
    SNOWFLAKE("reference/database-driver-reference/snowflake"),
    RELEASE_NOTES("release-notes-and-older-versions/release-notes-for-flyway-engine"),
    ORACLE_DATABASE("reference/database-driver-reference/oracle-database"),
    LICENSING_ACTIVATING_CLI("getting-started-with-flyway/system-requirements/licensing"),
    OFFLINE_LICENSE_PERMITS("getting-started-with-flyway/system-requirements/licensing/license-permits"),
    NATIVE_CONNECTORS_MONGODB("reference/database-driver-reference/mongodb-native-connectors"),

    // Concepts
    CHECK_CODE("check-code");

    private final String path;
    private final Optional<String> anchor;

    Topic(final String path, final String anchor) {
        this.path = path;
        this.anchor = Optional.of(anchor);
    }

    Topic(final String path) {
        this.path = path;
        this.anchor = Optional.empty();
    }

    public String getEndpoint() {
        return path + anchor.map(a -> "#" + a).orElse("");
    }
}
