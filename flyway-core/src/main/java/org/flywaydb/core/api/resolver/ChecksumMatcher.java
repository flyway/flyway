package org.flywaydb.core.api.resolver;

interface ChecksumMatcher {
    boolean checksumMatches(Integer checksum);

    boolean checksumMatchesWithoutBeingIdentical(Integer checksum);
}