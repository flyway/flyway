package com.googlecode.flyway.core.util;

import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * Test for PropertiesUtils.
 */
public class PropertiesUtilsSmallTest {
    @Test
    public void getIntProperty() {
        Properties properties = new Properties();
        properties.setProperty("consoleWidth", "");
        assertEquals(80, PropertiesUtils.getIntProperty(properties, "consoleWidth", 80));
    }
}
