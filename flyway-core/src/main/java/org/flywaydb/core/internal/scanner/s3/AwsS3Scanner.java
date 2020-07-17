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
package org.flywaydb.core.internal.scanner.s3;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.resource.LoadableResource;
import org.flywaydb.core.internal.resource.s3.AwsS3Resource;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

public class AwsS3Scanner {

    private static final Log LOG = LogFactory.getLog(AwsS3Scanner.class);

    private final Charset encoding;

    /**
     * Creates a new AWS S3 scanner.
     *
     * @param encoding The encoding to use.
     */
    public AwsS3Scanner(Charset encoding) {
        this.encoding = encoding;
    }

    /**
     * Scans S3 for the resources. In AWS SDK v2, only the region that the client is configured with can be used.
     * The format of the path is expected to be {@code s3:{bucketName}/{optional prefix}}.
     *
     * @param location The location in S3 to start searching. Subdirectories are also searched.
     * @return The resources that were found.
     */
    public Collection<LoadableResource> scanForResources(final Location location) {
        String bucketName = getBucketName(location);
        String prefix = getPrefix(bucketName, location.getPath());
        S3Client s3Client = S3Client.create();
        try {
            ListObjectsV2Request.Builder builder = ListObjectsV2Request.builder().bucket(bucketName).prefix(prefix);
            ListObjectsV2Request request = builder.build();
            ListObjectsV2Response listObjectResult = s3Client.listObjectsV2(request);
            return getLoadableResources(bucketName, listObjectResult);
        } catch (SdkClientException e) {
            LOG.warn("Skipping s3 location:" + bucketName + prefix + " due to error: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private Collection<LoadableResource> getLoadableResources(String bucketName, final ListObjectsV2Response listObjectResult) {
        List<S3Object> objectSummaries = listObjectResult.contents();
        Set<LoadableResource> resources = new TreeSet<>();
        for (S3Object objectSummary : objectSummaries) {
            LOG.debug("Found Amazon S3 resource: " +
                    bucketName.concat("/").concat(objectSummary.key()));
            resources.add(new AwsS3Resource(bucketName, objectSummary, encoding));
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
        } else { /* in top level of bucket */
            return location.getPath();
        }
    }
} 