/*
 * Copyright 2010-2020 Redgate Software Ltd
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
package org.flywaydb.core.internal.scanner.cloud;

import org.flywaydb.core.api.Location;
import org.flywaydb.core.internal.resource.LoadableResource;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.TreeSet;

public abstract class CloudScanner {

    protected Charset encoding;

    public CloudScanner(Charset encoding) {
        this.encoding = encoding;
    }

    public abstract Collection<LoadableResource> scanForResources(final Location location);

    protected String getPrefix(String bucketName, String path) {
        String relativePathToBucket = path.substring(bucketName.length());
        if (relativePathToBucket.startsWith("/")) {
            relativePathToBucket = relativePathToBucket.substring(1);
        }
        if (relativePathToBucket.isEmpty()) {
            return null;
        }
        return relativePathToBucket;
    }

    protected String getBucketName(final Location location) {
        int index = location.getPath().indexOf("/");

        if (index >= 0) {
            return location.getPath().substring(0, location.getPath().indexOf("/"));
        }

        // in top level of bucket
        return location.getPath();
    }
}
