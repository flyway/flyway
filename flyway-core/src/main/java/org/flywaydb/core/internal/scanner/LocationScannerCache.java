package org.flywaydb.core.internal.scanner;

import org.flywaydb.core.internal.scanner.classpath.ClassPathLocationScanner;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LocationScannerCache {

    /**
     * Cache the location scanner for each protocol.
     */
    private final Map<String, ClassPathLocationScanner> cache = new HashMap<>();

    public boolean containsKey(String protocol) {
        return cache.containsKey(protocol);
    }

    public ClassPathLocationScanner get(String protocol) {
        return cache.get(protocol);
    }

    public void put(String protocol, ClassPathLocationScanner scanner) {
        cache.put(protocol, scanner);
    }
}