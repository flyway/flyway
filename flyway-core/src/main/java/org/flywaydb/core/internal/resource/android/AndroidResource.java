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
package org.flywaydb.core.internal.resource.android;

import android.content.res.AssetManager;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.internal.resource.LoadableResource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * Resource within an Android App.
 */
public class AndroidResource extends LoadableResource {
    private final AssetManager assetManager;
    private final String fileName;
    private final String fileNameWithAbsolutePath;
    private final String fileNameWithRelativePath;
    private final Charset encoding;

    public AndroidResource(Location location, AssetManager assetManager, String path, String name, Charset encoding) {
        this.assetManager = assetManager;
        this.fileNameWithAbsolutePath = path + "/" + name;
        this.fileName = name;
        this.fileNameWithRelativePath = location == null ? fileNameWithAbsolutePath : location.getPathRelativeToThis(fileNameWithAbsolutePath);
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
        return null;
    }

    @Override
    public Reader read() {
        try {
            return new InputStreamReader(assetManager.open(fileNameWithAbsolutePath), encoding.newDecoder());
        } catch (IOException e) {
            throw new FlywayException("Unable to read asset: " + getAbsolutePath(), e);
        }
    }

    @Override
    public String getFilename() {
        return fileName;
    }
}