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
package org.flywaydb.core.internal.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.Topic;
import org.flywaydb.core.internal.license.VersionPrinter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FlywayDbWebsiteLinks {
    public static final String TRY_TEAMS_EDITION = helpUrl(Topic.TRY_TEAMS_EDITION);
    public static final String REDGATE_EDITION_DOWNLOAD = helpUrl(Topic.REDGATE_EDITION_DOWNLOAD);
    public static final String FILTER_INFO_OUTPUT = helpUrl(Topic.FILTER_INFO_OUTPUT);
    public static final String USAGE_COMMANDLINE = helpUrl(Topic.USAGE_COMMAND_LINE);
    public static final String STAYING_UP_TO_DATE = helpUrl(Topic.STAYING_UP_TO_DATE);
    public static final String CUSTOM_VALIDATE_RULES = helpUrl(Topic.CUSTOM_VALIDATE_RULES);
    public static final String IGNORE_MIGRATION_PATTERNS = helpUrl(Topic.IGNORE_MIGRATION_PATTERNS);
    public static final String RESET_THE_BASELINE_MIGRATION = helpUrl(Topic.RESET_THE_BASELINE_MIGRATION);
    public static final String ORACLE_SQL_PLUS = helpUrl(Topic.ORACLE_SQL_PLUS);
    public static final String LOCK_RETRY_COUNT = helpUrl(Topic.LOCK_RETRY_COUNT);
    public static final String WINDOWS_AUTH = helpUrl(Topic.WINDOWS_AUTH);
    public static final String AZURE_ACTIVE_DIRECTORY = helpUrl(Topic.AZURE_ACTIVE_DIRECTORY);
    public static final String TRIAL_UPGRADE = helpUrl(Topic.TRIAL_UPGRADE);
    public static final String KNOWN_PARSER_LIMITATIONS = helpUrl(Topic.KNOWN_PARSER_LIMITATIONS);
    public static final String TEAMS_FEATURES_FOR_BIG_QUERY = helpUrl(Topic.TEAMS_FEATURES_FOR_BIG_QUERY);
    public static final String TEAMS_FEATURES_FOR_CLOUD_SPANNER = helpUrl(Topic.TEAMS_FEATURES_FOR_CLOUD_SPANNER);
    public static final String FILE_ENCODING_HELP = helpUrl(Topic.FILE_ENCODING_HELP);
    public static final String TEAMS_ENTERPRISE_DOWNLOAD = helpUrl(Topic.TEAMS_ENTERPRISE_DOWNLOAD);
    public static final String SQLFLUFF_CONFIGURATION = helpUrl(Topic.SQLFLUFF_CONFIGURATION);
    public static final String UPGRADE_TO_REDGATE_FLYWAY = helpUrl(Topic.UPGRADE_TO_REDGATE_FLYWAY);
    public static final String CHANGES_REPORT_LEARN_MORE = helpUrl(Topic.CHANGES_REPORT);
    public static final String CODE_REVIEW_LEARN_MORE = helpUrl(Topic.CODE_REVIEW);
    public static final String DRIFT_REPORT_LEARN_MORE = helpUrl(Topic.DRIFT_REPORT);
    public static final String DRY_RUN_REPORT_LEARN_MORE = helpUrl(Topic.DRY_RUN_REPORT);
    public static final String INFO_REPORT_LEARN_MORE = helpUrl(Topic.INFO_REPORT);
    public static final String MIGRATION_REPORT_LEARN_MORE = helpUrl(Topic.MIGRATION_REPORT);
    public static final String GIVE_FEEDBACK = helpUrl(Topic.GIVE_FEEDBACK);
    public static final String TOML_HELP = helpUrl(Topic.TOML_HELP);
    public static final String EULA_LINK = helpUrl(Topic.EULA);
    public static final String COMMUNITY_SUPPORT = helpUrl(Topic.COMMUNITY_SUPPORT);
    public static final String MIGRATIONS = helpUrl(Topic.MIGRATIONS);
    public static final String DATABASE_TROUBLESHOOTING = helpUrl(Topic.DATABASE_TROUBLESHOOTING);
    public static final String SNOWFLAKE = helpUrl(Topic.SNOWFLAKE);

    public static final String ENTERPRISE_INFO = helpUrl(Topic.ENTERPRISE_INFO);
    public static final String RELEASE_NOTES = helpUrl(Topic.RELEASE_NOTES);

    public static final String COMMUNITY_CONTRIBUTED_DATABASES = helpUrl(Topic.COMMUNITY_CONTRIBUTED_DATABASES);

    public static final String ORACLE_DATABASE = helpUrl(Topic.ORACLE_DATABASE);
    public static final String SQL_PLUS = helpUrl(Topic.SQL_PLUS_DOWNLOAD);
    public static final String ORACLE_BLOG = helpUrl(Topic.ORACLE_BLOG);

    public static final String LICENSING_ACTIVATING_CLI = helpUrl(Topic.LICENSING_ACTIVATING_CLI);
    public static final String OFFLINE_LICENSE_PERMITS = helpUrl(Topic.OFFLINE_LICENSE_PERMITS);
    public static final String V10_BLOG = helpUrl(Topic.V10_BLOG);
    public static final String MONGOSH = helpUrl(Topic.MONGOSH);
    public static final String OSS_DOCKER_REPOSITORY = helpUrl(Topic.OSS_DOCKER_REPOSITORY);

    public static String FEEDBACK_SURVEY_LINK = "";
    public static final String FEEDBACK_SURVEY_LINK_ENTERPRISE = helpUrl(Topic.FEEDBACK_SURVEY_ENTERPRISE);
    public static final String FEEDBACK_SURVEY_LINK_COMMUNITY = helpUrl(Topic.FEEDBACK_SURVEY_COMMUNITY);

    private static String helpUrl(final Topic topic) {
        return getRedirectLinkFromTopic(topic).toString();
    }

    public static URL getRedirectLinkFromSlug(final String slug) {
        try {
            final String url = "https://help.red-gate.com/help/flyway-cli"
                + VersionPrinter.getMajorVersion()
                + "/help_"
                + VersionPrinter.getMinorVersion()
                + ".aspx?topic="
                + slug;
            return URI.create(url).toURL();
        } catch (final MalformedURLException e) {
            throw new FlywayException(e);
        }
    }

    public static URL getRedirectLinkFromTopic(final Topic topic) {
        return getRedirectLinkFromSlug(topic.getEndpoint());
    }
}
