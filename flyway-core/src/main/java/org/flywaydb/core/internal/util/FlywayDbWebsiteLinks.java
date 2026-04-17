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

// Non-documentation links (product pages, external sites, surveys) use direct URLs.
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FlywayDbWebsiteLinks {
    public static final String TRY_TEAMS_EDITION = "https://rd.gt/2VzHpkY";
    public static final String REDGATE_EDITION_DOWNLOAD = "https://rd.gt/3GGIXhh";
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
    public static final String TRIAL_UPGRADE = "https://rd.gt/2WNixqj";
    public static final String KNOWN_PARSER_LIMITATIONS = helpUrl(Topic.KNOWN_PARSER_LIMITATIONS);
    public static final String TEAMS_FEATURES_FOR_BIG_QUERY = helpUrl(Topic.TEAMS_FEATURES_FOR_BIG_QUERY);
    public static final String TEAMS_FEATURES_FOR_CLOUD_SPANNER = helpUrl(Topic.TEAMS_FEATURES_FOR_CLOUD_SPANNER);
    public static final String FILE_ENCODING_HELP = helpUrl(Topic.FILE_ENCODING_HELP);
    public static final String TEAMS_ENTERPRISE_DOWNLOAD = helpUrl(Topic.TEAMS_ENTERPRISE_DOWNLOAD);
    public static final String SQLFLUFF_CONFIGURATION = "https://rd.gt/3dDFeFN";
    public static final String UPGRADE_TO_REDGATE_FLYWAY = "https://rd.gt/3ytplJq";
    public static final String CHANGES_REPORT_LEARN_MORE = helpUrl(Topic.CHANGES_REPORT);
    public static final String CODE_ANALYSIS_LEARN_MORE = helpUrl(Topic.CODE_ANALYSIS);
    public static final String DRIFT_REPORT_LEARN_MORE = helpUrl(Topic.DRIFT_REPORT);
    public static final String DRY_RUN_REPORT_LEARN_MORE = helpUrl(Topic.DRY_RUN_REPORT);
    public static final String INFO_REPORT_LEARN_MORE = helpUrl(Topic.INFO_REPORT);
    public static final String MIGRATION_REPORT_LEARN_MORE = helpUrl(Topic.MIGRATION_REPORT);
    public static final String GIVE_FEEDBACK = "https://rd.gt/41oQMAG";
    public static final String TOML_HELP = "https://rd.gt/45ynYIt";
    public static final String EULA_LINK = "https://www.red-gate.com/eula";
    public static final String COMMUNITY_SUPPORT = "https://rd.gt/468B6ni";
    public static final String MIGRATIONS = helpUrl(Topic.MIGRATIONS);
    public static final String DATABASE_TROUBLESHOOTING = helpUrl(Topic.DATABASE_TROUBLESHOOTING);
    public static final String SNOWFLAKE = helpUrl(Topic.SNOWFLAKE);

    public static final String ENTERPRISE_INFO = "https://www.red-gate.com/products/flyway/enterprise/";
    public static final String RELEASE_NOTES = helpUrl(Topic.RELEASE_NOTES);

    public static final String COMMUNITY_CONTRIBUTED_DATABASES = "https://rd.gt/3SXtLDt";

    public static final String ORACLE_DATABASE = helpUrl(Topic.ORACLE_DATABASE);
    public static final String SQL_PLUS = "https://rd.gt/42ljPXB";
    public static final String ORACLE_BLOG = "https://rd.gt/3EuAlwU";

    public static final String LICENSING_ACTIVATING_CLI = helpUrl(Topic.LICENSING_ACTIVATING_CLI);
    public static final String OFFLINE_LICENSE_PERMITS = helpUrl(Topic.OFFLINE_LICENSE_PERMITS);
    public static final String V10_BLOG = "https://rd.gt/4hA3C7Y";
    public static final String MONGOSH = "https://rd.gt/3VudXc6";
    public static final String OSS_DOCKER_REPOSITORY = "https://rd.gt/3OSaoZA";

    public static final String NATIVE_CONNECTORS_MONGODB = helpUrl(Topic.NATIVE_CONNECTORS_MONGODB);
    public static String FEEDBACK_SURVEY_LINK = "";
    public static final String FEEDBACK_SURVEY_LINK_ENTERPRISE = "https://rd.gt/41g7TY9";
    public static final String FEEDBACK_SURVEY_LINK_COMMUNITY = "https://rd.gt/4jVBMEa";

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
