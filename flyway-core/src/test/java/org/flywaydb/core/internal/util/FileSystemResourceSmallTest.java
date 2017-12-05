/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.util;

import org.flywaydb.core.internal.util.scanner.filesystem.FileSystemResource;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FileSystemResourceSmallTest {
    @Test
    public void getFilename() throws Exception {
        assertEquals("Mig777__Test.sql", new FileSystemResource("Mig777__Test.sql").getFilename());
        assertEquals("Mig777__Test.sql", new FileSystemResource("folder/Mig777__Test.sql").getFilename());
    }

    @Test
    public void getPath() throws Exception {
        assertEquals("Mig777__Test.sql", new FileSystemResource("Mig777__Test.sql").getLocation());
        assertEquals("folder/Mig777__Test.sql", new FileSystemResource("folder/Mig777__Test.sql").getLocation());
    }
}
