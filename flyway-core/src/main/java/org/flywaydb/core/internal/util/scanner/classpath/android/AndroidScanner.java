/**
 * Copyright 2010-2016 Boxfuse GmbH
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
package org.flywaydb.core.internal.util.scanner.classpath.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.android.ContextHolder;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.flywaydb.core.internal.util.scanner.Resource;
import org.flywaydb.core.internal.util.scanner.classpath.ResourceAndClassScanner;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Class & resource scanner for Android.
 */
public class AndroidScanner implements ResourceAndClassScanner {
    private static final Log LOG = LogFactory.getLog(AndroidScanner.class);

    private final Context context;

    private final PathClassLoader classLoader;

    public AndroidScanner(ClassLoader classLoader) {
        this.classLoader = (PathClassLoader) classLoader;
        context = ContextHolder.getContext();
        if (context == null) {
            throw new FlywayException("Unable to scan for Migrations! Context not set. " +
                    "Within an activity you can fix this with org.flywaydb.core.api.android.ContextHolder.setContext(this);");
        }
    }

    public Resource[] scanForResources(Location location, String prefix, String suffix) throws Exception {
        List<Resource> resources = new ArrayList<Resource>();

        String path = location.getPath();
        for (String asset : context.getAssets().list(path)) {
            if (asset.startsWith(prefix) && asset.endsWith(suffix)
                    && (asset.length() > (prefix + suffix).length())) {
                resources.add(new AndroidResource(context.getAssets(), path, asset));
            } else {
                LOG.debug("Filtering out asset: " + asset);
            }
        }

        return resources.toArray(new Resource[resources.size()]);
    }

    public Class<?>[] scanForClasses(Location location, Class<?> implementedInterface) throws Exception {
        String pkg = location.getPath().replace("/", ".");

        List<Class> classes = new ArrayList<Class>();

        final List<DexFile> dexFiles = new ArrayList<DexFile>();
        
        //Add the primary dex file; always exists
        final DexFile dex = new DexFile(context.getApplicationInfo().sourceDir);
        dexFiles.add(dex);
        
        //Need to look for secondary dex files as well, which may contain migrations
        final File sourceApk = new File(context.getApplicationInfo().sourceDir);
        final String dexPrefix = sourceApk.getName() + ".classes";
        
        //The location of the dex files has changed over time
        final File oldDexDir = new File(context.getFilesDir(), "secondary-dexes");
        final File newDexDir = new File(new File(context.getApplicationInfo().dataDir, "code_cache"), "secondary-dexes");
        
        //Find how many secondary dex files exist
        final SharedPreferences prefs = context.getSharedPreferences("multidex.version", Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                ? Context.MODE_PRIVATE
                : Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
        final int totalDexNumber = prefs.getInt("dex.number", 1);//If there are no secondary files, we'll skip the following loop entirely
        
        //The dex directory is not always writeable; load dex files into the cache directory so we can examine them
        final File tmpDir = context.getCacheDir();
        
        for(int secondaryNumber = 2; secondaryNumber <= totalDexNumber; secondaryNumber++) {
        	final String zipFileName = dexPrefix + secondaryNumber + ".zip";
        	final String tmpFileName = dexPrefix + secondaryNumber + ".tmp";
        	final File tmpFile = new File(tmpDir, tmpFileName);
        	
        	try {
        		final File oldFile = new File(oldDexDir, zipFileName);
        		final File newFile = new File(newDexDir, zipFileName);
        		if(oldFile.exists()) {
        			dexFiles.add(DexFile.loadDex(oldFile.getAbsolutePath(), tmpFile.getAbsolutePath(), 0));
        		} else if(newFile.exists()) {
        			dexFiles.add(DexFile.loadDex(newFile.getAbsolutePath(), tmpFile.getAbsolutePath(), 0));
        		} else {
        			LOG.warn("Could not find a dex file named " + zipFileName);
        		}
        	} catch (IOException e) {
        		LOG.error("Could not load dex file", e);
        	}
        }
        
        for(final DexFile dexFile : dexFiles) {
        	Enumeration<String> dexEntries = dexFile.entries();
            while (dexEntries.hasMoreElements()) {
                String className = dexEntries.nextElement();
                if (className.startsWith(pkg)) {
                    Class<?> clazz = classLoader.loadClass(className);
                    if (Modifier.isAbstract(clazz.getModifiers())) {
                        LOG.debug("Skipping abstract class: " + className);
                        continue;
                    }

                    if (!implementedInterface.isAssignableFrom(clazz)) {
                        continue;
                    }

                    try {
                        ClassUtils.instantiate(className, classLoader);
                    } catch (Exception e) {
                        throw new FlywayException("Unable to instantiate class: " + className);
                    }

                    classes.add(clazz);
                    LOG.debug("Found class: " + className);
                }
            }
        }
        
        return classes.toArray(new Class<?>[classes.size()]);
    }
}
