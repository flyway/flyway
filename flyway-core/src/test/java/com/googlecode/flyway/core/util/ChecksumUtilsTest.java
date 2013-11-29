package com.googlecode.flyway.core.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.CRC32;


public class ChecksumUtilsTest {
    /**
     * old method to calculate Checksum
     * @param bytes
     * @return
     */
    private static int calculateChecksum(byte[] bytes) {
        final CRC32 crc32 = new CRC32();
        crc32.update(bytes);
        return (int) crc32.getValue();
    }

    /**
     * Compare the old and the new version of checksum
     * @throws IOException
     */
    @org.junit.Test
    public void testCalculateChecksum_backward_compatibility() throws IOException {

        String path = UrlUtils.toFilePath(getClass().getClassLoader().getResource("migration/subdir/V1_1__Populate_table.sql"));
        FileInputStream inputStream = new FileInputStream(path);
        int oldResult = calculateChecksum(FileCopyUtils.copyToByteArray(inputStream));

        FileSystemResource resource = new FileSystemResource(path);
        int newResult = ChecksumUtils.calculateChecksum(resource);
        org.junit.Assert.assertEquals(oldResult, newResult);

    }

}
