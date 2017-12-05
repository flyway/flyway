/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.util.scanner.filesystem;

import org.flywaydb.core.internal.util.Location;
import org.junit.Test;

/**
 * Test for FileSystemScanner.
 */
public class FileSystemScannerMediumTest {
    @Test
    public void nonExistentDirectory() throws Exception {
        new FileSystemScanner().scanForResources(new Location("filesystem:/invalid-path"), "", "");
    }
}
