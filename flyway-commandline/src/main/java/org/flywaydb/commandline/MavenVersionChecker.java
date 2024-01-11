package org.flywaydb.commandline;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.AccessLevel;
import lombok.Cleanup;
import lombok.CustomLog;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.core.internal.util.FlywayDbWebsiteLinks;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

@CustomLog
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MavenVersionChecker {

    @Setter
    @Getter
    @NoArgsConstructor
    private static class MavenVersioning {
        private String release;
    }

    @Setter
    @Getter
    @NoArgsConstructor
    private static class MavenMetadata {
        private MavenVersioning versioning;
    }

    private static final String FLYWAY_URL =

             "https://repo1.maven.org/maven2/org/flywaydb/flyway-core/maven-metadata.xml";





    public static void checkForVersionUpdates() {
        try {
            URL url = new URL(FLYWAY_URL);
            @Cleanup(value = "disconnect") HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Flyway");
            connection.setConnectTimeout(500);

            StringBuilder response = new StringBuilder();

            try (BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                }
            }

            ObjectMapper xmlMapper = new XmlMapper();
            MavenMetadata metadata = xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                                              .readValue(response.toString(), MavenMetadata.class);

            MigrationVersion current = MigrationVersion.fromVersion(VersionPrinter.getVersion());

            MigrationVersion latest = MigrationVersion.fromVersion(metadata.getVersioning().getRelease());

            if (current.compareTo(latest) < 0) {
                LOG.warn("This version of Flyway is out of date. Upgrade to Flyway " + latest + ": "
                                 + FlywayDbWebsiteLinks.STAYING_UP_TO_DATE + "\n");
            }
        } catch (Exception e) {
            LOG.debug("Unable to check for updates: " + e.getMessage());
        }
    }
}