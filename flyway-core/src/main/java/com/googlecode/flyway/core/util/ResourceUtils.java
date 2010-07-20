package com.googlecode.flyway.core.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * Utility class for dealing with classpath resources.
 */
public class ResourceUtils {
    /**
     * Prevents instantiation.
     */
    private ResourceUtils() {
        //Do nothing
    }

    /**
     * Loads the resource at this location within the classpath in a string using UTF-8 encoding.
     *
     * @param location The location of the resource on the classpath.
     * @return The resource contents as a string.
     */
    public static String loadResourceAsString(String location) {
        return loadResourceAsString(new ClassPathResource(location), "UTF-8");
    }

    /**
     * Loads this resource in a string using this encoding.
     *
     * @param resource The resource to load.
     * @param encoding The encoding of the resource.
     * @return The resource contents as a string.
     */
    public static String loadResourceAsString(Resource resource, String encoding) {
        try {
            InputStream inputStream = resource.getInputStream();
            Reader reader = new InputStreamReader(inputStream, Charset.forName(encoding));
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load resource: " + resource.getDescription() + " (encoding: " + encoding + ")", e);
        }
    }
}
