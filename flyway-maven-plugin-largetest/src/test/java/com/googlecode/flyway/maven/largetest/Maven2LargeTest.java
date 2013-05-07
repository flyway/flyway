package com.googlecode.flyway.maven.largetest;

/**
 * Executes the tests against Maven 2.
 */
public class Maven2LargeTest extends MavenTestCase {
    @Override
    protected String getMavenVersion() {
        return "2.2.1";
    }
}
