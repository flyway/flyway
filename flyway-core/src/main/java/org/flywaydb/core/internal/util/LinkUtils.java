/*
 * Copyright (C) Red Gate Software Ltd 2010-2021
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

    public static String createFlywayDbWebsiteLink(String url, String ref) {
        String link = createFlywayDbWebsiteLink(url);

        if (link.contains("?ref=v")) {
            link += "_" + ref;
        } else {
            link += "?ref=" + ref;
        }

        return link;
    }

    public static String createFlywayDbWebsiteLink(String url) {
        String link = "https://flywaydb.org/" + url;
        try {
            MigrationVersion current = MigrationVersion.fromVersion(VersionPrinter.getVersion());
            link += "?ref=v" + current;
        } catch (Exception e) {
            // Ignore failure to parse version
        }
        return link;
    }
}