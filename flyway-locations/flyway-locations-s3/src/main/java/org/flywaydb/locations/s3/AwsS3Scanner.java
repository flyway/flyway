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

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import lombok.CustomLog;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.configuration.S3ClientFactory;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.scanner.cloud.CloudScanner;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

@CustomLog
public class AwsS3Scanner extends CloudScanner {
    private final boolean throwOnMissingLocations;

    /**
     * Creates a new AWS S3 scanner.
     *
     * @param encoding                The encoding to use.
     * @param throwOnMissingLocations whether to throw on missing locations.
     */
    public AwsS3Scanner(final Charset encoding, final boolean throwOnMissingLocations) {
        super(encoding);
        this.throwOnMissingLocations = throwOnMissingLocations;
    }

    /**
     * Scans S3 for the resources. In AWS SDK v2, only the region that the client is configured with can be used. The
     * format of the path is expected to be {@code s3:{bucketName}/{optional prefix}}.
     *
     * @param location The location in S3 to start searching. Subdirectories are also searched.
     * @return The resources that were found.
     */
    @Override
    public Collection<LoadableResource> scanForResources(final Location location) {
        final String bucketName = getBucketName(location);
        final String prefix = getPrefix(bucketName, location.getRootPath());
        final S3Client s3Client = S3ClientFactory.getClient();
        try {
            final ListObjectsV2Request.Builder builder = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix);
            final ListObjectsV2Request request = builder.build();
            final ListObjectsV2Response listObjectResult = s3Client.listObjectsV2(request);
            return getLoadableResources(bucketName, listObjectResult);
        } catch (final SdkClientException e) {

            if (throwOnMissingLocations) {
                throw new FlywayException("Could not access s3 location:"
                    + bucketName
                    + prefix
                    + " due to error: "
                    + e.getMessage());
            }

            LOG.error("Skipping s3 location:" + bucketName + prefix + " due to error: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private Collection<LoadableResource> getLoadableResources(final String bucketName,
        final ListObjectsV2Response listObjectResult) {
        final List<S3Object> objectSummaries = listObjectResult.contents();
        final Collection<LoadableResource> resources = new TreeSet<>();
        for (final S3Object objectSummary : objectSummaries) {
            LOG.debug("Found Amazon S3 resource: " + (bucketName + "/") + objectSummary.key());
            resources.add(new AwsS3Resource(bucketName, objectSummary, encoding));
        }
        return resources;
    }
}
