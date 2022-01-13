/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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
package org.flywaydb.core.internal.resource.s3;

import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.S3ClientFactory;
import org.flywaydb.core.api.resource.LoadableResource;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.Reader;
import java.nio.channels.Channels;
import java.nio.charset.Charset;

@CustomLog
@RequiredArgsConstructor
public class AwsS3Resource extends LoadableResource {
    private final String bucketName;
    private final S3Object s3ObjectSummary;
    private final Charset encoding;

    @Override
    public Reader read() {
        S3Client s3 = S3ClientFactory.getClient();
        try {
            GetObjectRequest.Builder builder = GetObjectRequest.builder().bucket(bucketName).key(s3ObjectSummary.key());
            GetObjectRequest request = builder.build();
            ResponseInputStream o = s3.getObject(request);
            return Channels.newReader(Channels.newChannel(o), encoding.name());
        } catch (AwsServiceException e) {
            LOG.error(e.getMessage(), e);
            throw new FlywayException("Failed to get object from s3: " + e.getMessage(), e);
        }
    }

    @Override
    public String getAbsolutePath() {
        return bucketName.concat("/").concat(s3ObjectSummary.key());
    }

    @Override
    public String getAbsolutePathOnDisk() {
        return getAbsolutePath();
    }

    /**
     * @return The filename of this resource, without the path.
     */
    @Override
    public String getFilename() {
        return s3ObjectSummary.key().substring(s3ObjectSummary.key().lastIndexOf('/') + 1);
    }

    @Override
    public String getRelativePath() {
        return getAbsolutePath();
    }
}