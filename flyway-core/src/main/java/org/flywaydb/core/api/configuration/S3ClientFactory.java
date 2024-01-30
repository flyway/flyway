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