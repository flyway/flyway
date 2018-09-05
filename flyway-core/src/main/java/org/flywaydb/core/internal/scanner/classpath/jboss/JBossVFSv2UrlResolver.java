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
package org.flywaydb.core.internal.scanner.classpath.jboss;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.scanner.classpath.UrlResolver;

import java.lang.reflect.Method;
import java.net.URL;

/**
 * Resolves JBoss VFS v2 URLs into standard Java URLs.
 */
public class JBossVFSv2UrlResolver implements UrlResolver {
    public URL toStandardJavaUrl(URL url) {
        try {
            Class<?> vfsClass = Class.forName("org.jboss.virtual.VFS");
            Class<?> vfsUtilsClass = Class.forName("org.jboss.virtual.VFSUtils");
            Class<?> virtualFileClass = Class.forName("org.jboss.virtual.VirtualFile");

            Method getRootMethod = vfsClass.getMethod("getRoot", URL.class);
            Method getRealURLMethod = vfsUtilsClass.getMethod("getRealURL", virtualFileClass);

            Object root = getRootMethod.invoke(null, url);
            return (URL) getRealURLMethod.invoke(null, root);
        } catch (Exception e) {
            throw new FlywayException("JBoss VFS v2 call failed", e);
        }
    }
}