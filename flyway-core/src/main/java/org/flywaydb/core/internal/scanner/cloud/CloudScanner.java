package org.flywaydb.core.internal.scanner.cloud;

import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.resource.LoadableResource;

import java.nio.charset.Charset;
import java.util.Collection;

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