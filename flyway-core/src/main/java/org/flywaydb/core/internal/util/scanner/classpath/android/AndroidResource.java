/*
 * Copyright 2010-2018 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
 */
package org.flywaydb.core.internal.util.scanner.classpath.android;

import android.content.res.AssetManager;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.FileCopyUtils;
import org.flywaydb.core.internal.util.scanner.LoadableResource;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Resource within an Android App.
 */
public class AndroidResource implements LoadableResource {
    private final AssetManager assetManager;
    private final String path;
    private final String name;

    public AndroidResource(AssetManager assetManager, String path, String name) {
        this.assetManager = assetManager;
        this.path = path;
        this.name = name;
    }

    @Override
    public String getLocation() {
        return path + "/" + name;
    }

    @Override
    public String getLocationOnDisk() {
        return null;
    }

    @Override
    public String loadAsString(String encoding) {
        try {
            return FileCopyUtils.copyToString(new InputStreamReader(assetManager.open(getLocation()), encoding));
        } catch (IOException e) {
            throw new FlywayException("Unable to load asset: " + getLocation(), e);
        }
    }

    @Override
    public byte[] loadAsBytes() {
        try {
            return FileCopyUtils.copyToByteArray(assetManager.open(getLocation()));
        } catch (IOException e) {
            throw new FlywayException("Unable to load asset: " + getLocation(), e);
        }
    }

    @Override
    public String getFilename() {
        return name;
    }
}
