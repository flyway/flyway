/**
 * Copyright (C) 2010-2012 the original author or authors.
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
package com.googlecode.flyway.core.util.scanner.jboss;

import com.googlecode.flyway.core.util.scanner.LocationScanner;
import com.googlecode.flyway.core.util.logging.Log;
import com.googlecode.flyway.core.util.logging.LogFactory;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * LocationScanner for JBoss VFS v3.
 */
public class JBossVFSv3LocationScanner implements LocationScanner {
    private static final Log LOG = LogFactory.getLog(JBossVFSv3LocationScanner.class);

    public Set<String> findResourceNames(String location, String locationUrl) throws IOException {
        String classPathRootOnDisk = locationUrl.substring(0, locationUrl.length() - location.length());
        if (!classPathRootOnDisk.endsWith("/")) {
            classPathRootOnDisk = classPathRootOnDisk + "/";
        }
        LOG.debug("Scanning starting at classpath root on JBoss VFS: " + classPathRootOnDisk);

        Set<String> resourceNames = new TreeSet<String>();

        List<VirtualFile> files = VFS.getChild(locationUrl).getChildrenRecursively(new VirtualFileFilter() {
            public boolean accepts(VirtualFile file) {
                return file.isFile();
            }
        });
        for (VirtualFile file : files) {
            resourceNames.add(file.getPathName().substring(classPathRootOnDisk.length()));
        }

        return resourceNames;
    }

}
