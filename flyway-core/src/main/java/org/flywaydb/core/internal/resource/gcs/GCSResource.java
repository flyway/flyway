/*
 * Copyright © Red Gate Software Ltd 2010-2020
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
package org.flywaydb.core.internal.resource.gcs;

import com.google.cloud.storage.Blob;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.resource.LoadableResource;

import java.io.Reader;
import java.nio.channels.Channels;
import java.nio.charset.Charset;

public class GCSResource extends LoadableResource {

    private static final Log LOG = LogFactory.getLog(GCSResource.class);

    private final Blob blob;
    private final Charset encoding;

    public GCSResource(Blob blob, Charset encoding) {
        this.blob = blob;
        this.encoding = encoding;
    }

    @Override
    public Reader read() {
        try {
            return Channels.newReader(blob.reader(), encoding.name());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new FlywayException("Failed to get object from gcs: " + e.getMessage(), e);
        }
    }

    @Override
    public String getAbsolutePath() {
        return blob.getBucket().concat("/").concat(blob.getName());
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
        String objectName = blob.getName();
        return objectName.substring(objectName.lastIndexOf('/') + 1);
    }

    @Override
    public String getRelativePath() {
        return getAbsolutePath();
    }
} 