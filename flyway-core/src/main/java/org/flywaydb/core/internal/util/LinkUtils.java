/*
 * Copyright © Red Gate Software Ltd 2010-2021
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
package org.flywaydb.core.internal.util;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.license.VersionPrinter;

public class LinkUtils {
    public static String createFlywayDbWebsiteLinkWithRef(String ref, String... pathParts) {
        String link = createFlywayDbWebsiteLink(pathParts);

        if (link.contains("?ref=v")) {
            link += "_" + ref;
        } else {
            link += "?ref=" + ref;
        }

        return link;
    }

    public static String createFlywayDbWebsiteLink(String... pathParts) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("https://flywaydb.org/");

        for (String pathPart : pathParts) {
            stringBuilder.append(pathPart).append("/");
        }

        try {
            MigrationVersion current = MigrationVersion.fromVersion(VersionPrinter.getVersion());
            stringBuilder.append("?ref=v").append(current);
        } catch (Exception e) {
            // Ignore failure to parse version
        }

        return stringBuilder.toString();
    }
}