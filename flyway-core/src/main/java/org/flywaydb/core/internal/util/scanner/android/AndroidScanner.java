/**
 * Copyright 2010-2014 Axel Fontaine and the many contributors.
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
package org.flywaydb.core.internal.util.scanner.android;

import android.content.Context;
import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.android.ContextHolder;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.flywaydb.core.internal.util.scanner.Resource;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Class & resource scanner for Android.
 */
public class AndroidScanner {
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

    public Resource[] scanForResources(String path, String prefix, String suffix) throws Exception {
        List<Resource> resources = new ArrayList<Resource>();

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

    public Class<?>[] scanForClasses(String path, Class<?> implementedInterface) throws Exception {
        String pkg = path.replace("/", ".");

        List<Class> classes = new ArrayList<Class>();

        DexFile dex = new DexFile(context.getApplicationInfo().sourceDir);
        Enumeration<String> entries = dex.entries();
        while (entries.hasMoreElements()) {
            String className = entries.nextElement();
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
        return classes.toArray(new Class<?>[classes.size()]);
    }
}
