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
package org.flywaydb.core.internal.util.scanner.classpath.jboss;

import org.flywaydb.core.internal.util.UrlUtils;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.util.scanner.classpath.ClassPathLocationScanner;
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

    public Set<String> findResourceNames(String location, URL locationUrl) throws IOException {
        String filePath = UrlUtils.toFilePath(locationUrl);
        String classPathRootOnDisk = filePath.substring(0, filePath.length() - location.length());
        if (!classPathRootOnDisk.endsWith("/")) {
            classPathRootOnDisk = classPathRootOnDisk + "/";
        }
        LOG.debug("Scanning starting at classpath root on JBoss VFS: " + classPathRootOnDisk);

        Set<String> resourceNames = new TreeSet<String>();

        List<VirtualFile> files = VFS.getChild(filePath).getChildrenRecursively(new VirtualFileFilter() {
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
