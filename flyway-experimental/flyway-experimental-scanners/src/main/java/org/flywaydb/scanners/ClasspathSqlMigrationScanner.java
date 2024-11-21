/*-
 * ========================LICENSE_START=================================
 * flyway-experimental-scanners
 * ========================================================================
 * Copyright (C) 2010 - 2024 Red Gate Software Ltd
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
package org.flywaydb.scanners;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import lombok.CustomLog;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.sqlscript.SqlScriptMetadata;
import org.flywaydb.core.internal.util.Pair;

@CustomLog
public class ClasspathSqlMigrationScanner extends BaseSqlMigrationScanner {
    @Override
    public Collection<Pair<LoadableResource, SqlScriptMetadata>> scan(final Location location, final Configuration configuration, final ParsingContext parsingContext) {
        if (!location.isClassPath()){
            return List.of();
        }

        LOG.debug("Scanning for classpath resources at '" + location.getRootPath() + "'");
        
        final ClassLoader classLoader = configuration.getClassLoader();
        List<URL> locationUrls = new ArrayList<>();
        Enumeration<URL> urls;
        try {
            urls = classLoader.getResources(location.getRootPath());
            while (urls.hasMoreElements()) {
                locationUrls.add(urls.nextElement());
            }
        } catch (IOException e) {
            LOG.error("Unable to resolve location " + location + " (ClassLoader: " + classLoader + "): " + e.getMessage() + ".");
        }

        if(locationUrls.isEmpty()) {
            if (configuration.isFailOnMissingLocations()) {
                throw new FlywayException("Failed to find classpath location: " + location.getRootPath());
            }

            LOG.error("Skipping classpath location: " + location.getRootPath());
            return Collections.emptyList();
        }

        for (URL locationUrl: locationUrls) {
            LOG.debug("Scanning URL: " + locationUrl.toExternalForm());

            String protocol = locationUrl.getProtocol();
            if ("file".equals(protocol)) {
                return scanFromFileSystem(new File(locationUrl.getPath()), location, configuration, parsingContext);
            } else if ("jar".equals(protocol)) {
                return scanFromJar();
            }
        }

        return Collections.emptyList();
    }

    @Override
    boolean matchesPath(final String path, final Location location) {
        final String rootPath = new File(Thread.currentThread()
            .getContextClassLoader()
            .getResource(".")
            .getPath()).getAbsolutePath();
        String remainingPath = path.replace("\\", "/").substring(rootPath.length() + 1);

        return location.matchesPath(remainingPath);
    }

    private Collection<Pair<LoadableResource, SqlScriptMetadata>> scanFromJar() {
        return Collections.emptyList();
    }
}
