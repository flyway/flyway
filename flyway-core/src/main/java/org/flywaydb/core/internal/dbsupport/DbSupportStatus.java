package org.flywaydb.core.internal.dbsupport;

/**
 * The status for the support of this database version with this version of Flyway.
 */
public enum DbSupportStatus {
    /**
     * This is a database version that was released after this version of Flyway and has therefore not been tested with it.
     */
    FUTURE,

    /**
     * This database version has been successfully tested with this version of Flyway.
     */
    OK,

    /**
     * This database version is older and no longer covered by regular support from its vendor.
     * Flyway Open Source no longer supports it, however support is still available in Flyway Enterprise.
     */
    NO_SUPPORT,

    /**
     * This database version is outdated and no longer supported by Flyway.
     */
    OUTDATED
}
