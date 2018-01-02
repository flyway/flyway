/*
 * Copyright 2010-2018 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
 */
package org.flywaydb.core;

/**
 * Wraps all interfaces used to express that specific test need DB to run
 * It can be used also to exclude some test from running if for example some DB is missing
 */
public class DbCategory {
    public interface DB {}
    public interface EmbeddedDB extends DB {}
    public interface DockerDB extends DB {}
    public interface SpecialDB extends DB {}

    public interface Derby extends EmbeddedDB {}
    public interface H2 extends EmbeddedDB {}
    public interface HSQL extends EmbeddedDB {}
    public interface SQLite extends EmbeddedDB {}

    public interface MySQL extends DockerDB {}
    public interface MariaDB extends DockerDB {}
    public interface PostgreSQL extends DockerDB {}
    public interface CockroachDB extends DockerDB {}
    public interface Oracle extends DockerDB {}
    public interface DB2 extends DockerDB {}
    public interface SQLServer extends DockerDB {}
    public interface SAPHANA extends DockerDB {}
    public interface SybaseASE extends DockerDB {}

    public interface Redshift extends SpecialDB {}

    // Other databases support maintained by the third party contributors
//    public interface EnterpriseDB extends ContributorSupportedDB {}
//    public interface Phoenix extends ContributorSupportedDB {}
//    public interface SolidDB extends ContributorSupportedDB {}
}
