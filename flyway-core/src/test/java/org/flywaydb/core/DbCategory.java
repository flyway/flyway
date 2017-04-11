/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.core;

/**
 * Wraps all interfaces used to express that specific test need DB to run
 * It can be used also to exclude some test from running if for example some DB is missing
 */
public class DbCategory {
    public interface DB {}
    public interface InstallableDB extends DB {}
    public interface EmbeddedDB extends DB {}
    public interface OpenSourceDB extends InstallableDB {}
    public interface CommercialDB extends InstallableDB {}
    public interface ContributorSupportedDB extends CommercialDB {}

    // Embedded databases support maintained by the core Flyway team
    public interface Derby extends EmbeddedDB {}
    public interface H2 extends EmbeddedDB {}
    public interface HSQL extends EmbeddedDB {}
    public interface SQLite extends EmbeddedDB {}

    // Open-source databases support maintained by the core Flyway team
    public interface MySQL extends OpenSourceDB {}
    public interface MariaDB extends OpenSourceDB {}
    public interface PostgreSQL extends OpenSourceDB {}

    // Commercial databases support maintained by the core Flyway team
    public interface DB2 extends CommercialDB {}
    public interface Oracle extends CommercialDB {}
    public interface SQLServer extends CommercialDB {}

    // Other databases support maintained by the third party contributors
    public interface EnterpriseDB extends ContributorSupportedDB {}
    public interface Phoenix extends ContributorSupportedDB {}
    public interface GoogleCloudSQL extends ContributorSupportedDB {}
    public interface SapHana extends ContributorSupportedDB {}
    public interface DB2zOS extends ContributorSupportedDB {}
    public interface GreenPlum extends ContributorSupportedDB {}
    public interface Vertica extends ContributorSupportedDB {}
    public interface Redshift extends ContributorSupportedDB {}
    public interface SolidDB extends ContributorSupportedDB {}
    public interface SybaseASE extends ContributorSupportedDB {}
}
