package com.google.code.flyway.core;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Testcase for SqlFileMigration.
 */
public class SqlFileMigrationTest {
    /**
     * Test for extractVersionStringFromFileName.
     */
    @Test
    public void extractVersionStringFromFileName() {
        assertEquals("V8_0", SqlFileMigration.extractVersionStringFromFileName("sql/V8_0.sql"));
        assertEquals("V9_0", SqlFileMigration.extractVersionStringFromFileName("sql/V9_0-CommentAboutContents.sql"));
    }
}
