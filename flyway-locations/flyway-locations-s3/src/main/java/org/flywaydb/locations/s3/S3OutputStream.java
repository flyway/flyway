/*-
 * ========================LICENSE_START=================================
 * flyway-locations-s3
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import lombok.CustomLog;
import org.flywaydb.core.internal.util.FeatureDetector;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/*
 * For Dry Run output to a S3-based file, analogous to FileOutputStream. This stream accumulates data in a byte
 * array, and on closing flushes the entire contents to S3. This class will only support 5MB of dry run output.
 */
@CustomLog
class S3OutputStream extends ByteArrayOutputStream {
    private final String location;
    private final String bucket;
    private final String key;

    S3OutputStream(final String location) {
        this.location = location;
        final int i = location.indexOf("/");
        this.bucket = location.substring(3, i);
        this.key = location.substring(i + 1);
    }

    @Override
    public void close() throws IOException {
        final FeatureDetector fd = new FeatureDetector(Thread.currentThread().getContextClassLoader());
        if (fd.isAwsAvailable()) {
            final S3Client s3Client = S3ClientFactory.getClient();
            final PutObjectRequest objectRequest = PutObjectRequest.builder().bucket(bucket).key(key).build();
            s3Client.putObject(objectRequest, RequestBody.fromBytes(this.toByteArray()));
        } else {
            LOG.error("Can't write to location " + location + "; AWS SDK not found");
        }
    }
}
