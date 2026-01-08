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
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.license.VersionPrinter;










@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FlywayDbWebsiteLinks {
    public static final String TRY_TEAMS_EDITION = "https://rd.gt/2VzHpkY";
    public static final String REDGATE_EDITION_DOWNLOAD = "https://rd.gt/3GGIXhh";
    public static final String FILTER_INFO_OUTPUT = "https://rd.gt/3jqLqAn";
    public static final String USAGE_COMMANDLINE = "https://rd.gt/3Cc1xKC";
    public static final String STAYING_UP_TO_DATE = "https://rd.gt/3rXiSlV";
    public static final String CUSTOM_VALIDATE_RULES = "https://rd.gt/3AbJUZE";
    public static final String IGNORE_MIGRATION_PATTERNS = "https://rd.gt/37m4hXD";
    public static final String RESET_THE_BASELINE_MIGRATION = "https://rd.gt/3CdwkXD";
    public static final String ORACLE_SQL_PLUS = "https://rd.gt/3AYVsQY";
    public static final String LOCK_RETRY_COUNT = "https://rd.gt/3A57jfk";
    public static final String WINDOWS_AUTH = "https://rd.gt/39KICcS";
    public static final String AZURE_ACTIVE_DIRECTORY = "https://rd.gt/3unaRb8";
    public static final String TRIAL_UPGRADE = "https://rd.gt/2WNixqj";
    public static final String KNOWN_PARSER_LIMITATIONS = "https://rd.gt/3ipi7Pm";
    public static final String TEAMS_FEATURES_FOR_BIG_QUERY = "https://rd.gt/3CWAuTb";
    public static final String TEAMS_FEATURES_FOR_CLOUD_SPANNER = "https://rd.gt/2ZvELhV";
    public static final String FILE_ENCODING_HELP = "https://rd.gt/3BzSFhr";
    public static final String TEAMS_ENTERPRISE_DOWNLOAD = "https://rd.gt/3aqhTXb";
    public static final String SQLFLUFF_CONFIGURATION = "https://rd.gt/3dDFeFN";
    public static final String UPGRADE_TO_REDGATE_FLYWAY = "https://rd.gt/3ytplJq";
    public static final String CHANGES_REPORT_LEARN_MORE = "https://rd.gt/43G0r7G";
    public static final String CODE_ANALYSIS_LEARN_MORE = "https://rd.gt/3ornhzX";
    public static final String DRIFT_REPORT_LEARN_MORE = "https://rd.gt/40ikqXf";
    public static final String DRY_RUN_REPORT_LEARN_MORE = "https://rd.gt/3KSXkAW";
    public static final String INFO_REPORT_LEARN_MORE = "https://rd.gt/3UMs1wb";
    public static final String MIGRATION_REPORT_LEARN_MORE = "https://rd.gt/3okl9tM";
    public static final String GIVE_FEEDBACK = "https://rd.gt/41oQMAG";
    public static final String TOML_HELP = "https://rd.gt/45ynYIt";
    public static final String EULA_LINK = "https://www.red-gate.com/eula";
    public static final String COMMUNITY_SUPPORT = "https://rd.gt/468B6ni";
    public static final String MIGRATIONS = "https://rd.gt/4iUkpm1";
    public static final String DATABASE_TROUBLESHOOTING = "https://rd.gt/423f9a6";
    public static final String SNOWFLAKE = "https://rd.gt/4ixiG5r";

    public static final String ENTERPRISE_INFO = "https://www.red-gate.com/products/flyway/enterprise/";
    public static final String RELEASE_NOTES = "https://rd.gt/416ObMi";

    public static final String COMMUNITY_CONTRIBUTED_DATABASES = "https://rd.gt/3SXtLDt";

    public static final String ORACLE_DATABASE = "https://rd.gt/3SISqKJ";
    public static final String SQL_PLUS = "https://rd.gt/42ljPXB";
    public static final String ORACLE_BLOG = "https://rd.gt/3EuAlwU";

    public static final String LICENSING_ACTIVATING_CLI = "https://rd.gt/4p5c9mO";
    public static final String OFFLINE_LICENSE_PERMITS = "https://rd.gt/3UHAJwO";
    public static final String V10_BLOG = "https://rd.gt/4hA3C7Y";
    public static final String MONGOSH = "https://rd.gt/3VudXc6";
    public static final String OSS_DOCKER_REPOSITORY = "https://rd.gt/3OSaoZA";
    
    public static final String NATIVE_CONNECTORS_MONGODB = "https://rd.gt/3CVVLC1";
    public static String FEEDBACK_SURVEY_LINK = "";
    public static final String FEEDBACK_SURVEY_LINK_ENTERPRISE = "https://rd.gt/41g7TY9";
    public static final String FEEDBACK_SURVEY_LINK_COMMUNITY = "https://rd.gt/4jVBMEa";

    public enum Topic {
        RG01("rules/RG01"),
        RG02("rules/RG02"),
        RG03("rules/RG03"),
        RG04("rules/RG04"),
        RG05("rules/RG05"),
        RG06("rules/RG06"),
        RG07("rules/RG07"),
        RG08("rules/RG08"),
        RG09("rules/RG09"),
        RG10("rules/RG10"),
        RG11("rules/RG11"),
        RG12("rules/RG12"),
        RG13("rules/RG13"),
        RG14("rules/RG14"),
        RG15("rules/RG15"),
        RG16("rules/RG16"),
        RX001("rules/RX001"),
        RX002("rules/RX002"),
        RX003("rules/RX003"),
        RX004("rules/RX004"),
        RX005("rules/RX005"),
        RX006("rules/RX006"),
        RX007("rules/RX007"),
        RX008("rules/RX008"),
        RX009("rules/RX009"),
        RX010("rules/RX010"),
        RX011("rules/RX011"),
        RX012("rules/RX012"),
        RX013("rules/RX013"),
        RX014("rules/RX014");

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

    public static Optional<URL> getRedirectLinkFromTopicString(final String topicString) {
        for (final Topic topic : Topic.values()) {
            if (topic.name().equalsIgnoreCase(topicString)) {
                return Optional.of(getRedirectLinkFromTopic(topic));
            }
        }
        return Optional.empty();
    }

    public static URL getRedirectLinkFromTopic(final Topic topic) {
        try {
            final String url = "https://help.red-gate.com/help/flyway-cli"
                + VersionPrinter.getMajorVersion()
                + "/help_"
                + VersionPrinter.getMinorVersion()
                + ".aspx?topic="
                + topic.getEndpoint();
            return URI.create(url).toURL();
        } catch (final MalformedURLException e) {
            throw new FlywayException(e);
        }
    }
}
