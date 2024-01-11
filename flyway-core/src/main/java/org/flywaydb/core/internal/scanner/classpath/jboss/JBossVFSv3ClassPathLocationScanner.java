package org.flywaydb.core.internal.scanner.classpath.jboss;

import lombok.CustomLog;
import org.flywaydb.core.internal.util.UrlUtils;
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
@CustomLog
public class JBossVFSv3ClassPathLocationScanner implements ClassPathLocationScanner {
    public Set<String> findResourceNames(String location, URL locationUrl) {
        String filePath = UrlUtils.toFilePath(locationUrl);
        String classPathRootOnDisk = filePath.substring(0, filePath.length() - location.length());
        if (!classPathRootOnDisk.endsWith("/")) {
            classPathRootOnDisk = classPathRootOnDisk + "/";
        }
        LOG.debug("Scanning starting at classpath root on JBoss VFS: " + classPathRootOnDisk);

        Set<String> resourceNames = new TreeSet<String>();

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