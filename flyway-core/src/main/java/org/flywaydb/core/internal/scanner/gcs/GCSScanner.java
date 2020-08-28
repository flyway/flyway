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
package org.flywaydb.core.internal.scanner.gcs;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.resource.LoadableResource;
import org.flywaydb.core.internal.resource.gcs.GCSResource;
import software.amazon.awssdk.core.exception.SdkClientException;

import java.nio.charset.Charset;
import java.util.*;

public class GCSScanner {

    private static final Log LOG = LogFactory.getLog(org.flywaydb.core.internal.scanner.gcs.GCSScanner.class);

    private final Charset encoding;

    /**
     * Creates a new GCS scanner.
     *
     * @param encoding The encoding to use.
     */
    public GCSScanner(Charset encoding) {
        this.encoding = encoding;
    }

    public Collection<LoadableResource> scanForResources(final Location location) {
        if (System.getenv("GOOGLE_APPLICATION_CREDENTIALS") == null) {
            LOG.error("Can't read location " + location + "; GOOGLE_APPLICATION_CREDENTIALS environment variable not set");
            return Collections.emptyList();
        }

        String bucketName = getBucketName(location);
        String prefix = getPrefix(bucketName, location.getPath());

        try {
            Storage storage = StorageOptions.getDefaultInstance().getService();

            Bucket bucket = storage.get(bucketName);

            return getLoadableResources(bucketName, bucket.list());
        } catch (SdkClientException e) {
            LOG.error("Skipping gcs location:" + bucketName + prefix + " due to error: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private Collection<LoadableResource> getLoadableResources(String bucketName,  Page<Blob> listObjectResult) {
        Set<LoadableResource> resources = new TreeSet<>();
        for (Blob blob : listObjectResult.iterateAll()) {
            LOG.debug("Found GCS resource: " +
                    bucketName.concat("/").concat(blob.getName()));
            resources.add(new GCSResource(blob, encoding));
        }
        return resources;
    }

    private String getPrefix(String bucketName, String path) {
        String relativePathToBucket = path.substring(bucketName.length());
        if (relativePathToBucket.startsWith("/")) {
            relativePathToBucket = relativePathToBucket.substring(1);
        }
        if (relativePathToBucket.isEmpty()) {
            return null;
        }
        return relativePathToBucket;
    }

    private String getBucketName(final Location location) {
        int index = location.getPath().indexOf("/");

        if (index >= 0) {
            return location.getPath().substring(0, location.getPath().indexOf("/"));
        }

        // in top level of bucket
        return location.getPath();
    }
} 