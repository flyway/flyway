/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.internal.scanner.android;

import android.content.Context;
import dalvik.system.DexFile;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.android.ContextHolder;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.resource.LoadableResource;
import org.flywaydb.core.internal.resource.android.AndroidResource;
import org.flywaydb.core.internal.scanner.classpath.ResourceAndClassScanner;
import org.flywaydb.core.internal.util.ClassUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

/**
 * Class & resource scanner for Android.
 */
public class AndroidScanner implements ResourceAndClassScanner {
    private static final Log LOG = LogFactory.getLog(AndroidScanner.class);

    private final Context context;

    private final ClassLoader classLoader;
    private final Charset encoding;
    private final Location location;

    public AndroidScanner(ClassLoader classLoader, Charset encoding, Location location) {
        this.classLoader = classLoader;
        this.encoding = encoding;
        this.location = location;
        context = ContextHolder.getContext();
        if (context == null) {
            throw new FlywayException("Unable to scan for Migrations! Context not set. " +
                    "Within an activity you can fix this with org.flywaydb.core.api.android.ContextHolder.setContext(this);");
        }
    }

    @Override
    public Collection<LoadableResource> scanForResources() {
        List<LoadableResource> resources = new ArrayList<>();

        String path = location.getPath();
        try {
            for (String asset : context.getAssets().list(path)) {
                resources.add(new AndroidResource(location, context.getAssets(), path, asset, encoding));
            }
        } catch (IOException e) {
            LOG.warn("Unable to scan for resources: " + e.getMessage());
        }

        return resources;
    }

    @Override
    public Collection<Class<?>> scanForClasses() {
        String pkg = location.getPath().replace("/", ".");

        List<Class<?>> classes = new ArrayList<Class<?>>();
        String sourceDir = context.getApplicationInfo().sourceDir;
        DexFile dex = null;
        try {
            dex = new DexFile(sourceDir);
            Enumeration<String> entries = dex.entries();
            while (entries.hasMoreElements()) {
                String className = entries.nextElement();
                if (className.startsWith(pkg)) {
                    Class<?> clazz = ClassUtils.loadClass(className, classLoader);
                    if (clazz != null) {
                        classes.add(clazz);
                    }
                }
            }
        } catch (IOException e) {
            LOG.warn("Unable to scan DEX file (" + sourceDir + "): " + e.getMessage());
        } finally {
            if (dex != null) {
                try {
                    dex.close();
                } catch (IOException e) {
                    LOG.debug("Unable to close DEX file (" + sourceDir + "): " + e.getMessage());
                }
            }
        }
        return classes;
    }
}