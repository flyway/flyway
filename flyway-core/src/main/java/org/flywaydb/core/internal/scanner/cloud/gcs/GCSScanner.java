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
package org.flywaydb.core.internal.scanner.cloud.gcs;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.resource.gcs.GCSResource;
import org.flywaydb.core.internal.scanner.cloud.CloudScanner;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class GCSScanner extends CloudScanner {
    private static final Log LOG = LogFactory.getLog(GCSScanner.class);
    private boolean throwOnMissingLocations;

    /**
     * Creates a new GCS scanner.
     *
     * @param encoding The encoding to use.
     * @param throwOnMissingLocations Whether to throw on missing locations.
     */
    public GCSScanner(Charset encoding, boolean throwOnMissingLocations) {
        super(encoding);
        this.throwOnMissingLocations = throwOnMissingLocations;
    }

    @Override
    public Collection<LoadableResource> scanForResources(final Location location) {
        if (System.getenv("GOOGLE_APPLICATION_CREDENTIALS") == null) {

            String message = "Can't read location " + location + "; GOOGLE_APPLICATION_CREDENTIALS environment variable not set";

            if (throwOnMissingLocations) {
                throw new FlywayException(message);
            }

            LOG.error(message);

            return Collections.emptyList();
        }

        String bucketName = getBucketName(location);

        Storage storage = StorageOptions.getDefaultInstance().getService();
        Bucket bucket = storage.get(bucketName);

        return getLoadableResources(bucketName, bucket.list());
    }

    private Collection<LoadableResource> getLoadableResources(String bucketName, Page<Blob> listObjectResult) {
        Set<LoadableResource> resources = new TreeSet<>();
        for (Blob blob : listObjectResult.iterateAll()) {
            LOG.debug("Found GCS resource: " + bucketName.concat("/").concat(blob.getName()));
            resources.add(new GCSResource(blob, encoding));
        }
        return resources;
    }
}