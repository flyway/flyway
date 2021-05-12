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
package org.flywaydb.commandline;

import com.google.gson.Gson;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.license.VersionPrinter;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.ResultSet;


public class VersionChecker {
    private static class MavenResponse {
        public MavenDocs[] docs;
    }

    private static class MavenDocs {
        public String latestVersion;
    }

    private static class MavenObject {
        public MavenResponse response;
    }

    private static final String FlywayUrl = "https://search.maven.org/solrsearch/select?q=a:flyway-core";
    private static final Log LOG = LogFactory.getLog(VersionPrinter.class);

    public static void checkForVersionUpdates() {
        HttpsURLConnection connection = null;

        try {
            URL url = new URL(FlywayUrl);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            StringBuilder response = new StringBuilder();

            try(BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = rd.readLine()) != null) {
                    response.append(line).append('\r');
                }
            }

            MavenObject obj = new Gson().fromJson(response.toString(), MavenObject.class);

            MigrationVersion current = MigrationVersion.fromVersion(VersionPrinter.getVersion());
            MigrationVersion latest = MigrationVersion.fromVersion(obj.response.docs[0].latestVersion);

            if (current.compareTo(latest) < 0) {
                LOG.warn("You are on an older version of Flyway. Please download latest at https://www.flywaydb.org/download/community");
            }
        } catch (Exception e) {
            // Ignored
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
    }

}