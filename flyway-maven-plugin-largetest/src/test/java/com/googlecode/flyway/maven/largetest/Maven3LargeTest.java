package com.googlecode.flyway.maven.largetest;

/**
 * Executes the tests against Maven 3.
 */
public class Maven3LargeTest extends MavenTestCase {
    @Override
    protected String getMavenVersion() {
        return "3.0.5";
    }
}
