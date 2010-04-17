package com.google.code.flyway.core.sql;

import static org.junit.Assert.assertEquals;

import com.google.code.flyway.core.sql.SqlMigration;
import org.junit.Test;

/**
 * Testcase for SqlMigration.
 */
public class SqlFileMigrationTest {
    /**
     * Test for extractVersionStringFromFileName.
     */
    @Test
    public void extractVersionStringFromFileName() {
        assertEquals("V8_0", SqlMigration.extractVersionStringFromFileName("sql/V8_0.sql"));
        assertEquals("V9_0", SqlMigration.extractVersionStringFromFileName("sql/V9_0-CommentAboutContents.sql"));
    }
}
