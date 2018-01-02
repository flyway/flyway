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
package org.flywaydb.core.internal.util.scanner.classpath;

import org.flywaydb.core.internal.util.UrlUtils;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Test for FileSystemClassPathLocationScanner.
 */
public class FileSystemLocationScannerMediumTest {
    @Test
    public void findResourceNamesFromFileSystem() throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = UrlUtils.toFilePath(classLoader.getResources("migration").nextElement()) + File.separator;

        Set<String> resourceNames =
                new FileSystemClassPathLocationScanner().findResourceNamesFromFileSystem(path, "sql", new File(path, "sql"));

        assertEquals(4, resourceNames.size());
        String[] names = resourceNames.toArray(new String[4]);
        assertEquals("sql/V1.1__View.sql", names[0]);
        assertEquals("sql/V1_2__Populate_table.sql", names[1]);
        assertEquals("sql/V1__First.sql", names[2]);
        assertEquals("sql/V2_0__Add_foreign_key_and_super_mega_humongous_padding_to_exceed_the_maximum_column_length_in_the_metadata_table.sql", names[3]);
    }

    @Test
    public void findResourceNamesNonExistantPath() throws Exception {
        URL url = new URL("file", null, 0, "X:\\dummy\\sql");

        Set<String> resourceNames =
                new FileSystemClassPathLocationScanner().findResourceNames("sql", url);

        assertEquals(0, resourceNames.size());
    }
}
