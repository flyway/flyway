/*-
 * ========================LICENSE_START=================================
 * flyway-locations-s3
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
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
package org.flywaydb.locations.s3;

import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import lombok.CustomLog;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.scanner.FileLocation;
import org.flywaydb.core.internal.scanner.ReadWriteLocationHandler;
import org.flywaydb.core.internal.util.FeatureDetector;

@CustomLog
public class AwsS3LocationHandler implements ReadWriteLocationHandler {
    private static final String AWS_S3_PREFIX = "s3:";

    @Override
    public boolean canHandlePrefix(final String prefix) {
        return AWS_S3_PREFIX.equals(prefix);
    }

    @Override
    public Collection<LoadableResource> scanForResources(final Location location, final Configuration configuration) {
        final FeatureDetector detector = new FeatureDetector(configuration.getClassLoader());
        if (detector.isAwsAvailable()) {
            return new AwsS3Scanner(configuration.getEncoding(),
                configuration.isFailOnMissingLocations()).scanForResources(location);
        } else {
            LOG.error("Can't read location " + location + "; AWS SDK not found");
            return Collections.emptyList();
        }
    }

    @Override
    public boolean handlesWildcards() {
        return false;
    }

    @Override
    public String getPathSeparator() {
        return "/";
    }

    @Override
    public String normalizePath(final String path) {
        return path;
    }

    @Override
    public OutputStream getOutputStream(final FileLocation fileLocation, final Configuration configuration) {
        return new S3OutputStream(fileLocation.path());
    }
}
