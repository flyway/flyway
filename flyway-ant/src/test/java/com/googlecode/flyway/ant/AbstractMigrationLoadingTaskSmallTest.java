package com.googlecode.flyway.ant;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class AbstractMigrationLoadingTaskSmallTest {
    @Test
    public void adjustRelativeFileSystemLocationToBaseDir() {
        String root = File.listRoots()[0].getPath();

        File baseDir = new File("/tempo");
        assertEquals("db/migration",
                AbstractMigrationLoadingTask.adjustRelativeFileSystemLocationToBaseDir(baseDir, "db/migration"));

        assertEquals("filesystem:" + root + "test/migration",
                AbstractMigrationLoadingTask.adjustRelativeFileSystemLocationToBaseDir(baseDir, "filesystem:" + root + "test/migration"));

        assertEquals("filesystem:" + root + "tempo/test/migration",
                AbstractMigrationLoadingTask.adjustRelativeFileSystemLocationToBaseDir(baseDir, "filesystem:test/migration"));
    }
}
