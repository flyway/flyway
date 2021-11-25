/*
 * Copyright (C) Red Gate Software Ltd 2010-2021
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
package org.flywaydb.core.api.configuration;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Factory class for S3 clients. By default the basic S3 client from the AWS SDK v2 is generated, but this class
 * provides the facility to configure Flyway with a custom client (eg. <a href="https://github.com/localstack/localstack">Localstack</a>)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class S3ClientFactory {

    @Setter
    private static S3Client client = null;

    public static S3Client getClient() {
        if (client != null) {
            return client;
        }
        return S3Client.create();
    }
}