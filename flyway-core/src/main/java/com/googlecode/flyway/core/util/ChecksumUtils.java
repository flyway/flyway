package com.googlecode.flyway.core.util;


import com.googlecode.flyway.core.api.FlywayException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;

public class ChecksumUtils {

    /**
     * Stream calculate Checksum
     * @param resource
     * @return
     */
    public static int calculateChecksum(Resource resource) {
        InputStream input = null;
        try {
            input = new BufferedInputStream(resource.getInputStream());
            CRC32 crc = new CRC32();
            int i = 0;
            while ( (i=input.read()) != -1) {
                crc.update(i);
            }
           return (int)crc.getValue();

        } catch (IOException e) {
            throw new FlywayException("Unable to load resource: " + resource.getLocation(), e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ex) {
                }
            }
        }

    }
}
