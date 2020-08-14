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
package org.flywaydb.core.internal.resource.classpath;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.internal.resource.LoadableResource;
import org.flywaydb.core.internal.util.UrlUtils;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * A resource on the classpath.
 */
public class ClassPathResource extends LoadableResource {
    /**
     * The fileNameWithAbsolutePath of the resource on the classpath.
     */
    private final String fileNameWithAbsolutePath;
    private final String fileNameWithRelativePath;

    /**
     * The ClassLoader to use.
     */
    private final ClassLoader classLoader;
    private final Charset encoding;

    /**
     * Creates a new ClassPathResource.
     *
     * @param fileNameWithAbsolutePath The path and filename of the resource on the classpath.
     * @param classLoader              The ClassLoader to use.
     */
    public ClassPathResource(Location location, String fileNameWithAbsolutePath, ClassLoader classLoader,
                             Charset encoding) {
        this.fileNameWithAbsolutePath = fileNameWithAbsolutePath;
        this.fileNameWithRelativePath = location == null ? fileNameWithAbsolutePath : location.getPathRelativeToThis(fileNameWithAbsolutePath);
        this.classLoader = classLoader;
        this.encoding = encoding;
    }

    @Override
    public String getRelativePath() {
        return fileNameWithRelativePath;
    }

    @Override
    public String getAbsolutePath() {
        return fileNameWithAbsolutePath;
    }

    @Override
    public String getAbsolutePathOnDisk() {
        URL url = getUrl();
        if (url == null) {
            throw new FlywayException("Unable to find resource on disk: " + fileNameWithAbsolutePath);
        }
        return new File(UrlUtils.decodeURL(url.getPath())).getAbsolutePath();
    }

    /**
     * @return The url of this resource.
     */
    private URL getUrl() {
        return classLoader.getResource(fileNameWithAbsolutePath);
    }

    @Override
    public Reader read() {
        InputStream inputStream = classLoader.getResourceAsStream(fileNameWithAbsolutePath);
        if (inputStream == null) {
            throw new FlywayException("Unable to obtain inputstream for resource: " + fileNameWithAbsolutePath);
        }
        return new InputStreamReader(inputStream, encoding.newDecoder());
    }

    @Override
    public String getFilename() {
        return fileNameWithAbsolutePath.substring(fileNameWithAbsolutePath.lastIndexOf("/") + 1);
    }

    public boolean exists() {
        return getUrl() != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClassPathResource that = (ClassPathResource) o;

        return fileNameWithAbsolutePath.equals(that.fileNameWithAbsolutePath);
    }

    @Override
    public int hashCode() {
        return fileNameWithAbsolutePath.hashCode();
    }
}