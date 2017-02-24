package org.flywaydb.commandline;

import org.junit.Test;

import java.io.File;
import java.io.FileFilter;

import static org.junit.Assert.assertEquals;

public class AssemblyMediumTest {
    @SuppressWarnings("ConstantConditions")
    @Test
    public void jreIncluded() {
        assertEquals(3, new File(System.getProperty("target.dir", "flyway-commandline/target"))
                .listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.isFile() && pathname.canRead()
                                && pathname.getName().contains("-x64.")
                                && pathname.length() > 48 * 1024 * 1024;
                    }
                }).length);
    }
}
