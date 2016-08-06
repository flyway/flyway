/**
 * Copyright 2010-2016 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.core.internal.resolver;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.Triplet;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parsing support for migrations that use the standard Flyway version + description embedding in their name. These
 * migrations have names like 1_2__Description .
 */
public class MigrationInfoHelper {

    private static final Log LOG = LogFactory.getLog(MigrationInfoHelper.class);

    private static final String REQUIRED = "required";
    private static final String OPTIONAL = "optional";

    /**
     * Prevents instantiation.
     */
    private MigrationInfoHelper() {
        //Do nothing.
    }

    @Deprecated
    private static Triplet<MigrationVersion, Boolean, String> extractLegacyVersionAndDescriptionAndOptional(String migrationName,
                                                                                                            String prefix, String separator, String suffix){
        String quotedPrefix = Pattern.quote(prefix);
        String quotedSeparator = Pattern.quote(separator);
        String quotedSuffix = Pattern.quote(suffix);

        String regex = quotedPrefix + "(.*?)" + quotedSeparator + "(.+?)" + quotedSuffix;
        Matcher matcher = Pattern.compile(regex).matcher(migrationName);
        if(!matcher.matches()){
            throw new FlywayException("Wrong migration name format: " + migrationName
                    + "(It should look like this: " + prefix + "1_2" + separator + "[" + REQUIRED + "|" + OPTIONAL + "]" + separator + "Description" + suffix + ")");
        }

        String rawVersion = matcher.group(1);
        MigrationVersion version = StringUtils.hasText(rawVersion)? MigrationVersion.fromVersion(rawVersion): null;
        String description = matcher.group(2).replaceAll("_", " ");
        return Triplet.of(version, false, description);
    }

    /**
     * Extracts the schema version and the description from a migration name formatted as 1_2__Description.
     *
     * @param migrationName The migration name to parse. Should not contain any folders or packages.
     * @param prefix        The migration prefix.
     * @param separator     The migration separator.
     * @param suffix        The migration suffix.
     * @return The extracted schema version.
     * @throws FlywayException if the migration name does not follow the standard conventions.
     */
    public static Triplet<MigrationVersion, Boolean, String> extractVersionAndOptionalAndDescription(String migrationName,
                                                                                                     String prefix, String separator, String suffix) {
        String quotedPrefix = Pattern.quote(prefix);
        String quotedSeparator = Pattern.quote(separator);
        String quotedSuffix = Pattern.quote(suffix);

        String regex = quotedPrefix + "(.*?)" + quotedSeparator + "(" + REQUIRED + "|" + OPTIONAL + ")" + quotedSeparator + "(.+?)" + quotedSuffix;
        Matcher matcher = Pattern.compile(regex).matcher(migrationName);
        if(!matcher.matches()){
            LOG.warn("To be removed migration name format: " + migrationName
                    + "(It should look like this: " + prefix + "1_2" + separator + "[" + REQUIRED + "|" + OPTIONAL + "]" + separator + "Description" + suffix + ")");
            return extractLegacyVersionAndDescriptionAndOptional(migrationName, prefix, separator, suffix);
        }

        String rawVersion = matcher.group(1);
        MigrationVersion version = StringUtils.hasText(rawVersion)? MigrationVersion.fromVersion(rawVersion): null;
        boolean optional = OPTIONAL.equalsIgnoreCase(matcher.group(2));
        String description = matcher.group(3).replaceAll("_", " ");
        return Triplet.of(version, optional, description);
    }
}
