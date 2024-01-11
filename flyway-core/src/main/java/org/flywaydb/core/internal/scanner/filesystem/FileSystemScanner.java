package org.flywaydb.core.internal.scanner.filesystem;

import lombok.CustomLog;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.resource.filesystem.FileSystemResource;
import org.flywaydb.core.internal.sqlscript.SqlScriptMetadata;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

@CustomLog
public class FileSystemScanner {
    private final Charset defaultEncoding;
    private final boolean detectEncoding;
    private final boolean throwOnMissingLocations;
    private boolean stream = false;
    private Configuration config;

    public FileSystemScanner(boolean stream, Configuration config) {
        this.defaultEncoding = config.getEncoding();
        this.detectEncoding = config.isDetectEncoding();



        this.throwOnMissingLocations = config.isFailOnMissingLocations();
        this.config = config;
    }

    /**
     * Scans the FileSystem for resources under the specified location, starting with the specified prefix and ending with
     * the specified suffix.
     *
     * @param location The location in the filesystem to start searching. Subdirectories are also searched.
     * @return The resources that were found.
     */
    public Collection<LoadableResource> scanForResources(Location location) {
        String path = location.getRootPath();
        LOG.debug("Scanning for filesystem resources at '" + path + "'");

        File dir = new File(path);

        DirectoryValidationResult validationResult = getDirectoryValidationResult(dir);

        if (validationResult != DirectoryValidationResult.VALID) {
            if (throwOnMissingLocations) {
                throw new FlywayException("Failed to find filesystem location: " + path + " (" + validationResult + ")");
            }

            LOG.error("Skipping filesystem location: " + path + " (" + validationResult + ")");
            return Collections.emptyList();
        }

        Set<LoadableResource> resources = new TreeSet<>();

        for (String resourceName : findResourceNamesFromFileSystem(path, dir)) {
            boolean detectEncodingForThisResource = detectEncoding;
            if (location.matchesPath(resourceName)) {
                Charset encoding = defaultEncoding;
                String encodingBlurb = "";
                if (new File(resourceName + ".conf").exists()) {
                    LoadableResource metadataResource = new FileSystemResource(location, resourceName + ".conf", defaultEncoding, false);
                    SqlScriptMetadata metadata = SqlScriptMetadata.fromResource(metadataResource, null, config);
                    if (metadata.encoding() != null) {
                        encoding = Charset.forName(metadata.encoding());
                        detectEncodingForThisResource = false;
                        encodingBlurb = " (with overriding encoding " + encoding + ")";
                    }
                }
                resources.add(new FileSystemResource(location, resourceName, encoding, detectEncodingForThisResource, stream));

                LOG.debug("Found filesystem resource: " + resourceName + encodingBlurb);
            }
        }

        return resources;
    }

    private DirectoryValidationResult getDirectoryValidationResult(File directory) {
        if (!directory.exists()) {
            return DirectoryValidationResult.NOT_FOUND;
        }
        if (!directory.canRead()) {
            return DirectoryValidationResult.NOT_READABLE;
        }
        if (!directory.isDirectory()) {
            return DirectoryValidationResult.NOT_A_DIRECTORY;
        }
        return DirectoryValidationResult.VALID;
    }

    private Set<String> findResourceNamesFromFileSystem(String scanRootLocation, File folder) {
        String path = folder.getPath();
        LOG.debug("Scanning for resources in path: " + folder.getPath() + " (" + scanRootLocation + ")");

        Set<String> resourceNames = new TreeSet<>();

        File[] files = folder.listFiles();

        if (files == null) {
            if (throwOnMissingLocations) {
                throw new FlywayException("Failed to find filesystem location: " + path + " (" + DirectoryValidationResult.UNABLE_TO_ACCESS_FOLDER + ")");
            }

            LOG.error("Skipping filesystem location: " + path + " (" + DirectoryValidationResult.UNABLE_TO_ACCESS_FOLDER + ")");
            return Collections.emptySet();
        }

        for (File file : files) {
            if (file.canRead()) {
                if (file.isDirectory()) {
                    if (file.isHidden()) {
                        // #1807: Skip hidden directories to avoid issues with Kubernetes
                        LOG.debug("Skipping hidden directory: " + file.getAbsolutePath());
                    } else {
                        resourceNames.addAll(findResourceNamesFromFileSystem(scanRootLocation, file));
                    }
                } else {
                    resourceNames.add(file.getPath());
                }
            }
        }

        return resourceNames;
    }

    private enum DirectoryValidationResult {
        NOT_FOUND,
        NOT_READABLE,
        NOT_A_DIRECTORY,
        UNABLE_TO_ACCESS_FOLDER,
        VALID;

        @Override
        public String toString() {
            return name().toLowerCase().replace('_', ' ');
        }
    }
}