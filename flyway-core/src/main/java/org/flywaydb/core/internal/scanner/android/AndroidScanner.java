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
import android.content.SharedPreferences;
import android.os.Build;
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

import java.io.File;
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



        /*
         * Android applications can consist of multiple dex files (c.f. https://developer.android.com/studio/build/multidex)
         * Because of this, we can't only scan the primary dex file at sourceDir.
         * If we did, we might miss Java-based migrations that are located in the secondary dex files.
         * Apps which target API 21+ _should_ be fine as their dex files are precompiled into a single .oat file, but
         * apps which target 20 or lower (e.g. KitKat) will potentially have this issue.
         */
        List<DexFile> dexFiles = new ArrayList<>();
        List<File> searchPaths = new ArrayList<>();

        try {
            //There's always at least one dex file, located at sourceDir
            dexFiles.add(new DexFile(sourceDir));
        } catch (IOException e) {
            LOG.error("Unable to load dex file " + sourceDir, e);
        }

        String dexPrefix = new File(sourceDir).getName() + ".classes";

        //The location where the secondary dexes are stored has changed over time and across phone models
        searchPaths.add(new File(context.getFilesDir(), "secondary-dexes"));
        searchPaths.add(new File(new File(context.getApplicationInfo().dataDir, "code_cache"), "secondary-dexes"));

        //How many secondary dex files do we expect?
        SharedPreferences prefs = context.getSharedPreferences("multidex.version", Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
            ? Context.MODE_PRIVATE
            : Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
        //If this preference doesn't exist, assume that means there's only the primary dex file
        int totalDexes = prefs.getInt("dex.number", 1);

        //We need a writeable directory to load dex files into - use the cache directory to avoid stomping user files
        File scratchDir = context.getCacheDir();

        //If there really is only a single dex file, this entire loop is skipped
        tryDexes: for(int secondaryIndex = 2; secondaryIndex <= totalDexes; secondaryIndex++) {
            String expectedZip = dexPrefix + secondaryIndex + ".zip";
            File temp = new File(scratchDir, dexPrefix + secondaryIndex + ".tmp");

            try {
                tryPossibilities: for(File possibility : searchPaths) {
                    File zip = new File(possibility, expectedZip);
                    if(!zip.exists()) {
                        // Keep looking in our search paths
                        continue tryPossibilities;
                    }
                    dexFiles.add(DexFile.loadDex(zip.getAbsolutePath(), temp.getAbsolutePath(), 0));
                    // Keep looking for more dexes
                    continue tryDexes;
                }
                //The inner loop concluded without loading any dex files - this is unexpected
                LOG.warn("Unable to find a dex file named " + expectedZip);
            } catch (IOException e) {
                LOG.error("Unable to load dex file " + expectedZip, e);
            }
        }

        //Now that we've added all of the dex files we can find, scan them all for classes
        for(DexFile dex : dexFiles) {
            Enumeration<String> entries = dex.entries();
            while(entries.hasMoreElements()) {
                String className = entries.nextElement();
                if(className.startsWith(pkg)) {
                    Class<?> clazz = ClassUtils.loadClass(className, classLoader);
                    if(clazz != null) {
                        classes.add(clazz);
                    }
                }
            }
            try {
                dex.close();
            } catch (IOException e) {
                LOG.error("Unable to close dex file " + dex.getName(), e);
            }
        }

        //Clean up the temp files we created to extract dexes to
        for(int secondaryIndex = 2; secondaryIndex <= totalDexes; secondaryIndex++) {
            File temp = new File(scratchDir, dexPrefix + secondaryIndex + ".tmp");
            boolean success = temp.delete();
            if(!success) {
                LOG.warn("Unable to manually delete temporary file " + temp.getName() + " in cache directory");
            }
        }

        return classes;
    }
}