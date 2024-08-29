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
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
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
        
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final URL resource = classLoader.getResource(location.getRootPath());
        if(resource == null) {
            if (configuration.isFailOnMissingLocations()) {
                throw new FlywayException("Failed to find classpath location: " + location.getRootPath());
            }

            LOG.error("Skipping classpath location: " + location.getRootPath());
            return Collections.emptyList();
        }
        final File directory = new File(resource.getPath());
        return scan(directory, location, configuration, parsingContext);
    }

    @Override
    boolean matchesPath(final String path, final Location location) {
        final String rootPath = Thread.currentThread().getContextClassLoader().getResource(".").getPath();
        final String remainingPath = path.substring(rootPath.length() - 1).replace("\\", "/");
        return location.matchesPath(remainingPath);
    }
}
