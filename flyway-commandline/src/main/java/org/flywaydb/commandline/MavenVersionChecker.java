/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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
package org.flywaydb.commandline;

import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.Cleanup;
import lombok.CustomLog;
import lombok.NoArgsConstructor;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.core.internal.util.FlywayDbWebsiteLinks;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;

@CustomLog
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MavenVersionChecker {
    private static class MavenResponse {
        public MavenDoc[] docs;
    }

    private static class MavenDoc {
        public String latestVersion;

        // 'g' is the key for the group id in the Maven REST API
        public String g;
    }

    private static class MavenObject {
        public MavenResponse response;
    }

    private static final String FLYWAY_URL = "https://search.maven.org/solrsearch/select?q=a:flyway-core";

    private static boolean canConnectToMaven() {
        try {
            InetAddress address = InetAddress.getByName("maven.org");
            return address.isReachable(500);
        } catch (Exception e) {
            return false;
        }
    }

    public static void checkForVersionUpdates() {
        if (!canConnectToMaven()) {
            return;
        }

        try {
            URL url = new URL(FLYWAY_URL);
            @Cleanup(value = "disconnect") HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(1000);

            StringBuilder response = new StringBuilder();

            try (BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = rd.readLine()) != null) {
                    response.append(line).append('\r');
                }
            }

            MavenObject obj = new Gson().fromJson(response.toString(), MavenObject.class);

            MigrationVersion current = MigrationVersion.fromVersion(VersionPrinter.getVersion());
            MigrationVersion latest = null;

            String groupID = "org.flywaydb";




            MavenDoc[] mavenDocs = obj.response.docs;
            for (MavenDoc mavenDoc : mavenDocs) {
                if (mavenDoc.g.equals(groupID)) {
                    latest = MigrationVersion.fromVersion(mavenDoc.latestVersion);
                    break;
                }
            }

            if (current.compareTo(latest) < 0) {
                LOG.warn("This version of Flyway is out of date. Upgrade to Flyway " + latest + ": "
                                 + FlywayDbWebsiteLinks.STAYING_UP_TO_DATE + "\n");
            }
        } catch (Exception ignored) {
        }
    }
}