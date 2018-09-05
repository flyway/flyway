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

import org.flywaydb.core.internal.util.UrlUtils;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.scanner.classpath.ClassPathLocationScanner;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * ClassPathLocationScanner for JBoss VFS v3.
 */
public class JBossVFSv3ClassPathLocationScanner implements ClassPathLocationScanner {
    private static final Log LOG = LogFactory.getLog(JBossVFSv3ClassPathLocationScanner.class);

    public Set<String> findResourceNames(String location, URL locationUrl) {
        String filePath = UrlUtils.toFilePath(locationUrl);
        String classPathRootOnDisk = filePath.substring(0, filePath.length() - location.length());
        if (!classPathRootOnDisk.endsWith("/")) {
            classPathRootOnDisk = classPathRootOnDisk + "/";
        }
        LOG.debug("Scanning starting at classpath root on JBoss VFS: " + classPathRootOnDisk);

        Set<String> resourceNames = new TreeSet<>();

        List<VirtualFile> files;
        try {
            files = VFS.getChild(filePath).getChildrenRecursively(new VirtualFileFilter() {
                public boolean accepts(VirtualFile file) {
                    return file.isFile();
                }
            });
            for (VirtualFile file : files) {
                resourceNames.add(file.getPathName().substring(classPathRootOnDisk.length()));
            }
        } catch (IOException e) {
            LOG.warn("Unable to scan classpath root (" + classPathRootOnDisk + ") using JBoss VFS: " + e.getMessage());
        }

        return resourceNames;
    }

}